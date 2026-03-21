package com.zhilian.zhilianbackend.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        // 在 Windows 环境变量中，通过命令行或 IDEA 配置的环境变量极易在尾部包含不可见的空格或换行符（如 \r）。
        // 比如在 IDEA 的 Run Configurations 里粘贴时多了一个空格，或者使用 echo 导出环境变量时带有尾随换行。
        // 为了防止这些不可见字符导致 OSS SDK 签名不匹配（SignatureDoesNotMatch），在这里强制执行 trim() 进行清理。
        String cleanAk = accessKeyId != null ? accessKeyId.trim() : null;
        String cleanSk = accessKeySecret != null ? accessKeySecret.trim() : null;
        String cleanEndpoint = endpoint != null ? endpoint.trim() : null;
        
        log.info("Initializing OSS Client with endpoint: {}", cleanEndpoint);
        log.info("AccessKeyId length after trim: {}", cleanAk != null ? cleanAk.length() : 0);
        log.info("AccessKeySecret length after trim: {}", cleanSk != null ? cleanSk.length() : 0);
        
        return new OSSClientBuilder().build(cleanEndpoint, cleanAk, cleanSk);
    }
}