package com.zhilian.zhilianbackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
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
 * @Param:
 * @Return:
 * @Description: 阿里云OSS服务类，提供文件上传和删除功能
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

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
    public String uploadFile(MultipartFile file) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

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
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName;
            if (fileUrl.startsWith("http")) {
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            } else {
                fileName = fileUrl;
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