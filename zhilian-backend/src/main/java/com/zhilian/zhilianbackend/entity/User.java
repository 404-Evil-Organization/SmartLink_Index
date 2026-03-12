package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:43
 * @Param:
 * @Return:
 * @Description: 用户表实体类，对应数据库user表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    @TableField("username")
    private String username;

    /**
     * 加密存储
     */
    @TableField("password")
    private String password;

    /**
     * 角色：制造企业、服务商、园区/政府、管理员
     */
    @TableField("role")
    private String role;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 状态：0禁用 1正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 逻辑删除标记，NULL代表未删除，非NULL代表删除时间
     */
    @TableField("deleted")
    @TableLogic(value = "null", delval = "now()")
    private Date deleted;

    /**
     * 注册时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
