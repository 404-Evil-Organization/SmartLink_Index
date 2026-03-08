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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 用于生成和解析JWT令牌
 *
 * 优化点：
 * 1. 启动时校验密钥长度，实现fail-fast
 * 2. 增强空值校验，避免NPE
 * 3. 优化token验证逻辑，更健壮
 * 4. 统一异常处理，提供清晰错误信息
 */
@Component
public class JwtUtil implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // HS256要求密钥至少32字节（256位）
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    private SecretKey signingKey;

    /**
     * 初始化方法，在依赖注入完成后自动执行
     * 校验密钥配置，实现fail-fast
     */
    @Override
    public void afterPropertiesSet() {
        validateSecret();
        this.signingKey = generateSigningKey();
        logger.info("JwtUtil initialized successfully with key length: {} bytes",
                secret.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * 校验密钥配置
     */
    private void validateSecret() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT secret cannot be null or empty. Please configure 'jwt.secret' in application properties."
            );
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    String.format("JWT secret length is insufficient. Current: %d bytes, Minimum required: %d bytes (HS256). " +
                            "Please use a longer secret key for security.", keyBytes.length, MIN_SECRET_LENGTH)
            );
        }
    }

    /**
     * 生成签名密钥（缓存结果提升性能）
     */
    private SecretKey generateSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        // 如果signingKey为null（理论上不会发生），重新生成
        if (signingKey == null) {
            signingKey = generateSigningKey();
        }
        return signingKey;
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            logger.debug("Failed to get username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.debug("Failed to get expiration date from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取指定claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    /**
     * 从token中获取所有claims
     * 增强的异常处理和空值校验
     */
    private Claims getAllClaimsFromToken(String token) {
        // 对token进行非空校验
        if (!StringUtils.hasText(token)) {
            logger.debug("JWT token is null or empty");
            return null;
        }

        String processedToken = token;
        // 移除Bearer前缀
        if (token.startsWith(tokenPrefix + " ")) {
            processedToken = token.substring(tokenPrefix.length() + 1);
        }

        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(processedToken)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            logger.debug("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查token是否过期
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 生成token
     */
    public String generateToken(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 生成带有额外信息的token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        // 处理claims为空的情况，归一化为空Map
        Map<String, Object> safeClaims = claims != null ? claims : Collections.emptyMap();
        return createToken(safeClaims, username);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证token是否有效
     * 优化：内部捕获异常，返回明确的boolean结果
     */
    public Boolean validateToken(String token, String username) {
        // 参数校验
        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            logger.debug("Token or username is null/empty for validation");
            return false;
        }

        try {
            String tokenUsername = getUsernameFromToken(token);
            if (tokenUsername == null) {
                logger.debug("Failed to extract username from token");
                return false;
            }

            boolean isNotExpired = !isTokenExpired(token);
            boolean usernameMatches = tokenUsername.equals(username);

            return usernameMatches && isNotExpired;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token是否有效（无需用户名）
     */
    public Boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            final Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                throw new JwtException("Invalid token: cannot extract claims");
            }
            return createToken(claims, claims.getSubject());
        } catch (Exception e) {
            logger.error("Failed to refresh token: {}", e.getMessage());
            throw new JwtException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * 获取token剩余过期时间（毫秒）
     */
    public Long getRemainingExpiration(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            final Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return null;
            }
            return expiration.getTime() - new Date().getTime();
        } catch (Exception e) {
            logger.debug("Failed to get remaining expiration: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断token是否即将过期（剩余时间小于指定阈值）
     * @param token JWT token
     * @param thresholdMillis 阈值毫秒数
     */
    public Boolean isTokenExpiringSoon(String token, long thresholdMillis) {
        Long remaining = getRemainingExpiration(token);
        return remaining != null && remaining < thresholdMillis;
    }
}