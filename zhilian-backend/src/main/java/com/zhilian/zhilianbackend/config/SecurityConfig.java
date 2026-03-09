package com.zhilian.zhilianbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;

    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * 统一 CORS 配置，从 application.yml 读取
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 从配置文件中读取允许的域名
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // 从配置文件中读取允许的 HTTP 方法
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());

        // 从配置文件中读取允许的请求头
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());

        // 从配置文件中读取是否允许携带凭证
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());

        // 从配置文件中读取预检请求缓存时间
        configuration.setMaxAge(corsProperties.getMaxAge());

        // 对所有路径生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    @Profile({"dev", "default"})
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 启用 CORS（使用统一的配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 3. 授权配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/test/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().permitAll()  // 开发环境全放行
                )
                // 4. 禁用不需要的功能
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
                // 同样要启用 CORS（使用统一的配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/test/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}