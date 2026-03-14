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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用于用户名的锁缓存：使用带引用计数的锁对象，避免并发场景下错误移除导致互斥失效
    private final ConcurrentHashMap<String, UsernameLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 用户名级别锁对象：
     * - mutex 作为 synchronized 的真正锁
     * - refCount 记录当前持有/等待该锁的线程数量，用于安全地从缓存中移除锁
     */
    private static class UsernameLock {
        /**
         * 实际用于 synchronized 的锁对象
         */
        final Object mutex = new Object();

        /**
         * 引用计数，表示有多少线程正在使用该锁（包含持有和等待）
         */
        final AtomicInteger refCount = new AtomicInteger(0);
    }

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
        // 使用 compute + 引用计数，保证同一个 username 始终复用同一把锁对象
        UsernameLock usernameLock = lockMap.compute(username, (k, existing) -> {
            if (existing == null) {
                existing = new UsernameLock();
            }
            // 当前线程开始使用该锁（包括后续 synchronized 阻塞等待的场景）
            existing.refCount.incrementAndGet();
            return existing;
        });
        Object lock = usernameLock.mutex;

        try {
            synchronized (lock) {
                // 3. 双检锁：再次检查用户名是否已存在
                LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(User::getUsername, username)
                        .isNull(User::getDeleted);
                if (userMapper.selectCount(checkWrapper) > 0) {
                    throw new BusinessException(409, "用户名已存在");
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
                try {
                    userMapper.insert(user);
                } catch (DuplicateKeyException e) {
                    // 并发场景下数据库唯一键约束兜底：转换为 409 业务异常，而非 500
                    throw new BusinessException(409, "用户名已存在");
                }

                // 6. 返回响应
                UserRegisterResponse response = new UserRegisterResponse();
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setRole(user.getRole());
                return response;
            }
        } finally {
            // 7. 安全释放锁：仅当引用计数归零时才从缓存中移除，避免并发场景下互斥失效
            lockMap.computeIfPresent(username, (k, existing) -> {
                int newCount = existing.refCount.decrementAndGet();
                return newCount == 0 ? null : existing;
            });
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
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
                .isNull(User::getDeleted);
        List<User> users = userMapper.selectList(wrapper);

        // 2. 用户不存在
        if (users.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 3. 处理多条记录的情况（数据异常）
        User user;
        if (users.size() > 1) {
            log.error("系统数据异常：用户名【{}】存在多条有效记录", request.getUsername());
            // 按创建时间倒序取最新的用户
            users.sort((u1, u2) -> u2.getCreateTime().compareTo(u1.getCreateTime()));
            user = users.get(0);
            log.warn("使用最新创建的用户记录: userId={}, createTime={}",
                    user.getId(), user.getCreateTime());
        } else {
            user = users.get(0);
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 5. 验证账号状态
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 6. 生成token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

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
     * @Param: userId 用户ID
     * @Param: request 修改密码请求参数
     * @Return: void
     * @Description: 修改密码业务实现
     **/
    @Override
    public void changePassword(Long userId, UserChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() != null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(401, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }
}