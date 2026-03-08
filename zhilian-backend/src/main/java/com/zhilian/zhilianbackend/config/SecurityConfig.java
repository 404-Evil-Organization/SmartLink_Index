package com.zhilian.zhilianbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 白名单路径：不需要认证就能访问
    private static final String[] PUBLIC_URLS = {
            "/test/**",           // 测试接口
            "/swagger-ui/**",      // Swagger 文档
            "/swagger-ui.html",    // Swagger 首页
            "/v3/api-docs/**",     // OpenAPI 文档
            "/auth/login",         // 登录接口（Day 4 实现）
            "/auth/register",      // 注册接口（Day 4 实现）
            "/**.html",            // 静态资源
            "/**.css",
            "/**.js",
            "/images/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 前后端分离项目通常禁用
                .authorizeHttpRequests(auth -> auth
                        // 白名单放行
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}