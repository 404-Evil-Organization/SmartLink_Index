package com.zhilian.zhilianbackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * OSS文件存储服务
 * 提供文件上传、删除、存在性检查等功能
 * 当OSS客户端不可用时，服务将不可用并抛出明确的异常信息
 */
@Slf4j
@Service
public class OssService {

    @Autowired(required = false)
    private OSS ossClient;

    @Value("${oss.bucket-name:}")
    private String bucketName;

    @Value("${oss.domain:}")
    private String domain;

    @Value("${oss.endpoint:}")
    private String endpoint;

    @Value("${app.upload.allow-types:image/jpeg,image/png,image/gif,application/pdf}")
    private String[] allowTypes;

    @Value("${app.upload.allow-extensions:.jpg,.jpeg,.png,.gif,.pdf}")
    private String[] allowExtensions;

    @Value("${app.upload.max-size:10485760}") // 默认10MB
    private long maxSize;

    @Value("${app.upload.allowed-prefix:uploads}") // 允许的文件前缀目录
    private String allowedPrefix;

    // 标志位，表示OSS客户端是否可用
    private boolean ossAvailable = false;

    // 允许的文件类型集合
    private Set<String> allowedMimeTypes;

    // 允许的文件扩展名集合
    private Set<String> allowedFileExtensions;

    // 规范化的允许前缀
    private String normalizedAllowedPrefix;

    /**
     * 初始化方法
     * 检查OSS客户端状态并初始化文件类型白名单
     */
    @PostConstruct
    public void init() {
        // 初始化允许的文件类型
        initializeAllowedTypes();

        // 初始化规范化允许前缀
        initializeAllowedPrefix();

        // 检查OSS客户端是否可用
        if (ossClient != null && hasText(bucketName)) {
            try {
                // 验证bucket是否存在
                if (ossClient.doesBucketExist(bucketName)) {
                    ossAvailable = true;
                    log.info("OSS服务初始化成功，Bucket：{}", bucketName);
                } else {
                    log.error("OSS Bucket不存在：{}，OSS服务将不可用", bucketName);
                    ossAvailable = false;
                }
            } catch (Exception e) {
                log.error("OSS服务初始化失败，OSS服务将不可用", e);
                ossAvailable = false;
            }
        } else {
            log.warn("OSS客户端未创建或Bucket名称为空，OSS服务将不可用");
            ossAvailable = false;
        }

        if (!ossAvailable) {
            log.error("OSS服务当前不可用，请检查配置和网络连接");
        }
    }

    /**
     * 销毁方法
     * 关闭OSS客户端
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    /**
     * 初始化允许的文件类型和扩展名
     */
    private void initializeAllowedTypes() {
        allowedMimeTypes = new HashSet<>();
        if (allowTypes != null) {
            allowedMimeTypes.addAll(Arrays.asList(allowTypes));
        }

        allowedFileExtensions = new HashSet<>();
        if (allowExtensions != null) {
            allowedFileExtensions.addAll(Arrays.asList(allowExtensions));
        }

        log.info("文件类型白名单初始化完成，允许的MIME类型：{}，允许的扩展名：{}",
                allowedMimeTypes, allowedFileExtensions);
    }

    /**
     * 初始化规范化允许前缀
     */
    private void initializeAllowedPrefix() {
        if (hasText(allowedPrefix)) {
            normalizedAllowedPrefix = normalizePath(allowedPrefix);
            log.info("允许的文件前缀已规范化：{}", normalizedAllowedPrefix);
        } else {
            normalizedAllowedPrefix = "uploads";
            log.info("使用默认允许前缀：{}", normalizedAllowedPrefix);
        }
    }

