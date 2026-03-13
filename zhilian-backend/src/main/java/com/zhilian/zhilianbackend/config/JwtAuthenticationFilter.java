package com.zhilian.zhilianbackend.config;

import com.zhilian.zhilianbackend.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/13 18:15
 * @Param:
 * @Return:
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
     */
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/register",
            "/test/public",
            "/test/status",
            "/test/info",
            "/test/generate-token",
            "/swagger-ui/**",      // Swagger UI相关资源
            "/v3/api-docs/**",     // OpenAPI文档
            "/swagger-ui.html",    // Swagger首页
            "/webjars/**"          // Swagger依赖的静态资源
    };

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 18:16
     * @Param: request HTTP请求
     * @Param: response HTTP响应
     * @Param: filterChain 过滤器链
     * @Return:
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
                    // 解析token获取用户ID
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 设置到SecurityContext中
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (log.isDebugEnabled()) {
                            log.debug("JWT模式(1) - Token验证成功, userId: {}, 请求: {} {}",
                                    userId, request.getMethod(), requestURI);
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
     * @Param:
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
            if (requestURI.contains(whitePath)) {
                return true;
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
     * @Return:
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