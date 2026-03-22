package com.zhilian.zhilianbackend.config;

import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.service.UserService;
import com.zhilian.zhilianbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/13 18:15
 * @Description: JWT认证过滤器，拦截请求并验证Token
 **/
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 日志记录器，用于记录JWT认证过滤过程中的异常信息
     */
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private UserService userService;  // 新增注入，使用 @Lazy 避免潜在循环依赖

    @Value("${jwt.header:Authorization}")
    private String header;

    @Value("${jwt.token-prefix:Bearer}")
    private String tokenPrefix;

    @Value("${app.jwt-mode:0}")
    private String jwtMode;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 白名单路径 - 在JWT模式下，这些路径不需要认证
     * 注意：由于项目配置了 server.servlet.context-path=/api，
     * 这里统一使用带 /api 前缀的路径以便与 request.getRequestURI() 对齐。
     */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/swagger-ui/",
            "/api/v3/api-docs/",
            "/api/v3/api-docs",
            "/api/swagger-ui.html",
            "/api/webjars/",
    };

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:16
     * @Description: 过滤器核心逻辑，根据jwt-mode配置决定是否进行JWT验证
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 判断是否为JWT模式（1-开启JWT认证，0-开发模式全放行）
            if (!isJwtMode()) {
                if (log.isDebugEnabled()) {
                    log.debug("开发模式(0) - 放行所有请求: {} {}", request.getMethod(), request.getRequestURI());
                }
                filterChain.doFilter(request, response);
                return;
            }

            // JWT模式：进行JWT验证
            String requestURI = request.getRequestURI();

            // 检查是否为白名单路径
            if (isWhiteListed(requestURI)) {
                if (log.isDebugEnabled()) {
                    log.debug("JWT模式(1) - 白名单路径放行: {} {}", request.getMethod(), requestURI);
                }
                filterChain.doFilter(request, response);
                return;
            }

            // 获取token
            String token = getTokenFromRequest(request);

            // 验证token
            if (StringUtils.hasText(token)) {
                try {
                    // 解析token获取Claims（包含角色信息）
                    Claims claims = jwtUtil.parseToken(token);
                    Long userId;
                    try {
                        // 从 JWT subject 中解析用户ID，若 subject 非数字将抛出 NumberFormatException
                        userId = Long.parseLong(claims.getSubject());
                    } catch (NumberFormatException ex) {
                        // 将 subject 非法格式转换为 JwtException，统一按 Token 非法处理为 401
                        throw new JwtException("Token subject 非法，无法解析为用户ID", ex);
                    }
                    String username = claims.get(JwtUtil.CLAIM_USERNAME, String.class);
                    String role = claims.get(JwtUtil.CLAIM_ROLE, String.class);

                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                        // ========== 新增：校验用户状态 ==========
                        // 查询用户信息，确保用户存在且状态为正常（status=1）
                        try {
                            UserInfoResponse userInfo = userService.getCurrentUser(userId);
                            if (userInfo == null || userInfo.getStatus() != 1) {
                                log.warn("用户已被禁用或不存在，userId: {}, 请求: {} {}", userId, request.getMethod(), requestURI);
                                sendUnauthorizedResponse(response, "账号已被禁用");
                                return;
                            }
                        } catch (Exception e) {
                            log.error("查询用户状态失败，userId: {}", userId, e);
                            sendUnauthorizedResponse(response, "用户状态校验失败");
                            return;
                        }
                        // =====================================

                        // 构建角色（Spring Security 需要 ROLE_ 前缀）
                        String springRole = role != null ? "ROLE_" + role.toUpperCase() : "ROLE_USER";
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(springRole);

                        // 创建 UserDetails
                        UserDetails userDetails = User.builder()
                                .username(String.valueOf(userId))
                                .password("")
                                .authorities(Collections.singletonList(authority))
                                .build();

                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 设置到SecurityContext中
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (log.isDebugEnabled()) {
                            log.debug("JWT模式(1) - Token验证成功, userId: {}, role: {}, 请求: {} {}",
                                    userId, springRole, request.getMethod(), requestURI);
                        }
                    }
                    filterChain.doFilter(request, response);
                } catch (JwtException e) {
                    // token无效，返回401
                    log.warn("JWT模式(1) - Token验证失败: {}, 请求: {} {}",
                            e.getMessage(), request.getMethod(), requestURI);
                    sendUnauthorizedResponse(response, "Token无效或已过期");
                }
            } else {
                // 没有token，返回401
                log.warn("JWT模式(1) - 缺少Token, 请求: {} {}", request.getMethod(), requestURI);
                sendUnauthorizedResponse(response, "缺少Token，请先登录");
            }
        } catch (Exception e) {
            // 统一捕获过滤器中未处理的异常
            log.error("JWT认证过滤器执行异常", e);
            if (!response.isCommitted()) {
                response.setContentType("application/json;charset=UTF-8");
                if (e instanceof JwtException) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期，请重新登录\",\"data\":null}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误，请稍后重试\",\"data\":null}");
                }
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:17
     * @Return: boolean 是否为JWT模式
     * @Description: 判断是否开启JWT认证模式
     **/
    private boolean isJwtMode() {
        return "1".equals(jwtMode);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:17
     * @Param: requestURI 请求路径
     * @Return: boolean 是否为白名单路径
     * @Description: 判断请求路径是否在白名单中
     **/
    private boolean isWhiteListed(String requestURI) {
        if (requestURI == null) {
            return false;
        }
        for (String whitePath : WHITE_LIST) {
            if (whitePath.endsWith("/**")) {
                String prefix = whitePath.substring(0, whitePath.length() - 3);
                if (requestURI.startsWith(prefix)) {
                    return true;
                }
            } else if (requestURI.equals(whitePath)) {
                return true;
            } else if (whitePath.contains("{id}")) {
                // 处理路径参数，如 /tag/{id}
                String pattern = whitePath.replace("{id}", "[^/]+");
                if (requestURI.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:02
     * @Param: request HTTP请求
     * @Return: String JWT token字符串
     * @Description: 从请求中获取token
     **/
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix + " ")) {
            return bearerToken.substring(tokenPrefix.length() + 1);
        }
        return null;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:03
     * @Param: response HTTP响应
     * @Param: message 错误信息
     * @Description: 发送401未授权响应
     **/
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String jsonResponse = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}