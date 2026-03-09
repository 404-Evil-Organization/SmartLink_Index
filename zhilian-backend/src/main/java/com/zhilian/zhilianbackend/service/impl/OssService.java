package com.zhilian.zhilianbackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * OSS文件存储服务 - 提供文件上传和删除功能
 */
@Slf4j
@Service
public class OssService {

    @Autowired
    private OSS ossClient;

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Value("${oss.domain:}")
    private String domain;

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${app.upload.max-size:10485760}") // 默认10MB
    private long maxSize;

    // 安全目录名称的正则表达式：只允许字母、数字、连字符、下划线、斜杠
    private static final Pattern SAFE_DIRECTORY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_/]+$");

    // 默认目录名
    private static final String DEFAULT_DIRECTORY = "uploads";

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 上传文件到OSS，按日期自动组织目录结构
     **/
    public String uploadFile(MultipartFile file, String directory) {
        // 基础校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制：" + maxSize + "字节");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            // 安全处理目录参数
            String safeDirectory = sanitizeDirectory(directory);

            // 生成文件路径：允许目录/日期/唯一文件名
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = safeDirectory + "/" + datePath + "/" +
                    UUID.randomUUID().toString().replace("-", "") + fileExtension;

            // 执行上传
            ossClient.putObject(new PutObjectRequest(bucketName, fileName, inputStream));

            log.info("文件上传成功：{}", fileName);

            // 返回访问URL
            return buildFileUrl(fileName);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 安全处理目录参数，防止路径遍历攻击
     **/
    private String sanitizeDirectory(String directory) {
        // 处理null或空值，使用默认目录
        if (directory == null || directory.trim().isEmpty()) {
            return DEFAULT_DIRECTORY;
        }

        // 去除首尾空格和斜杠
        String cleanDir = directory.trim();
        cleanDir = cleanDir.replaceAll("^[/\\\\]+|[/\\\\]+$", "");

        // 检查路径遍历攻击（包含..、反斜杠等）
        if (cleanDir.contains("..") ||
                cleanDir.contains("\\") ||
                cleanDir.contains("./") ||
                cleanDir.contains(".\\")) {
            throw new IllegalArgumentException("目录名称包含非法字符或路径遍历尝试");
        }

        // 替换反斜杠为正斜杠（统一分隔符）
        cleanDir = cleanDir.replace('\\', '/');

        // 检查是否只包含安全字符
        if (!SAFE_DIRECTORY_PATTERN.matcher(cleanDir).matches()) {
            throw new IllegalArgumentException("目录名称只能包含字母、数字、连字符、下划线和斜杠");
        }

        // 防止多层斜杠（例如：a//b -> a/b）
        cleanDir = cleanDir.replaceAll("/{2,}", "/");

        return cleanDir;
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 从OSS删除文件，支持URL或objectKey两种格式
     **/
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return false;
        }

        try {
            String objectKey = extractObjectKey(fileUrl);
            if (objectKey == null) {
                return false;
            }

            ossClient.deleteObject(bucketName, objectKey);
            log.info("文件删除成功：{}", objectKey);
            return true;

        } catch (Exception e) {
            log.error("文件删除失败", e);
            return false;
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 从URL中提取OSS objectKey
     **/
    private String extractObjectKey(String fileUrl) {
        if (fileUrl.startsWith("http")) {
            try {
                java.net.URI uri = new java.net.URI(fileUrl);
                String path = uri.getPath();
                return path != null && path.startsWith("/") ? path.substring(1) : path;
            } catch (Exception e) {
                log.warn("URL解析失败：{}", fileUrl);
                return null;
            }
        }
        return fileUrl;
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 构建文件的访问URL，支持自定义域名和标准OSS域名
     **/
    private String buildFileUrl(String fileName) {
        if (domain != null && !domain.trim().isEmpty()) {
            String cleanDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
            return cleanDomain + "/" + fileName;
        } else {
            // 使用标准OSS域名格式
            String cleanEndpoint = endpoint.replaceFirst("^https?://", "");
            if (cleanEndpoint.endsWith("/")) {
                cleanEndpoint = cleanEndpoint.substring(0, cleanEndpoint.length() - 1);
            }
            return "https://" + bucketName + "." + cleanEndpoint + "/" + fileName;
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 获取文件扩展名
     **/
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}