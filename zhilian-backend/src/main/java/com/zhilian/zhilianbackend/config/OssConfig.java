package com.zhilian.zhilianbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS客户端配置类
 * 只有在所有配置属性都存在时才创建OSS客户端
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        name = {
                "oss.endpoint",
                "oss.access-key-id",
                "oss.access-key-secret",
                "oss.bucket-name"
        },
        havingValue = "true",  // 配置项需要非空值
        matchIfMissing = false
)
public class OssConfig {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.bucket-name}")
    private String bucketName;

    /**
     * 创建OSS客户端Bean
     */
    @Bean
    public OSS ossClient() {
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 验证bucket是否存在，确保配置正确
            if (ossClient.doesBucketExist(bucketName)) {
                log.info("OSS客户端创建成功，Bucket：{}，Endpoint：{}", bucketName, endpoint);
                return ossClient;
            } else {
                log.error("OSS Bucket不存在：{}", bucketName);
                ossClient.shutdown();
                throw new IllegalStateException("OSS Bucket不存在: " + bucketName);
            }
        } catch (Exception e) {
            log.error("OSS客户端创建失败", e);
            throw new IllegalStateException("OSS客户端创建失败", e);
        }
    }
}