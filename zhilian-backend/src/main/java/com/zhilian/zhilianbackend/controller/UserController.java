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
import com.zhilian.zhilianbackend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    private final JwtUtil jwtUtil;

    /**
     * 从 SecurityContext 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return Long.parseLong(authentication.getName());
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