package com.zhilian.zhilianbackend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * JWT工具类 - 提供令牌的生成、解析和验证功能
 */
@Component
public class JwtUtil implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private SecretKey signingKey;

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: Spring Bean初始化完成后执行，验证配置并生成签名密钥
     **/
    @Override
    public void afterPropertiesSet() {
        validateConfig();
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        logger.info("JwtUtil初始化成功");
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 验证JWT配置的正确性，包括密钥和过期时间
     **/
    private void validateConfig() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT密钥不能为空");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("JWT密钥长度不足32字节");
        }
        if (expiration == null || expiration <= 0) {
            throw new IllegalStateException("JWT过期时间必须为正数");
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 生成JWT令牌，使用用户名作为主题
     **/
    public String generateToken(String username) {
        return generateToken(username, null);
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 生成JWT令牌，支持自定义claims
     **/
    public String generateToken(String username, Map<String, Object> claims) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        Date now = new Date();
        return Jwts.builder()
                .claims(claims != null ? claims : Map.of())
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 从JWT令牌中获取用户名
     **/
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 解析JWT令牌，返回Claims对象，处理令牌前缀和各类异常
     **/
    private Claims parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        String processedToken = token;
        if (token.startsWith(tokenPrefix + " ")) {
            processedToken = token.substring(tokenPrefix.length() + 1);
        }

        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(processedToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.debug("令牌已过期");
            return null;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.debug("令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 完整验证JWT令牌，包括签名、过期时间和用户名匹配
     **/
    public boolean validateToken(String token, String username) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            return false;
        }

        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }

        // 验证用户名和过期时间
        return Objects.equals(claims.getSubject(), username) &&
                !claims.getExpiration().before(new Date());
    }

    /**
     * @Author:xiaodengyou
     * @Date: 2026/3/9 22:01
     * @Param:
     * @Return:
     * @Description: 简化验证JWT令牌，仅验证签名和过期时间
     **/
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        Claims claims = parseToken(token);
        return claims != null && !claims.getExpiration().before(new Date());
    }
}