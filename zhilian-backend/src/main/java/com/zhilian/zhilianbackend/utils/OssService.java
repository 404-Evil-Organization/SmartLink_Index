package com.zhilian.zhilianbackend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class OssService {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Value("${oss.domain}")
    private String domain;

    private OSS ossClient;

    // 标志位，表示是否使用真实OSS
    private boolean useRealOss = false;

    @PostConstruct
    public void init() {
        // 检查accessKey是否配置
        if (accessKeyId == null || accessKeyId.trim().isEmpty() ||
                accessKeySecret == null || accessKeySecret.trim().isEmpty()) {
            log.warn("OSS AccessKey未配置，将使用Stub模式（模拟上传）");
            useRealOss = false;
            return;
        }

        try {
            // 初始化OSS客户端
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            useRealOss = true;
            log.info("OSS客户端初始化成功，Bucket：{}，Endpoint：{}", bucketName, endpoint);
        } catch (Exception e) {
            log.error("OSS客户端初始化失败，将使用Stub模式：{}", e.getMessage());
            useRealOss = false;
        }
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
        if (useRealOss) {
            return realUploadFile(file, directory);
        } else {
            return stubUploadFile(file, directory);
        }
    }

    /**
     * 真实OSS上传
     */
    private String realUploadFile(MultipartFile file, String directory) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = generateFileName(directory, fileExtension);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream()
            );

            PutObjectResult result = ossClient.putObject(putObjectRequest);

            log.info("文件上传成功 - 原始文件名：{}，OSS文件名：{}", originalFilename, fileName);

            return domain + "/" + fileName;

        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
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
        return "https://" + bucketName + ".oss-cn-stub.aliyuncs.com/" + fileName;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        if (!useRealOss) {
            return stubDeleteFile(fileUrl);
        }

        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            ossClient.deleteObject(bucketName, filePath);
            log.info("文件删除成功：{}", filePath);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Stub模式删除
     */
    public boolean stubDeleteFile(String fileUrl) {
        log.info("[STUB] 模拟文件删除 - URL：{}", fileUrl);
        return true;
    }

    /**
     * 检查文件是否存在
     */
    public boolean doesFileExist(String fileUrl) {
        if (!useRealOss) {
            return true; // Stub模式默认返回true
        }

        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            return ossClient.doesObjectExist(bucketName, filePath);
        } catch (Exception e) {
            log.error("检查文件是否存在失败：{}", e.getMessage(), e);
            return false;
        }
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
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String fileUrl) {
        return fileUrl.replace(domain + "/", "");
    }

    /**
     * 是否使用真实OSS
     */
    public boolean isUseRealOss() {
        return useRealOss;
    }
}