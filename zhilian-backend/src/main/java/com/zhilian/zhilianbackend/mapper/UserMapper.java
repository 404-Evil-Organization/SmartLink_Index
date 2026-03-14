package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:49
 * @Param: 
 * @Return: 
 * @Description: 用户表Mapper接口，提供用户相关的数据库操作
**/
public interface UserMapper extends BaseMapper<User> {

    /**
     * @Author: 
     * @Date: 2026/3/13 20:30
     * @Param: id 用户ID
     * @Return: User 用户信息
     * @Description: 根据ID查询用户并加锁（悲观锁），用于并发控制
     **/
    @Select("SELECT * FROM user WHERE id = #{id} FOR UPDATE")
    User selectByIdForUpdate(Long id);
}
