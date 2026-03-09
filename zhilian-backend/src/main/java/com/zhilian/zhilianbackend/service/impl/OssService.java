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

            // 生成文件路径：允许目录/日期/唯一文件名
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = directory + "/" + datePath + "/" +
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