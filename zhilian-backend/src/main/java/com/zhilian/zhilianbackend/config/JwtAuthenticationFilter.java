package com.zhilian.zhilianbackend.config;

import com.zhilian.zhilianbackend.exception.BusinessException;
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
import com.zhilian.zhilianbackend.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/13 16:35
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

    @Autowired
    private AppStateConfig appStateConfig;

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 16:36
     * @Param: request HTTP请求
     * @Param: response HTTP响应
     * @Param: filterChain 过滤器链
     * @Return:
     * @Description: 过滤器核心逻辑，验证JWT token并设置认证信息
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 开发模式直接放行所有请求
            if (appStateConfig.isDevMode()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 生产模式才进行JWT验证
            String requestURI = request.getRequestURI();

            // 登录和注册接口不需要认证
            if (requestURI != null && (requestURI.contains("/auth/login") ||
                    requestURI.contains("/auth/register") ||
                    requestURI.contains("/test/public") ||
                    requestURI.contains("/test/status") ||
                    requestURI.contains("/test/info"))) {
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
                    }
                    filterChain.doFilter(request, response);
                } catch (JwtException e) {
                    // token无效，返回401，使用统一Result封装并通过ObjectMapper序列化
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    Result<?> result = Result.unauthorized("Token无效或已过期");
                    response.getWriter().write(objectMapper.writeValueAsString(result));
                    response.getWriter().flush();
                    return;
                }
            } else {
                // 没有token，返回401，使用统一Result封装并通过ObjectMapper序列化
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                Result<?> result = Result.unauthorized("缺少Token，请先登录");
                response.getWriter().write(objectMapper.writeValueAsString(result));
                response.getWriter().flush();
                return;
            }
        } catch (Exception e) {
            // 统一捕获过滤器中未处理的异常，记录错误日志并返回合适的HTTP状态码
            log.error("JWT认证过滤器执行异常", e);
            // 如果响应尚未提交，则根据异常类型构造标准错误响应
            if (!response.isCommitted()) {
                response.setContentType("application/json;charset=UTF-8");
                if (e instanceof JwtException || e instanceof BusinessException) {
                    // JWT相关异常或业务鉴权异常，返回401
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期，请重新登录\",\"data\":null}");
                } else {
                    // 其他未知异常，返回500，避免静默放行导致安全风险
                    response.setStatus(500);
                    response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误，请稍后重试\",\"data\":null}");
                }
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 16:37
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
}