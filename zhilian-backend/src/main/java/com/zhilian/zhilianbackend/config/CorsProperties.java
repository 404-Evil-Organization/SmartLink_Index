package com.zhilian.zhilianbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:35
 * @Param: 
 * @Return: 
 * @Description: CORS跨域配置属性类，从application.yml中读取app.cors前缀的配置
**/
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private Boolean allowCredentials = true;
    private Long maxAge = 3600L;

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:37
     * @Param: 
     * @Return: List<String> 允许的域名列表
     * @Description: 获取允许访问的域名列表
    **/
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:37
     * @Param: allowedOrigins 允许的域名列表
     * @Return: 
     * @Description: 设置允许访问的域名列表
    **/
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:37
     * @Param: 
     * @Return: List<String> 允许的HTTP方法列表
     * @Description: 获取允许的HTTP方法列表
    **/
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:37
     * @Param: allowedMethods 允许的HTTP方法列表
     * @Return: 
     * @Description: 设置允许的HTTP方法列表
    **/
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:37
     * @Param: 
     * @Return: List<String> 允许的请求头列表
     * @Description: 获取允许的请求头列表
    **/
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:38
     * @Param: allowedHeaders 允许的请求头列表
     * @Return: 
     * @Description: 设置允许的请求头列表
    **/
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:38
     * @Param: 
     * @Return: Boolean 是否允许携带凭证
     * @Description: 获取是否允许携带凭证（如Cookie）
    **/
    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:39
     * @Param: allowCredentials 是否允许携带凭证
     * @Return: 
     * @Description: 设置是否允许携带凭证（如Cookie）
    **/
    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:39
     * @Param: 
     * @Return: Long 预检请求缓存时间（秒）
     * @Description: 获取预检请求的缓存时间
    **/
    public Long getMaxAge() {
        return maxAge;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:39
     * @Param: maxAge 预检请求缓存时间（秒）
     * @Return: 
     * @Description: 设置预检请求的缓存时间
    **/
    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }
}
