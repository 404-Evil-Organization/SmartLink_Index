package com.zhilian.zhilianbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * OSS客户端配置类
 * 负责创建和管理OSSClient Bean实例
 * 在Spring Boot启动时将OSSClient注册到容器中
 */
@Slf4j
@Configuration
public class OssConfig {

    @Value("${oss.endpoint:}")
    private String endpoint;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.bucket-name:}")
    private String bucketName;

    /**
     * 创建OSS客户端Bean
     * 只有在配置完整的情况下才会创建真实的OSSClient
     * 否则返回null，由OssService处理降级逻辑
     */
    @Bean
    public OSS ossClient() {
        // 检查配置是否完整
        if (!hasText(endpoint) || !hasText(accessKeyId) ||
                !hasText(accessKeySecret) || !hasText(bucketName)) {
            log.warn("OSS配置不完整，将不创建OSS客户端。缺失配置：{}{}{}{}",
                    !hasText(endpoint) ? "endpoint " : "",
                    !hasText(accessKeyId) ? "accessKeyId " : "",
                    !hasText(accessKeySecret) ? "accessKeySecret " : "",
                    !hasText(bucketName) ? "bucketName " : "");
            return null;
        }

        try {
            // 创建OSS客户端
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 验证bucket是否存在，确保配置正确
            if (ossClient.doesBucketExist(bucketName)) {
                log.info("OSS客户端创建成功，Bucket：{}，Endpoint：{}", bucketName, endpoint);
                return ossClient;
            } else {
                log.error("OSS Bucket不存在：{}，将不创建OSS客户端", bucketName);
                ossClient.shutdown();
                return null;
            }
        } catch (Exception e) {
            log.error("OSS客户端创建失败", e);
            return null;
        }
    }

    /**
     * 判断字符串是否有内容
     */
    private boolean hasText(String str) {
        return StringUtils.hasText(str);
    }
}