package com.zhilian.zhilianbackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.zhilian.zhilianbackend.service.OssService;  // 导入接口
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @Author:xiaodengyou
 * @Date: 2026/3/10 11:30
 * @Description: 阿里云OSS服务类，提供文件上传和删除功能
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {  // 添加 implements OssService

    private final OSS ossClient;

    @Value("${oss.bucket-name}")
    private String bucketName;

    @Value("${oss.domain}")
    private String domain;

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 11:31
     * @Param: file 要上传的文件
     * @Return: String 文件访问URL
     * @Description: 上传文件到OSS
     **/
    @Override  // 添加 @Override 注解
    public String uploadFile(MultipartFile file) {
        try {
            // 生成唯一且安全的文件名
            String originalFilename = file.getOriginalFilename();
            // 1. 提取原始文件名的最后一段（去除可能携带的路径，比如 "../a/b/c.txt"）
            String safeBaseName = "file";
            String extension = "";
            if (originalFilename != null) {
                // 兼容 Windows 路径分隔符，将 "\" 统一转换为 "/"
                String normalized = originalFilename.replace("\\", "/");
                int lastSlashIndex = normalized.lastIndexOf('/');
                if (lastSlashIndex >= 0 && lastSlashIndex < normalized.length() - 1) {
                    normalized = normalized.substring(lastSlashIndex + 1);
                }
                // 2. 提取扩展名（如 ".png"），其余部分作为文件名主体
                int lastDotIndex = normalized.lastIndexOf('.');
                if (lastDotIndex > 0 && lastDotIndex < normalized.length() - 1) {
                    safeBaseName = normalized.substring(0, lastDotIndex);
                    extension = normalized.substring(lastDotIndex); // 保留点号，例如 ".png"
                } else {
                    safeBaseName = normalized;
                }
                // 3. 清洗文件名主体，仅保留字母、数字、点、下划线和中划线，其他字符统一替换为下划线
                safeBaseName = safeBaseName.replaceAll("[^A-Za-z0-9._-]", "_").trim();
                // 4. 限制文件名长度，避免过长导致存储或展示问题
                if (safeBaseName.length() > 50) {
                    safeBaseName = safeBaseName.substring(0, 50);
                }
            }
            // 5. 如果清洗后文件名主体为空，使用安全默认名
            if (safeBaseName == null || safeBaseName.isEmpty()) {
                safeBaseName = "file";
            }
            // 6. 如果没有扩展名，使用安全的默认扩展名，防止客户端无法识别类型
            if (extension == null || extension.isEmpty()) {
                extension = ".bin";
            }
            String fileName = UUID.randomUUID().toString() + "_" + safeBaseName + extension;
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream());

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 返回文件访问URL（使用配置的domain）
            return domain + "/" + fileName;

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 11:31
     * @Param: fileUrl 要删除的文件URL或文件名
     * @Return: boolean true-删除成功 false-删除失败
     * @Description: 从OSS删除文件
     **/
    @Override  // 添加 @Override 注解
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName;
            if (fileUrl.startsWith("http")) {
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            } else {
                fileName = fileUrl;
            }
            // 兼容带查询参数的签名URL，去掉文件名中的 query string，避免删除失败
            int queryIndex = fileName.indexOf("?");
            if (queryIndex != -1) {
                fileName = fileName.substring(0, queryIndex);
            }

            // 删除文件
            ossClient.deleteObject(bucketName, fileName);
            return true;

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }
}