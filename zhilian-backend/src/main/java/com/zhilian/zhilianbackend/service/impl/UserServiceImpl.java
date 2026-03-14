package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.dto.request.UserChangePasswordRequest;
import com.zhilian.zhilianbackend.dto.request.UserLoginRequest;
import com.zhilian.zhilianbackend.dto.request.UserRegisterRequest;
import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.dto.response.UserLoginResponse;
import com.zhilian.zhilianbackend.dto.response.UserRegisterResponse;
import com.zhilian.zhilianbackend.entity.User;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.UserMapper;
import com.zhilian.zhilianbackend.service.UserService;
import com.zhilian.zhilianbackend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:50
 * @Description: 用户表业务逻辑实现类，实现用户相关的业务方法
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用于用户名的锁缓存
    private final ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:09
     * @Param: request 用户注册请求参数
     * @Return: UserRegisterResponse 注册响应信息
     * @Description: 用户注册业务实现（加锁处理并发注册）
     **/
    @Override
    public UserRegisterResponse register(UserRegisterRequest request) {
        // 1. 校验并设置用户角色，仅允许 manufacture/service/park
        String role = request.getRole();
        if (!"manufacture".equals(role)
                && !"service".equals(role)
                && !"park".equals(role)) {
            // 禁止通过注册接口创建管理员账号，避免任意用户自注册为 admin
            throw new BusinessException(400, "用户角色不合法");
        }

        String username = request.getUsername();

        // 2. 获取用户名锁（避免并发注册相同用户名）
        Object lock = lockMap.computeIfAbsent(username, k -> new Object());

        synchronized (lock) {
            // 3. 双检锁：再次检查用户名是否已存在（在同一用户名锁内，确保并发互斥）
            LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(User::getUsername, username)
                    .isNull(User::getDeleted);
            if (userMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException(409, "用户名已存在"); // 409 Conflict
            }

            // 4. 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setStatus(1); // 默认正常

            // 5. 保存到数据库
            userMapper.insert(user);

            // 6. 返回响应
            UserRegisterResponse response = new UserRegisterResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            return response;
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:09
     * @Param: request 用户登录请求参数
     * @Return: UserLoginResponse 登录响应信息（JWT token）
     * @Description: 用户登录业务实现
     **/
    @Override
    public UserLoginResponse login(UserLoginRequest request) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
                .isNull(User::getDeleted);
        List<User> users = userMapper.selectList(wrapper);

        // 如果未查到用户，直接返回“用户不存在”
        if (users.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 如果查到多条，说明数据库存在重复用户名记录，属于数据一致性问题，需显式报错/告警
        if (users.size() > 1) {
            log.error("系统数据异常：用户名【{}】存在多条有效记录", request.getUsername());
            // 按创建时间倒序取最新的用户
            users.sort((u1, u2) -> u2.getCreateTime().compareTo(u1.getCreateTime()));
            User latestUser = users.get(0);
            log.warn("使用最新创建的用户记录: userId={}, createTime={}",
                    latestUser.getId(), latestUser.getCreateTime());

            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), latestUser.getPassword())) {
                throw new BusinessException(401, "用户名或密码错误");
            }

            if (!Integer.valueOf(1).equals(latestUser.getStatus())) {
                throw new BusinessException(403, "账号已被禁用");
            }

            String token = jwtUtil.generateToken(latestUser.getId(), latestUser.getUsername(),latestUser.getRole());
            UserLoginResponse response = new UserLoginResponse();
            response.setToken(token);
            return response;
        }

        User user = users.get(0);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误"); // 401 Unauthorized
        }


        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用"); // 403 Forbidden
        }


        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),user.getRole());

        UserLoginResponse response = new UserLoginResponse();
        response.setToken(token);
        return response;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:10
     * @Param: userId 用户ID
     * @Return: UserInfoResponse 用户详细信息
     * @Description: 获取当前用户信息业务实现
     **/
    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        return response;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:12
     * @Param: userId 用户ID,request 修改密码请求参数
     * @Return:
     * @Description: 修改密码业务实现
     **/
    @Override
    public void changePassword(Long userId, UserChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(401, "旧密码错误"); // 401 Unauthorized
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }
}