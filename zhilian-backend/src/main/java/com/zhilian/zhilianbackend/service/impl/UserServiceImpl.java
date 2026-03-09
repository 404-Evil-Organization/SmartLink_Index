package com.zhilian.zhilianbackend.service.impl;

import com.zhilian.zhilianbackend.entity.User;
import com.zhilian.zhilianbackend.mapper.UserMapper;
import com.zhilian.zhilianbackend.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:50
 * @Param: 
 * @Return: 
 * @Description: 用户表业务逻辑实现类，实现用户相关的业务方法
**/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
