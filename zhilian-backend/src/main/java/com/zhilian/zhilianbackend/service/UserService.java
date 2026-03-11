package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.dto.request.UserChangePasswordRequest;
import com.zhilian.zhilianbackend.dto.request.UserLoginRequest;
import com.zhilian.zhilianbackend.dto.request.UserRegisterRequest;
import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.dto.response.UserLoginResponse;
import com.zhilian.zhilianbackend.dto.response.UserRegisterResponse;
import com.zhilian.zhilianbackend.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:49
 * @Param: 
 * @Return: 
 * @Description: 用户表业务逻辑接口，定义用户相关的业务方法
**/
public interface UserService {
    
    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:05
     * @Param: request 用户注册请求参数
     * @Return: UserRegisterResponse 注册响应信息（用户ID、用户名、角色）
     * @Description: 用户注册
    **/
    UserRegisterResponse register(UserRegisterRequest request);
    
    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:06
     * @Param: request 用户登录请求参数
     * @Return: UserLoginResponse 登录响应信息（JWT token）
     * @Description: 用户登录
    **/
    UserLoginResponse login(UserLoginRequest request);
    
    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:06
     * @Param: userId 用户ID
     * @Return: UserInfoResponse 用户详细信息
     * @Description: 根据用户ID获取当前用户信息
    **/
    UserInfoResponse getCurrentUser(Long userId);

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:07
     * @Param: userId 用户ID,request 修改密码请求参数（旧密码、新密码）
     * @Return: 
     * @Description: 修改用户密码
    **/
    void changePassword(Long userId, UserChangePasswordRequest request);
}
