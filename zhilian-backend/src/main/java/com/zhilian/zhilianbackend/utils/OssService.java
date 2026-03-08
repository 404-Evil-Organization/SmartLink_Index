package com.zhilian.zhilianbackend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
 * OSS服务类
 * 支持真实OSS上传和Stub模式（模拟上传）
 *
 * 优化点：
 * 1. 支持无配置自动降级为Stub模式
 * 2. 增强异常处理，统一包装为业务异常
 * 3. 优化URL解析，使用URI进行可靠的文件路径提取
 * 4. 添加配置完整性校验
 * 5. 添加文件类型白名单校验
 * 6. 增强URL解析的安全校验，防止越权操作
 * 7. 添加文件前缀目录限制
 */
@Slf4j
@Component
public class OssService {

    // 提供默认空值，避免启动失败
    @Value("${oss.endpoint:}")
    private String endpoint;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.bucket-name:}")
    private String bucketName;

    @Value("${oss.domain:}")
    private String domain;

    @Value("${app.upload.allow-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String[] allowTypes;

    @Value("${app.upload.allow-extensions:.jpg,.jpeg,.png,.gif,.webp,.pdf}")
    private String[] allowExtensions;

    @Value("${app.upload.max-size:10485760}") // 默认10MB
    private long maxSize;

    @Value("${app.upload.allowed-prefix:uploads}") // 允许的文件前缀目录
    private String allowedPrefix;

    private OSS ossClient;

    // 标志位，表示是否使用真实OSS
    private boolean useRealOss = false;

    // 配置完整性标志
    private boolean configComplete = false;

    // 允许的文件类型集合
    private Set<String> allowedMimeTypes;

    // 允许的文件扩展名集合
    private Set<String> allowedFileExtensions;

    @PostConstruct
    public void init() {
        // 初始化允许的文件类型
        initializeAllowedTypes();

        // 检查所有必要配置是否完整
        configComplete = checkConfiguration();

        if (!configComplete) {
            log.warn("OSS配置不完整，将使用Stub模式（模拟上传）。缺失配置：{}{}{}{}",
                    !hasText(endpoint) ? "endpoint " : "",
                    !hasText(accessKeyId) ? "accessKeyId " : "",
                    !hasText(accessKeySecret) ? "accessKeySecret " : "",
                    !hasText(bucketName) ? "bucketName " : "");
            useRealOss = false;
            return;
        }

        try {
            // 初始化OSS客户端
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 可选：验证bucket是否存在，确保配置正确
            if (ossClient.doesBucketExist(bucketName)) {
                useRealOss = true;
                log.info("OSS客户端初始化成功，Bucket：{}，Endpoint：{}", bucketName, endpoint);
            } else {
                log.error("OSS Bucket不存在：{}，将使用Stub模式", bucketName);
                useRealOss = false;
                ossClient.shutdown();
                ossClient = null;
            }
        } catch (Exception e) {
            log.error("OSS客户端初始化失败，将使用Stub模式", e);
            useRealOss = false;
            if (ossClient != null) {
                ossClient.shutdown();
                ossClient = null;
            }
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
     * 检查配置完整性
     */
    private boolean checkConfiguration() {
        return hasText(endpoint) &&
                hasText(accessKeyId) &&
                hasText(accessKeySecret) &&
                hasText(bucketName);
    }

    /**
     * 判断字符串是否有内容
     */
    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    /**
     * 上传文件 - 自动选择真实OSS或Stub模式
     */
    public String uploadFile(MultipartFile file, String directory) {
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
        if (useRealOss) {
            return realUploadFile(file, directory);
        } else {
            return stubUploadFile(file, directory);
        }
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

        // TODO: 可选：添加文件头魔数校验，提高安全性
        // 对于关键文件类型，可以进一步验证文件头魔数
    }

    /**
     * 真实OSS上传
     * 增强的异常处理
     */
    private String realUploadFile(MultipartFile file, String directory) {
        // 使用try-with-resources确保InputStream自动关闭
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
     * Stub模式上传
     */
    public String stubUploadFile(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = generateFileName(directory, fileExtension);

        log.info("[STUB] 模拟文件上传 - 原始文件名：{}，模拟路径：{}", originalFilename, fileName);

        // 返回模拟的URL
        return "https://" + (hasText(bucketName) ? bucketName : "stub-bucket") +
                ".oss-cn-stub.aliyuncs.com/" + fileName;
    }

    /**
     * 删除文件 - 接收内部objectKey（推荐方式）
     */
    public boolean deleteFileByKey(String objectKey) {
        if (!hasText(objectKey)) {
            log.warn("删除文件失败：objectKey为空");
            return false;
        }

        // 校验objectKey是否在允许的前缀目录下
        if (!isAllowedObjectKey(objectKey)) {
            log.warn("删除文件失败：objectKey不在允许的目录下 - {}", objectKey);
            return false;
        }

        if (!useRealOss) {
            log.info("[STUB] 模拟文件删除 - objectKey：{}", objectKey);
            return true;
        }

        try {
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
     * 删除文件 - 通过URL（带安全校验）
     */
    public boolean deleteFile(String fileUrl) {
        if (!hasText(fileUrl)) {
            log.warn("删除文件失败：文件URL为空");
            return false;
        }

        try {
            String objectKey = extractAndValidateObjectKey(fileUrl);
            if (objectKey == null) {
                log.warn("删除文件失败：无法从URL提取或校验失败 - {}", fileUrl);
                return false;
            }

            return deleteFileByKey(objectKey);

        } catch (Exception e) {
            log.error("删除文件失败 - 处理URL时发生异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在 - 通过内部objectKey
     */
    public boolean doesFileExistByKey(String objectKey) {
        if (!hasText(objectKey)) {
            return false;
        }

        // 校验objectKey是否在允许的前缀目录下
        if (!isAllowedObjectKey(objectKey)) {
            log.debug("objectKey不在允许的目录下：{}", objectKey);
            return false;
        }

        if (!useRealOss) {
            return true; // Stub模式默认返回true
        }

        try {
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
     * 检查文件是否存在 - 通过URL
     */
    public boolean doesFileExist(String fileUrl) {
        if (!hasText(fileUrl)) {
            return false;
        }

        try {
            String objectKey = extractAndValidateObjectKey(fileUrl);
            if (objectKey == null) {
                return false;
            }

            return doesFileExistByKey(objectKey);

        } catch (Exception e) {
            log.error("检查文件是否存在失败 - 处理URL时发生异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从URL提取并校验objectKey
     * @return 校验通过的objectKey，校验失败返回null
     */
    private String extractAndValidateObjectKey(String fileUrl) {
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

            // 校验objectKey是否在允许的目录下
            if (!isAllowedObjectKey(path)) {
                log.warn("objectKey不在允许的目录下：{}", path);
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
     * 规范化并校验目录，防止路径穿越和非法前后斜杠
     *
     * @param directory 原始目录参数，可能为 null、空或包含非法片段
     * @return 经过校验与规范化后的安全目录，不包含首尾斜杠
     */
    private String normalizeDirectory(String directory) {
        // 默认兜底目录，避免出现 null/ 或 // 等异常前缀
        final String defaultDir = "uploads";
        if (directory == null || directory.trim().isEmpty()) {
            return defaultDir;
        }

        // 统一分隔符为正斜杠
        String normalized = directory.trim().replace("\\", "/");

        // 合并重复斜杠
        normalized = normalized.replaceAll("/{2,}", "/");

        // 去掉首尾斜杠，避免生成 // 或 /xxx/yyy/ 这类多余层级
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            return defaultDir;
        }

        // 防止路径穿越：拒绝 "." 或 ".." 片段
        String[] segments = normalized.split("/");
        for (String segment : segments) {
            if (".".equals(segment) || "..".equals(segment)) {
                log.warn("检测到非法目录片段，已回退到默认目录。originalDirectory={}", directory);
                return defaultDir;
            }
        }

        return normalized;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String directory, String extension) {
        // 对 directory 做兜底校验与规范化，避免出现 null/、// 或路径穿越
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
     * 是否使用真实OSS
     */
    public boolean isUseRealOss() {
        return useRealOss;
    }

    /**
     * 配置是否完整
     */
    public boolean isConfigComplete() {
        return configComplete;
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