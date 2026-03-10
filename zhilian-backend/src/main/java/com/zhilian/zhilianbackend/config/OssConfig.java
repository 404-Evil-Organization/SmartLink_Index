package com.zhilian.zhilianbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:xiaodengyou
 * @Date: 2026/3/10 11:30
 * @Param:
 * @Return:
 * @Description: 阿里云OSS配置类
 **/
@Configuration
public class OssConfig {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 11:30
     * @Param:
     * @Return: OSS客户端实例
     * @Description: 创建OSS客户端Bean
     **/
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}