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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/10 23:55
 * @Param:
 * @Return:
 * @Description: 用户认证控制器，提供注册、登录、获取用户信息、修改密码等接口
 **/
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:29
     * @Param: request 用户注册请求参数（用户名、密码、角色、手机号、邮箱）
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
     * @Param: request 用户登录请求参数（用户名、密码）
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
     * @Param: request HttpServletRequest，用于从请求头中获取token
     * @Return: Result<UserInfoResponse> 返回当前登录用户的详细信息
     * @Description: 获取当前登录用户信息接口（需要token认证）
    **/
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:30
     * @Param: request HttpServletRequest，用于从请求头中获取token;changeRequest 修改密码请求参数（旧密码、新密码）
     * @Return: Result<Void> 修改成功返回空数据
     * @Description: 修改密码接口（需要token认证）
    **/
    @PostMapping("/change-password")
    public Result<Void> changePassword(HttpServletRequest request,
                                       @Valid @RequestBody UserChangePasswordRequest changeRequest) {
        String token = extractToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.changePassword(userId, changeRequest);
        return Result.success();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 15:31
     * @Param: request HttpServletRequest对象
     * @Return: String 提取出的JWT token字符串
     * @Description: 从请求头的Authorization字段中提取Bearer Token
    **/
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 优先判断是否缺少 Authorization 头或值为空
        if (bearerToken == null || bearerToken.isBlank()) {
            // 401 未授权：客户端未提供任何认证信息
            throw new BusinessException(401, "缺少Authorization请求头");
        }
        // 存在 Authorization 头，但格式不符合 Bearer Token 规范
        if (!bearerToken.startsWith("Bearer ")) {
            // 401 未授权：认证信息格式错误
            throw new BusinessException(401, "Authorization格式错误，缺少Bearer前缀");
        }
        String token = bearerToken.substring(7);
        // 进一步校验 Bearer 后的 token 内容是否为空，避免空串 token
        if (token.isBlank()) {
            throw new BusinessException(401, "Token内容为空");
        }
        return token;
    }
}
