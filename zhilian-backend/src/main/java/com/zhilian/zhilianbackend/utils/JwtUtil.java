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
import java.util.Objects;
import java.util.function.Function;

/**
 * JWT（JSON Web Token）工具类
 * 提供JWT令牌的生成、解析和验证功能
 * 基于HS256签名算法，支持自定义claims和令牌刷新
 */
@Component
public class JwtUtil implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * HS256算法要求的最小密钥长度（32字节/256位）
     */
    private static final int MIN_SECRET_LENGTH = 32;

    /**
     * 默认最大过期时间：365天（毫秒）
     */
    private static final long MAX_EXPIRATION_MS = 365L * 24 * 60 * 60 * 1000;

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * JWT令牌前缀（如Bearer）
     */
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    /**
     * 签名密钥对象（缓存以提高性能）
     */
    private SecretKey signingKey;

    /**
     * 初始化方法，在依赖注入完成后自动执行
     * 校验密钥和过期时间配置，确保配置正确性
     */
    @Override
    public void afterPropertiesSet() {
        validateSecret();
        validateExpiration();
        this.signingKey = generateSigningKey();
        logger.info("JwtUtil初始化成功，密钥长度：{}字节，过期时间：{}毫秒",
                secret.getBytes(StandardCharsets.UTF_8).length, expiration);
    }

    /**
     * 校验密钥配置
     * 确保密钥不为空且长度符合HS256算法要求
     */
    private void validateSecret() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "JWT密钥不能为空，请在配置文件中设置'jwt.secret'属性"
            );
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    String.format("JWT密钥长度不足，当前长度：%d字节，最小要求：%d字节（HS256算法），请使用更长的密钥",
                            keyBytes.length, MIN_SECRET_LENGTH)
            );
        }
    }

    /**
     * 校验过期时间配置
     * 确保过期时间为正数，并检查是否超过最大建议值
     */
    private void validateExpiration() {
        if (expiration == null) {
            throw new IllegalStateException(
                    "JWT过期时间不能为空，请在配置文件中设置'jwt.expiration'属性"
            );
        }

        if (expiration <= 0) {
            throw new IllegalStateException(
                    String.format("JWT过期时间必须为正数，当前值：%d毫秒", expiration)
            );
        }

        if (expiration > MAX_EXPIRATION_MS) {
            logger.warn("JWT过期时间过大：{}毫秒（建议最大值：{}毫秒），请确认配置是否正确",
                    expiration, MAX_EXPIRATION_MS);
        }
    }

    /**
     * 生成签名密钥
     * @return 用于JWT签名的SecretKey对象
     */
    private SecretKey generateSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取签名密钥
     * @return 缓存的签名密钥，如果为null则重新生成
     */
    private SecretKey getSigningKey() {
        if (signingKey == null) {
            signingKey = generateSigningKey();
        }
        return signingKey;
    }

    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名，解析失败返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            logger.debug("从令牌中获取用户名失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中获取过期时间
     * @param token JWT令牌
     * @return 过期时间，解析失败返回null
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.debug("从令牌中获取过期时间失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中获取指定的claim
     * @param token JWT令牌
     * @param claimsResolver claim解析函数
     * @param <T> 返回类型
     * @return claim值，解析失败返回null
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    /**
     * 从JWT令牌中获取所有claims
     * @param token JWT令牌
     * @return Claims对象，解析失败返回null
     */
    private Claims getAllClaimsFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            logger.debug("JWT令牌为空");
            return null;
        }

        String processedToken = token;
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
            logger.debug("解析JWT令牌失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查JWT令牌是否过期
     * @param token JWT令牌
     * @return true：已过期或无法解析过期时间；false：未过期
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            logger.debug("JWT令牌缺少过期时间（exp）声明，视为无效令牌");
            return true;
        }
        return expiration.before(new Date());
    }

    /**
     * 生成JWT令牌
     * @param username 用户名（作为subject）
     * @return JWT令牌
     */
    public String generateToken(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 生成带有额外信息的JWT令牌
     * @param username 用户名（作为subject）
     * @param claims 额外的声明信息
     * @return JWT令牌
     */
    public String generateToken(String username, Map<String, Object> claims) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        Map<String, Object> safeClaims = claims != null ? claims : Collections.emptyMap();
        return createToken(safeClaims, username);
    }

    /**
     * 创建JWT令牌
     * @param claims 声明信息
     * @param subject 主题（通常为用户名）
     * @return JWT令牌
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
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @param username 预期的用户名
     * @return true：令牌有效且用户名匹配；false：无效
     */
    public Boolean validateToken(String token, String username) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            logger.debug("令牌或用户名为空，验证失败");
            return false;
        }

        try {
            String tokenUsername = getUsernameFromToken(token);
            if (tokenUsername == null) {
                logger.debug("无法从令牌中提取用户名");
                return false;
            }

            boolean isNotExpired = !isTokenExpired(token);
            boolean usernameMatches = Objects.equals(tokenUsername, username);

            return usernameMatches && isNotExpired;
        } catch (Exception e) {
            logger.debug("令牌验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证JWT令牌是否有效（仅验证签名和过期时间）
     * @param token JWT令牌
     * @return true：令牌有效；false：无效
     */
    public Boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                logger.debug("无法解析令牌claims");
                return false;
            }

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                logger.debug("令牌缺少过期时间声明");
                return false;
            }

            return !expiration.before(new Date());
        } catch (Exception e) {
            logger.debug("令牌验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新JWT令牌
     * @param token 原JWT令牌
     * @return 新的JWT令牌
     * @throws JwtException 令牌无效或刷新失败时抛出
     */
    public String refreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("令牌不能为空");
        }

        try {
            final Claims claims = getAllClaimsFromToken(token);
            if (claims == null) {
                throw new JwtException("无效的令牌：无法提取claims");
            }
            return createToken(claims, claims.getSubject());
        } catch (Exception e) {
            logger.error("刷新令牌失败：{}", e.getMessage());
            throw new JwtException("刷新令牌失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取令牌剩余过期时间
     * @param token JWT令牌
     * @return 剩余毫秒数，正数表示还有效，负数表示已过期，无法解析返回null
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
            logger.debug("获取剩余过期时间失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断令牌是否即将过期
     * @param token JWT令牌
     * @param thresholdMillis 阈值毫秒数
     * @return true：剩余时间小于阈值或无法获取剩余时间；false：剩余时间大于等于阈值
     */
    public Boolean isTokenExpiringSoon(String token, long thresholdMillis) {
        Long remaining = getRemainingExpiration(token);
        return remaining != null && remaining < thresholdMillis;
    }
}