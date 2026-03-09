package com.zhilian.zhilianbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:11
 * @Param: 
 * @Return: 
 * @Description: Swagger 文档配置类，配置 API 文档信息和界面
**/
@Configuration
public class SwaggerConfig {

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:16
     * @Param: 
     * @Return: OpenAPI OpenAPI 配置对象
     * @Description: 配置 OpenAPI 文档信息，包括标题、描述、版本、联系人等
    **/
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智链指数 API 文档")
                        .description("大湾区电子信息产业两业融合智能评估与协同服务平台")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("智链团队")
                                .email("contact@zhilian.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}