package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.request.UserChangePasswordRequest;
import com.zhilian.zhilianbackend.dto.request.UserListRequest;
import com.zhilian.zhilianbackend.dto.request.UserLoginRequest;
import com.zhilian.zhilianbackend.dto.request.UserRegisterRequest;
import com.zhilian.zhilianbackend.dto.response.*;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.entity.User;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.UserMapper;
import com.zhilian.zhilianbackend.service.ManufactureService;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import com.zhilian.zhilianbackend.service.UserService;
import com.zhilian.zhilianbackend.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:50
 * @Description: 用户表业务逻辑实现类，实现用户相关的业务方法
 **/
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 用于用户名的锁缓存：使用带引用计数的锁对象
    private final ConcurrentHashMap<String, UsernameLock> lockMap = new ConcurrentHashMap<>();

    // 使用构造器注入 + 参数级 @Lazy，避免字段注入带来的不可变性/可测试性问题
    private final ManufactureService manufactureService;
    private final ServiceProviderService serviceProviderService;

    /**
     * 通过构造器注入所有依赖，在参数上使用 @Lazy 解决与其他 Service 的循环依赖问题。
     * 保持依赖字段为 final，提高不可变性和可测试性。
     */
    public UserServiceImpl(UserMapper userMapper,
                           JwtUtil jwtUtil,
                           @Lazy ManufactureService manufactureService,
                           @Lazy ServiceProviderService serviceProviderService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.manufactureService = manufactureService;
        this.serviceProviderService = serviceProviderService;
    }

    /**
     * 用户名级别锁对象：
     * - mutex 作为 synchronized 的真正锁
     * - refCount 记录当前持有/等待该锁的线程数量
     */
    private static class UsernameLock {
        final Object mutex = new Object();
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
        // 1. 校验角色
        String role = request.getRole();
        if (!"manufacture".equals(role) && !"service".equals(role) && !"park".equals(role)) {
            throw new BusinessException(400, "用户角色不合法");
        }

        String username = request.getUsername();

        // 2. 获取或创建锁，并增加引用计数
        UsernameLock usernameLock = lockMap.compute(username, (k, existing) -> {
            if (existing == null) {
                existing = new UsernameLock();
            }
            existing.refCount.incrementAndGet(); // 引用计数+1
            return existing;
        });

        try {
            synchronized (usernameLock.mutex) {
                // 3. 再次检查用户名是否已存在
                LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(User::getUsername, username);
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
                user.setStatus(1);

                // 5. 保存到数据库
                try {
                    userMapper.insert(user);
                } catch (DuplicateKeyException e) {
                    // 数据库唯一键约束兜底
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
            // 7. 减少引用计数，当计数为0时移除锁
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
        wrapper.eq(User::getUsername, request.getUsername());
        List<User> users = userMapper.selectList(wrapper);

        // 2. 用户不存在
        if (users.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 3. 处理多条记录
        User user;
        if (users.size() > 1) {
            log.error("系统数据异常：用户名【{}】存在多条有效记录", request.getUsername());
            users.sort((u1, u2) -> u2.getCreateTime().compareTo(u1.getCreateTime()));
            user = users.get(0);
            log.warn("使用最新创建的用户记录: userId={}, createTime={}", user.getId(), user.getCreateTime());
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
        if (user == null) {
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
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(401, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param:
     * @Return: String 随机生成的密码（8-12位，字母数字混合）
     * @Description: 生成随机临时密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        int length = 8 + random.nextInt(5); // 8~12位
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: request 用户列表查询参数
     * @Return: Page<UserListVO> 分页用户列表
     * @Description: 管理员 - 分页查询用户列表
     */
    @Override
    public Page<UserListVO> pageUsers(UserListRequest request) {
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            wrapper.eq(User::getRole, request.getRole());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, request.getKeyword())
                    .or().like(User::getPhone, request.getKeyword()));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);
        Page<UserListVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(user -> {
            UserListVO vo = new UserListVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRole(user.getRole());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Return: UserDetailVO 用户详情（含企业信息）
     * @Description: 管理员 - 获取用户详情（含企业信息）
     */
    @Override
    public UserDetailVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserDetailVO detail = new UserDetailVO();
        detail.setId(user.getId());
        detail.setUsername(user.getUsername());
        detail.setRole(user.getRole());
        detail.setPhone(user.getPhone());
        detail.setEmail(user.getEmail());
        detail.setStatus(user.getStatus());
        detail.setCreateTime(user.getCreateTime());

        if ("manufacture".equals(user.getRole())) {
            // 使用 getOne 并传入 false 避免多条记录时抛出异常，多条时返回第一条
            Manufacture manufacture = manufactureService.getOne(
                    new LambdaQueryWrapper<Manufacture>().eq(Manufacture::getUserId, user.getId()),
                    false
            );
            if (manufacture != null) {
                UserDetailVO.ManufactureInfo info = new UserDetailVO.ManufactureInfo();
                info.setId(manufacture.getId());
                info.setCompanyName(manufacture.getCompanyName());
                info.setRegion(manufacture.getRegion());
                info.setScale(manufacture.getScale());
                detail.setManufactureInfo(info);
            }
        } else if ("service".equals(user.getRole())) {
            // 使用 getOne 并传入 false 避免多条记录时抛出异常，多条时返回第一条
            ServiceProvider service = serviceProviderService.getOne(
                    new LambdaQueryWrapper<ServiceProvider>().eq(ServiceProvider::getUserId, user.getId()),
                    false
            );
            if (service != null) {
                UserDetailVO.ServiceProviderInfo info = new UserDetailVO.ServiceProviderInfo();
                info.setId(service.getId());
                info.setCompanyName(service.getCompanyName());
                info.setRegion(service.getRegion());
                info.setServiceType(service.getServiceType());
                detail.setServiceProviderInfo(info);
            }
        }
        return detail;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Param: status 状态值（0禁用 1正常）
     * @Return: void
     * @Description: 管理员 - 修改用户状态（启用/禁用）
     */
    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 可选：防止管理员禁用自己
        // if (userId.equals(getCurrentUserId())) {
        //     throw new BusinessException(400, "不能禁用当前登录的管理员账号");
        // }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: userId 用户ID
     * @Return: String 新生成的临时密码
     * @Description: 管理员 - 重置用户密码（生成随机临时密码）
     */
    @Override
    @Transactional
    public String resetUserPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return newPassword;
    }
}