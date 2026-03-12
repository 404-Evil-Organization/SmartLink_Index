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

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:56
 * @Param: 
 * @Return: 
 * @Description: Spring Security 配置类，配置认证授权、CORS 跨域和路径放行规则
**/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:21
     * @Param: corsProperties CORS配置属性
     * @Return: 
     * @Description: 构造方法注入CORS配置属性
    **/
    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 20:59
     * @Param: 
     * @Return: CorsConfigurationSource CORS 配置源
     * @Description: 配置 CORS 跨域规则，允许前端域名访问
    **/
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

    /**
     * @Author: 6017
     * @Date: 2026/3/9 20:59
     * @Param: http HttpSecurity 对象
     * @Return: SecurityFilterChain 安全过滤器链
     * @Description: 开发环境安全配置，放行所有请求以便调试
    **/
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
                                "/auth/**",
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

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:02
     * @Param: http HttpSecurity 对象
     * @Return: SecurityFilterChain 安全过滤器链
     * @Description: 生产环境安全配置，除白名单外所有请求都需要认证
    **/
    @Bean
    @Profile("prod")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        http
                // 同样要启用 CORS（使用统一的配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/test/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()
                        // TODO: 引入 JWT 或其他认证机制后，将这里改回 .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}