    /**
     * 上传文件
     *
     * @param file      待上传的文件
     * @param directory 文件存储目录
     * @return 文件的访问URL
     * @throws OssServiceException 当OSS服务不可用或上传失败时抛出
     * @throws IllegalArgumentException 当文件校验不通过时抛出
     */
    public String uploadFile(MultipartFile file, String directory) {
        // 检查OSS服务是否可用
        if (!ossAvailable) {
            log.error("上传失败：OSS服务当前不可用");
            throw new OssServiceException("OSS服务当前不可用，无法执行上传操作", OssErrorCode.SERVICE_UNAVAILABLE);
        }

        try {
            // 1. 基础校验
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("上传文件不能为空");
            }

            // 2. 文件大小校验
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException(String.format(
                        "文件大小超过限制：最大允许 %d 字节，当前文件 %d 字节", maxSize, file.getSize()));
            }

            // 3. 文件类型白名单校验
            validateFileType(file);

            // 4. 规范化并校验目录（确保在允许前缀下）
            String safeDirectory = normalizeAndValidateDirectory(directory);

            // 5. 执行上传
            return realUploadFile(file, safeDirectory);

        } catch (IllegalArgumentException e) {
            // 参数异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("文件上传过程中发生未知错误", e);
            throw new OssServiceException("文件上传失败：" + e.getMessage(), e, OssErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 规范化并校验目录，确保在允许前缀下
     */
    private String normalizeAndValidateDirectory(String directory) {
        String normalized = normalizePath(directory);

        // 确保目录在允许前缀下
        if (!normalized.startsWith(normalizedAllowedPrefix)) {
            log.warn("目录不在允许前缀下：{} (允许前缀：{})，将强制放置在允许前缀下",
                    normalized, normalizedAllowedPrefix);
            return normalizedAllowedPrefix;
        }

        return normalized;
    }

    /**
     * 文件类型白名单校验
     */
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename).toLowerCase();

        // 记录校验状态
        boolean mimeTypeValid = false;
        boolean extensionValid = false;

        // 1. MIME类型校验
        if (!allowedMimeTypes.isEmpty()) {
            if (contentType == null || contentType.trim().isEmpty()) {
                log.warn("文件MIME类型缺失，已启用MIME白名单时禁止上传，文件名={}", originalFilename);
                throw new IllegalArgumentException("文件MIME类型缺失，禁止上传");
            }
            if (!allowedMimeTypes.contains(contentType)) {
                log.warn("文件类型不被允许：MIME类型={}，文件名={}", contentType, originalFilename);
                throw new IllegalArgumentException("不支持的文件类型：" + contentType);
            }
            mimeTypeValid = true;
            log.debug("MIME类型校验通过：{}", contentType);
        }

        // 2. 扩展名校验
        if (!allowedFileExtensions.isEmpty()) {
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                log.warn("文件名为空，已启用扩展名白名单时禁止上传");
                throw new IllegalArgumentException("文件名为空，禁止上传");
            }

            if (fileExtension.isEmpty()) {
                log.warn("文件扩展名缺失，已启用扩展名白名单时禁止上传，文件名={}", originalFilename);
                throw new IllegalArgumentException("文件扩展名缺失，禁止上传");
            }

            // 移除扩展名前的点进行比较（如果配置中包含点）
            String extensionWithoutDot = fileExtension.startsWith(".") ?
                    fileExtension.substring(1) : fileExtension;

            // 检查两种格式：带点和不带点
            boolean containsWithDot = allowedFileExtensions.contains(fileExtension);
            boolean containsWithoutDot = allowedFileExtensions.contains(extensionWithoutDot);

            if (!containsWithDot && !containsWithoutDot) {
                log.warn("文件扩展名不被允许：扩展名={}，文件名={}", fileExtension, originalFilename);
                throw new IllegalArgumentException("不支持的文件扩展名：" + fileExtension);
            }
            extensionValid = true;
            log.debug("扩展名校验通过：{}", fileExtension);
        }

        // 3. 严格模式检查：至少有一种校验方式通过
        if (allowedMimeTypes.isEmpty() && allowedFileExtensions.isEmpty()) {
            // 如果两个白名单都为空，视为未配置类型校验，但需要记录警告
            log.warn("文件类型白名单未配置，任何文件都可以上传！请检查配置");
            // 建议：可以抛出异常要求配置至少一种校验方式
            // throw new IllegalStateException("系统未配置文件类型校验规则，请联系管理员");
        } else if (!mimeTypeValid && !extensionValid) {
            // 理论上不会执行到这里，因为前面的校验会直接抛出异常
            // 但保留这个安全检查
            log.error("文件类型校验失败：既未通过MIME校验也未通过扩展名校验，文件名={}", originalFilename);
            throw new IllegalArgumentException("文件类型校验失败");
        }

        // 4. 可选：MIME类型与扩展名一致性校验
        if (mimeTypeValid && extensionValid) {
            validateMimeTypeExtensionConsistency(contentType, fileExtension, originalFilename);
        }
    }

    /**
     * 验证MIME类型与扩展名是否一致（防止伪装文件）
     */
    private void validateMimeTypeExtensionConsistency(String contentType, String extension, String filename) {
        // 定义常见的MIME类型与扩展名对应关系
        java.util.Map<String, java.util.Set<String>> mimeTypeExtensionMap = new java.util.HashMap<>();
        mimeTypeExtensionMap.put("image/jpeg", new java.util.HashSet<>(java.util.Arrays.asList(".jpg", ".jpeg")));
        mimeTypeExtensionMap.put("image/png", new java.util.HashSet<>(java.util.Arrays.asList(".png")));
        mimeTypeExtensionMap.put("image/gif", new java.util.HashSet<>(java.util.Arrays.asList(".gif")));
        mimeTypeExtensionMap.put("application/pdf", new java.util.HashSet<>(java.util.Arrays.asList(".pdf")));

        // 如果MIME类型在映射中，检查扩展名是否匹配
        if (mimeTypeExtensionMap.containsKey(contentType)) {
            if (!mimeTypeExtensionMap.get(contentType).contains(extension)) {
                log.warn("MIME类型与扩展名不一致：MIME={}，扩展名={}，文件名={}",
                        contentType, extension, filename);
                throw new IllegalArgumentException("文件类型与扩展名不匹配");
            }
        }
    }

    /**
     * 真实OSS上传
     */
    private String realUploadFile(MultipartFile file, String directory) {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            // 生成文件名时确保在允许前缀下
            String fileName = generateFileName(directory, fileExtension);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream
            );

            // 执行上传
            ossClient.putObject(putObjectRequest);

            log.info("文件上传成功 - 原始文件名：{}，OSS文件名：{}", originalFilename, fileName);

            // 构建访问URL
            String fileUrl = buildFileUrl(fileName);
            log.debug("生成的文件访问URL：{}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败 - IO异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件上传失败：读取文件内容出错", e, OssErrorCode.IO_ERROR);
        } catch (OSSException e) {
            log.error("文件上传失败 - OSS异常：错误码={}，错误消息={}，请求ID={}",
                    e.getErrorCode(), e.getErrorMessage(), e.getRequestId(), e);
            throw new OssServiceException("文件上传失败：OSS服务异常 - " + e.getErrorMessage(),
                    e, mapOssErrorCode(e.getErrorCode()));
        } catch (ClientException e) {
            log.error("文件上传失败 - 客户端异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件上传失败：网络或客户端异常 - " + e.getMessage(),
                    e, OssErrorCode.CLIENT_ERROR);
        } catch (Exception e) {
            log.error("文件上传失败 - 未知异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件上传失败：未知错误", e, OssErrorCode.UNKNOWN_ERROR);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 映射OSS错误码到业务错误码
     */
    private OssErrorCode mapOssErrorCode(String ossErrorCode) {
        if (ossErrorCode == null) {
            return OssErrorCode.OSS_SERVER_ERROR;
        }

        switch (ossErrorCode) {
            case "NoSuchBucket":
                return OssErrorCode.BUCKET_NOT_FOUND;
            case "AccessDenied":
                return OssErrorCode.ACCESS_DENIED;
            case "SignatureDoesNotMatch":
                return OssErrorCode.AUTHENTICATION_FAILED;
            case "InvalidAccessKeyId":
                return OssErrorCode.INVALID_ACCESS_KEY;
            case "RequestTimeout":
                return OssErrorCode.REQUEST_TIMEOUT;
            default:
                return OssErrorCode.OSS_SERVER_ERROR;
        }
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String fileName) {
        if (hasText(domain)) {
            String cleanDomain = domain.endsWith("/") ?
                    domain.substring(0, domain.length() - 1) : domain;
            return cleanDomain + "/" + fileName;
        } else {
            String cleanEndpoint = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
            try {
                URI uri = new URI(cleanEndpoint);
                String host = uri.getHost();
                String path = uri.getPath();
                int port = uri.getPort();

                if (host != null && !host.isEmpty()) {
                    StringBuilder urlBuilder = new StringBuilder();
                    urlBuilder.append("https://")
                            .append(bucketName)
                            .append(".")
                            .append(host);

                    if (port != -1) {
                        urlBuilder.append(":").append(port);
                    }

                    if (path != null && !path.isEmpty() && !"/".equals(path)) {
                        if (!path.startsWith("/")) {
                            urlBuilder.append("/");
                        }
                        urlBuilder.append(path);
                    }

                    if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                        urlBuilder.append("/");
                    }
                    urlBuilder.append(fileName);
                    return urlBuilder.toString();
                }

                // 降级逻辑
                String fallbackEndpoint = cleanEndpoint.replaceFirst("^https?://", "");
                if (fallbackEndpoint.endsWith("/")) {
                    fallbackEndpoint = fallbackEndpoint.substring(0, fallbackEndpoint.length() - 1);
                }
                return "https://" + bucketName + "." + fallbackEndpoint + "/" + fileName;

            } catch (URISyntaxException e) {
                log.warn("解析OSS endpoint失败，使用降级拼接方式 - endpoint：{}", endpoint, e);
                String fallbackEndpoint = cleanEndpoint.replaceFirst("^https?://", "");
                if (fallbackEndpoint.endsWith("/")) {
                    fallbackEndpoint = fallbackEndpoint.substring(0, fallbackEndpoint.length() - 1);
                }
                return "https://" + bucketName + "." + fallbackEndpoint + "/" + fileName;
            }
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrlOrObjectKey 文件的访问URL或OSS对象键
     * @return 删除是否成功
     * @throws OssServiceException 当OSS服务不可用时抛出
     */
    public boolean deleteFile(String fileUrlOrObjectKey) {
        if (!ossAvailable) {
            log.error("删除失败：OSS服务当前不可用");
            throw new OssServiceException("OSS服务当前不可用，无法执行删除操作", OssErrorCode.SERVICE_UNAVAILABLE);
        }

        if (!hasText(fileUrlOrObjectKey)) {
            log.warn("删除文件失败：参数为空");
            return false;
        }

        try {
            String objectKey = extractObjectKey(fileUrlOrObjectKey);
            if (objectKey == null) {
                return false;
            }

            // 校验objectKey是否在允许的目录下
            if (!isAllowedObjectKey(objectKey)) {
                log.warn("删除文件失败：objectKey不在允许的目录下 - {}", objectKey);
                throw new OssServiceException("无权删除该文件：文件不在允许的目录下", OssErrorCode.FORBIDDEN);
            }

            // 执行删除
            ossClient.deleteObject(bucketName, objectKey);
            log.info("文件删除成功：{}", objectKey);
            return true;

        } catch (OSSException e) {
            log.error("文件删除失败 - OSS异常：错误码={}，错误消息={}", e.getErrorCode(), e.getErrorMessage(), e);
            throw new OssServiceException("文件删除失败：" + e.getErrorMessage(),
                    e, mapOssErrorCode(e.getErrorCode()));
        } catch (ClientException e) {
            log.error("文件删除失败 - 客户端异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件删除失败：网络或客户端异常", e, OssErrorCode.CLIENT_ERROR);
        } catch (Exception e) {
            log.error("文件删除失败 - 未知异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件删除失败：未知错误", e, OssErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileUrlOrObjectKey 文件的访问URL或OSS对象键
     * @return 文件是否存在
     * @throws OssServiceException 当OSS服务不可用时抛出
     */
    public boolean doesFileExist(String fileUrlOrObjectKey) {
        if (!ossAvailable) {
            log.error("检查失败：OSS服务当前不可用");
            throw new OssServiceException("OSS服务当前不可用，无法执行检查操作", OssErrorCode.SERVICE_UNAVAILABLE);
        }

        if (!hasText(fileUrlOrObjectKey)) {
            return false;
        }

        try {
            String objectKey = extractObjectKey(fileUrlOrObjectKey);
            if (objectKey == null) {
                return false;
            }

            // 校验objectKey是否在允许的目录下
            if (!isAllowedObjectKey(objectKey)) {
                log.debug("objectKey不在允许的目录下：{}", objectKey);
                return false;
            }

            return ossClient.doesObjectExist(bucketName, objectKey);

        } catch (OSSException e) {
            log.error("检查文件是否存在失败 - OSS异常：错误码={}，错误消息={}", e.getErrorCode(), e.getErrorMessage(), e);
            throw new OssServiceException("检查文件存在失败：" + e.getErrorMessage(),
                    e, mapOssErrorCode(e.getErrorCode()));
        } catch (ClientException e) {
            log.error("检查文件是否存在失败 - 客户端异常：{}", e.getMessage(), e);
            throw new OssServiceException("检查文件存在失败：网络或客户端异常", e, OssErrorCode.CLIENT_ERROR);
        } catch (Exception e) {
            log.error("检查文件是否存在失败 - 未知异常：{}", e.getMessage(), e);
            throw new OssServiceException("检查文件存在失败：未知错误", e, OssErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 从URL或objectKey中提取objectKey
     */
    private String extractObjectKey(String fileUrlOrObjectKey) {
        if (isUrl(fileUrlOrObjectKey)) {
            return extractObjectKeyFromUrl(fileUrlOrObjectKey);
        }
        return fileUrlOrObjectKey;
    }

    /**
     * 判断字符串是否为URL
     */
    private boolean isUrl(String str) {
        return str != null && (str.startsWith("http://") || str.startsWith("https://"));
    }

    /**
     * 从URL提取objectKey
     *
     * @return 提取出的objectKey，提取失败返回null
     */
    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            if (path == null || path.isEmpty()) {
                log.warn("URL路径为空：{}", fileUrl);
                return null;
            }

            // 移除开头的斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // 校验域名是否匹配
            if (!validateUrlHost(uri)) {
                return null;
            }

            return path;

        } catch (URISyntaxException e) {
            log.error("URL格式错误：{} - {}", fileUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 校验URL的host是否合法
     */
    private boolean validateUrlHost(URI uri) {
        String actualHost = uri.getHost();
        if (actualHost == null) {
            log.warn("URL缺少有效host");
            return false;
        }

        // 如果配置了自定义domain
        if (hasText(domain)) {
            try {
                URI domainUri = new URI(domain);
                String expectedHost = domainUri.getHost();
                if (expectedHost != null && !expectedHost.equals(actualHost)) {
                    log.warn("URL域名不匹配：期望 {}，实际 {}，URL：{}", expectedHost, actualHost, uri);
                    return false;
                }
            } catch (URISyntaxException e) {
                log.warn("domain配置格式错误：{}", domain);
                return false;
            }
        }
        // 如果没有自定义domain，校验是否为合法的OSS域名
        else if (hasText(bucketName) && hasText(endpoint)) {
            String endpointHost = extractHostFromEndpoint();
            if (endpointHost != null) {
                String expectedHost = bucketName + "." + endpointHost;
                if (!expectedHost.equals(actualHost)) {
                    log.warn("URL域名不匹配：期望 {}，实际 {}，URL：{}", expectedHost, actualHost, uri);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 从endpoint配置中提取host
     */
    private String extractHostFromEndpoint() {
        if (!hasText(endpoint)) {
            return null;
        }

        try {
            if (endpoint.startsWith("http")) {
                URI endpointUri = new URI(endpoint);
                return endpointUri.getHost();
            } else {
                return endpoint;
            }
        } catch (URISyntaxException e) {
            return endpoint;
        }
    }

    /**
     * 校验objectKey是否在允许的目录下
     */
    private boolean isAllowedObjectKey(String objectKey) {
        if (!hasText(objectKey)) {
            return false;
        }

        // 检查是否以允许的前缀开头
        if (!objectKey.startsWith(normalizedAllowedPrefix + "/") && !objectKey.equals(normalizedAllowedPrefix)) {
            log.debug("objectKey不在允许的目录下：{} (允许的前缀：{})", objectKey, normalizedAllowedPrefix);
            return false;
        }

        // 防止路径穿越攻击
        if (objectKey.contains("../") || objectKey.contains("..\\")) {
            log.warn("objectKey包含路径穿越字符：{}", objectKey);
            return false;
        }

        return true;
    }

    /**
     * 规范化路径
     */
    private String normalizePath(String path) {
        final String defaultPath = "uploads";

        if (path == null || path.trim().isEmpty()) {
            return defaultPath;
        }

        String normalized = path.trim().replace("\\", "/");
        normalized = normalized.replaceAll("/{2,}", "/");

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            return defaultPath;
        }

        String[] segments = normalized.split("/");
        for (String segment : segments) {
            if (".".equals(segment) || "..".equals(segment)) {
                log.warn("检测到非法目录片段，已回退到默认目录。原始目录={}", path);
                return defaultPath;
            }
        }

        return normalized;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String directory, String extension) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "")
                + "_" + System.currentTimeMillis()
                + extension;
        return directory + "/" + datePath + "/" + uniqueFileName;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * OSS服务是否可用
     */
    public boolean isOssAvailable() {
        return ossAvailable;
    }

    /**
     * 判断字符串是否有内容
     */
    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * OSS错误码枚举
     */
    public enum OssErrorCode {
        SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "OSS服务不可用"),
        IO_ERROR("IO_ERROR", "IO异常"),
        CLIENT_ERROR("CLIENT_ERROR", "客户端异常"),
        OSS_SERVER_ERROR("OSS_SERVER_ERROR", "OSS服务器异常"),
        BUCKET_NOT_FOUND("BUCKET_NOT_FOUND", "Bucket不存在"),
        ACCESS_DENIED("ACCESS_DENIED", "访问被拒绝"),
        AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "认证失败"),
        INVALID_ACCESS_KEY("INVALID_ACCESS_KEY", "无效的AccessKey"),
        REQUEST_TIMEOUT("REQUEST_TIMEOUT", "请求超时"),
        FORBIDDEN("FORBIDDEN", "无权操作"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误");

        private final String code;
        private final String message;

        OssErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 自定义OSS服务异常
     */
    public static class OssServiceException extends RuntimeException {
        private final OssErrorCode errorCode;

        public OssServiceException(String message, Throwable cause, OssErrorCode errorCode) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public OssServiceException(String message, OssErrorCode errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public OssErrorCode getErrorCode() {
            return errorCode;
        }

        @Override
        public String toString() {
            return String.format("OssServiceException{errorCode=%s, message=%s}",
                    errorCode, getMessage());
        }
    }
}