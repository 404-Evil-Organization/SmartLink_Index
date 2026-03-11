package com.zhilian.zhilianbackend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:xiaodengyou
 * @Date: 2026/3/10 10:30
 * @Param:
 * @Return:
 * @Description: JWT工具类，提供Token的生成、解析和验证功能
 **/
@Component
public class JwtUtil {

    private String secret;
    private Long expiration;

    // 无参构造器，设置默认值（使用32字节以上的密钥）
    public JwtUtil() {
        this.secret = "zhilianSecretKeyForJWT20260310With32Bytes!"; // 36字节 > 256位
        this.expiration = 86400000L; // 24小时
    }

    // 带参构造器，方便测试时注入
    public JwtUtil(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    @Value("${jwt.secret:zhilianSecretKeyForJWT20260310With32Bytes!}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Value("${jwt.expiration:86400000}")
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 10:30
     * @Param: subject 主题（通常为用户名或用户ID）
     * @Param: claims 自定义声明信息
     * @Return: String JWT token字符串
     * @Description: 生成JWT Token
     **/
    public String generateToken(String subject, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 10:30
     * @Param: token JWT token字符串
     * @Return: Claims Token中包含的声明信息
     * @Description: 解析JWT Token，返回Claims对象
     **/
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 10:30
     * @Param: token JWT token字符串
     * @Return: boolean true-有效 false-无效
     * @Description: 验证JWT Token的有效性
     **/
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/10 10:31
     * @Param:
     * @Return: SecretKey 签名密钥
     * @Description: 获取JWT签名密钥
     **/
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:18
     * @Param: userId 用户ID
     * @Return: String JWT token字符串
     * @Description: 生成Token（只传用户ID）
    **/
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(String.valueOf(userId), claims);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:19
     * @Param: userId 用户ID,username 用户名
     * @Return: String JWT token字符串
     * @Description: 生成Token（传用户ID和用户名，用户名会存入claims中）
    **/
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return generateToken(String.valueOf(userId), claims);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:21
     * @Param: token JWT token字符串
     * @Return: Long 用户ID
     * @Description: 从Token中解析并获取用户ID
     * <p>
     * 注意：当 token 过期、签名不合法或 subject 非数字时，会抛出业务异常 InvalidTokenException，
     * 由全局异常处理器转换为 401 等认证失败响应，避免将认证失败误判为 500 系统异常。
     **/
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException | NumberFormatException e) {
            // 将底层解析异常统一包装为业务异常，供上层识别为认证失败
            throw new InvalidTokenException("Token无效或已过期", e);
        }
    }

    /**
     * 业务层 Token 无效异常。
     * <p>
     * 用于将 JWT 底层异常（过期、签名不合法、格式错误等）封装为统一的业务异常，
     * 方便全局异常处理器将其映射为 401 等认证失败响应，而不是 500 系统异常。
     */
    public static class InvalidTokenException extends RuntimeException {

        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}