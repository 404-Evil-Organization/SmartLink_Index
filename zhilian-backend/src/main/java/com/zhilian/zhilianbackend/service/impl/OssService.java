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

    @Value("${app.upload.allow-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String[] allowTypes;

    @Value("${app.upload.allow-extensions:.jpg,.jpeg,.png,.gif,.webp,.pdf}")
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

    /**
     * 初始化方法
     * 检查OSS客户端状态并初始化文件类型白名单
     */
    @PostConstruct
    public void init() {
        // 初始化允许的文件类型
        initializeAllowedTypes();

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
     * 上传文件
     *
     * @param file      待上传的文件
     * @param directory 文件存储目录
     * @return 文件的访问URL
     * @throws IllegalStateException 当OSS服务不可用时抛出
     * @throws IllegalArgumentException 当文件校验不通过时抛出
     * @throws OssServiceException 当上传过程中发生异常时抛出
     */
    public String uploadFile(MultipartFile file, String directory) {
        // 检查OSS服务是否可用
        if (!ossAvailable) {
            log.error("上传失败：OSS服务当前不可用");
            throw new IllegalStateException("OSS服务当前不可用，无法执行上传操作");
        }

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

        // 4. 执行上传
        return realUploadFile(file, directory);
    }

    /**
     * 文件类型白名单校验
     */
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename).toLowerCase();

        // MIME类型校验
        if (contentType != null && !allowedMimeTypes.isEmpty()) {
            if (!allowedMimeTypes.contains(contentType)) {
                log.warn("文件类型不被允许：MIME类型={}，文件名={}", contentType, originalFilename);
                throw new IllegalArgumentException("不支持的文件类型：" + contentType);
            }
        }

        // 文件扩展名校验
        if (!fileExtension.isEmpty() && !allowedFileExtensions.isEmpty()) {
            if (!allowedFileExtensions.contains(fileExtension)) {
                log.warn("文件扩展名不被允许：扩展名={}，文件名={}", fileExtension, originalFilename);
                throw new IllegalArgumentException("不支持的文件扩展名：" + fileExtension);
            }
        }
    }

    /**
     * 真实OSS上传
     */
    private String realUploadFile(MultipartFile file, String directory) {
        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
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
            throw new OssServiceException("文件上传失败：读取文件内容出错", e);
        } catch (OSSException | ClientException e) {
            log.error("文件上传失败 - OSS异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件上传失败：OSS服务异常 - " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("文件上传失败 - 未知异常：{}", e.getMessage(), e);
            throw new OssServiceException("文件上传失败：未知错误", e);
        }
    }

    /**
     * 构建文件访问URL
     */
    private String buildFileUrl(String fileName) {
        if (hasText(domain)) {
            // 确保domain不以斜杠结尾
            String cleanDomain = domain.endsWith("/") ?
                    domain.substring(0, domain.length() - 1) : domain;
            return cleanDomain + "/" + fileName;
        } else {
            // 如果没有配置domain，使用endpoint构建
            String cleanEndpoint = endpoint.startsWith("http") ? endpoint : "https://" + endpoint;
            if (cleanEndpoint.endsWith("/")) {
                cleanEndpoint = cleanEndpoint.substring(0, cleanEndpoint.length() - 1);
            }
            return "https://" + bucketName + "." + cleanEndpoint.replace("https://", "") + "/" + fileName;
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrlOrObjectKey 文件的访问URL或OSS对象键
     * @return 删除是否成功
     * @throws IllegalStateException 当OSS服务不可用时抛出
     */
    public boolean deleteFile(String fileUrlOrObjectKey) {
        if (!ossAvailable) {
            log.error("删除失败：OSS服务当前不可用");
            throw new IllegalStateException("OSS服务当前不可用，无法执行删除操作");
        }

        if (!hasText(fileUrlOrObjectKey)) {
            log.warn("删除文件失败：参数为空");
            return false;
        }

        try {
            // 判断是URL还是objectKey
            String objectKey;
            if (isUrl(fileUrlOrObjectKey)) {
                // 如果是URL，提取objectKey
                objectKey = extractObjectKeyFromUrl(fileUrlOrObjectKey);
                if (objectKey == null) {
                    log.warn("删除文件失败：无法从URL提取objectKey - {}", fileUrlOrObjectKey);
                    return false;
                }
            } else {
                // 直接作为objectKey使用
                objectKey = fileUrlOrObjectKey;
            }

            // 校验objectKey是否在允许的目录下
            if (!isAllowedObjectKey(objectKey)) {
                log.warn("删除文件失败：objectKey不在允许的目录下 - {}", objectKey);
                return false;
            }

            // 执行删除
            ossClient.deleteObject(bucketName, objectKey);
            log.info("文件删除成功：{}", objectKey);
            return true;

        } catch (OSSException | ClientException e) {
            log.error("文件删除失败 - OSS异常：{}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("文件删除失败 - 未知异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileUrlOrObjectKey 文件的访问URL或OSS对象键
     * @return 文件是否存在
     * @throws IllegalStateException 当OSS服务不可用时抛出
     */
    public boolean doesFileExist(String fileUrlOrObjectKey) {
        if (!ossAvailable) {
            log.error("检查失败：OSS服务当前不可用");
            throw new IllegalStateException("OSS服务当前不可用，无法执行检查操作");
        }

        if (!hasText(fileUrlOrObjectKey)) {
            return false;
        }

        try {
            // 判断是URL还是objectKey
            String objectKey;
            if (isUrl(fileUrlOrObjectKey)) {
                objectKey = extractObjectKeyFromUrl(fileUrlOrObjectKey);
                if (objectKey == null) {
                    return false;
                }
            } else {
                objectKey = fileUrlOrObjectKey;
            }

            // 校验objectKey是否在允许的目录下
            if (!isAllowedObjectKey(objectKey)) {
                log.debug("objectKey不在允许的目录下：{}", objectKey);
                return false;
            }

            return ossClient.doesObjectExist(bucketName, objectKey);

        } catch (OSSException | ClientException e) {
            log.error("检查文件是否存在失败 - OSS异常：{}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("检查文件是否存在失败 - 未知异常：{}", e.getMessage(), e);
            return false;
        }
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

            // 校验域名是否匹配（可选，用于增强安全性）
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
     * 校验URL的host是否合法（增强安全性）
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

        // 如果配置了允许的前缀，检查objectKey是否以该前缀开头
        if (hasText(allowedPrefix)) {
            // 规范化前缀，确保不以斜杠开头
            String normalizedPrefix = allowedPrefix;
            if (normalizedPrefix.startsWith("/")) {
                normalizedPrefix = normalizedPrefix.substring(1);
            }

            // 检查是否以允许的前缀开头
            if (!objectKey.startsWith(normalizedPrefix + "/") && !objectKey.equals(normalizedPrefix)) {
                log.debug("objectKey不在允许的目录下：{} (允许的前缀：{})", objectKey, normalizedPrefix);
                return false;
            }
        }

        // 防止路径穿越攻击
        if (objectKey.contains("../") || objectKey.contains("..\\")) {
            log.warn("objectKey包含路径穿越字符：{}", objectKey);
            return false;
        }

        return true;
    }

    /**
     * 规范化并校验目录
     *
     * @param directory 原始目录参数
     * @return 经过校验与规范化后的安全目录
     */
    private String normalizeDirectory(String directory) {
        final String defaultDir = "uploads";
        if (directory == null || directory.trim().isEmpty()) {
            return defaultDir;
        }

        String normalized = directory.trim().replace("\\", "/");
        normalized = normalized.replaceAll("/{2,}", "/");

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            return defaultDir;
        }

        String[] segments = normalized.split("/");
        for (String segment : segments) {
            if (".".equals(segment) || "..".equals(segment)) {
                log.warn("检测到非法目录片段，已回退到默认目录。原始目录={}", directory);
                return defaultDir;
            }
        }

        return normalized;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String directory, String extension) {
        String safeDirectory = normalizeDirectory(directory);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "")
                + "_" + System.currentTimeMillis()
                + extension;
        return safeDirectory + "/" + datePath + "/" + uniqueFileName;
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
     * 自定义OSS服务异常
     */
    public static class OssServiceException extends RuntimeException {
        public OssServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public OssServiceException(String message) {
            super(message);
        }
    }
}