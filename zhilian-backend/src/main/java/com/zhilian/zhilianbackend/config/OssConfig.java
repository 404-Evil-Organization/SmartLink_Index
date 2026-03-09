package com.zhilian.zhilianbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * OSS客户端配置类
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        prefix = "oss",
        name = {"endpoint", "access-key-id", "access-key-secret", "bucket-name"}
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
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 创建OSS客户端Bean，并验证Bucket是否存在
     **/
    @Bean
    public OSS ossClient() {
        validateConfiguration();

        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            if (ossClient.doesBucketExist(bucketName)) {
                log.info("OSS客户端创建成功，Bucket：{}", bucketName);
                return ossClient;
            } else {
                ossClient.shutdown();
                throw new IllegalStateException("OSS Bucket不存在: " + bucketName);
            }
        } catch (Exception e) {
            log.error("OSS客户端创建失败", e);
            throw new IllegalStateException("OSS客户端创建失败", e);
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 验证OSS配置参数不能为空
     **/
    private void validateConfiguration() {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("OSS endpoint不能为空");
        }
        if (!StringUtils.hasText(accessKeyId)) {
            throw new IllegalStateException("OSS access-key-id不能为空");
        }
        if (!StringUtils.hasText(accessKeySecret)) {
            throw new IllegalStateException("OSS access-key-secret不能为空");
        }
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("OSS bucket-name不能为空");
        }
    }
}