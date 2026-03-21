package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.request.UserChangePasswordRequest;
import com.zhilian.zhilianbackend.dto.request.UserListRequest;
import com.zhilian.zhilianbackend.dto.request.UserLoginRequest;
import com.zhilian.zhilianbackend.dto.request.UserRegisterRequest;
import com.zhilian.zhilianbackend.dto.response.UserDetailVO;
import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.dto.response.UserListVO;
import com.zhilian.zhilianbackend.dto.response.UserLoginResponse;
import com.zhilian.zhilianbackend.dto.response.UserRegisterResponse;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:49
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
     * @Param: userId 用户ID
     * @Param: request 修改密码请求参数（旧密码、新密码）
     * @Return: void
     * @Description: 修改用户密码
     **/
    void changePassword(Long userId, UserChangePasswordRequest request);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: request 用户列表查询参数（分页、筛选）
     * @Return: Page<UserListVO> 分页用户列表
     * @Description: 管理员 - 分页查询用户列表
     */
    Page<UserListVO> pageUsers(UserListRequest request);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Return: UserDetailVO 用户详情（含企业信息）
     * @Description: 管理员 - 获取用户详情（含企业信息）
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Param: status 状态值（0禁用 1正常）
     * @Return: void
     * @Description: 管理员 - 修改用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Return: String 新生成的临时密码
     * @Description: 管理员 - 重置用户密码（生成临时密码并返回）
     */
    String resetUserPassword(Long userId);
}