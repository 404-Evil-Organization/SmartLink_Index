package com.zhilian.zhilianbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:21
     * @Param: corsProperties CORS配置属性
     * @Param: jwtAuthenticationFilter JWT认证过滤器
     * @Return:
     * @Description: 构造方法注入所需依赖
     **/
    public SecurityConfig(CorsProperties corsProperties, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsProperties = corsProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
     * @Date: 2026/3/13 18:05
     * @Param: http HttpSecurity 对象
     * @Return: SecurityFilterChain 安全过滤器链
     * @Description: 安全配置，JWT过滤器会通过jwt-mode控制是否进行验证
     **/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 启用 CORS（使用统一的配置）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 3. 设置会话为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. 禁用表单登录和HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 5. 放行所有请求，认证逻辑在JwtAuthenticationFilter中控制
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // 6. 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}