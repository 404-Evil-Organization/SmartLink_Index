package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.UserChangePasswordRequest;
import com.zhilian.zhilianbackend.dto.request.UserLoginRequest;
import com.zhilian.zhilianbackend.dto.request.UserRegisterRequest;
import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.dto.response.UserLoginResponse;
import com.zhilian.zhilianbackend.dto.response.UserRegisterResponse;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
/**
 * @Author: 6017
 * @Date: 2026/3/10 23:55
 * @Description: 用户认证控制器，提供注册、登录、获取用户信息、修改密码等接口
 **/
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 从 SecurityContext 获取当前用户ID；当 SecurityContext 为空（例如开发模式 jwt-mode=0 未填充）
     * 时，兜底从 Authorization 头中的 JWT 中解析用户ID。
     */
    private Long getCurrentUserId() {
        // 1. 优先从 SecurityContext 中获取认证信息（生产环境主路径）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return Long.parseLong(authentication.getName());
        }

        // 2. 兜底逻辑：当 SecurityContext 为空或为匿名用户时，从 Authorization 头解析 JWT 获取用户ID
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    String authorizationHeader = request.getHeader("Authorization");
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        String token = authorizationHeader.substring(7);
                        // 简单解析 JWT，不进行签名校验：按 header.payload.signature 分段，解码 payload
                        String[] parts = token.split("\\.");
                        if (parts.length >= 2) {
                            String payload = parts[1];
                            String payloadJson = new String(
                                    Base64.getUrlDecoder().decode(payload),
                                    StandardCharsets.UTF_8
                            );
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = objectMapper.readTree(payloadJson);
                            JsonNode subNode = rootNode.get("sub");
                            if (subNode != null && !subNode.isNull()) {
                                if (subNode.isNumber()) {
                                    return subNode.longValue();
                                } else {
                                    return Long.parseLong(subNode.asText());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败仅记录告警日志，不暴露具体异常给前端，最终仍返回统一的未登录错误
            log.warn("从 Authorization 头解析 JWT 获取当前用户ID失败，将返回未登录错误", e);
        }

        // 3. 两种方式都无法获取用户ID，抛出未登录业务异常
        throw new BusinessException(401, "请先登录");
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:29
     * @Param: request 用户注册请求参数
     * @Return: Result<UserRegisterResponse> 注册成功返回用户ID、用户名、角色
     * @Description: 用户注册接口
     **/
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:29
     * @Param: request 用户登录请求参数
     * @Return: Result<UserLoginResponse> 登录成功返回JWT token
     * @Description: 用户登录接口
     **/
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:30
     * @Return: Result<UserInfoResponse> 返回当前登录用户的详细信息
     * @Description: 获取当前登录用户信息接口（需要token认证）
     **/
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        Long userId = getCurrentUserId();
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:30
     * @Param: changeRequest 修改密码请求参数（旧密码、新密码）
     * @Return: Result<Void> 修改成功返回空数据
     * @Description: 修改密码接口（需要token认证）
     **/
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest changeRequest) {
        Long userId = getCurrentUserId();
        userService.changePassword(userId, changeRequest);
        return Result.success();
    }
}