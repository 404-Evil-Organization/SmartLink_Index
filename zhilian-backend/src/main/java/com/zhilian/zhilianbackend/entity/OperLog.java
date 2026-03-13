package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/13 21:53
 * @Param:
 * @Return:
 * @Description: 操作日志表实体类，对应oper_log表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("oper_log")
public class OperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID，关联user.id，可为NULL
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作用户名（冗余，便于查询）
     */
    @TableField("username")
    private String username;

    /**
     * 操作描述（如“用户登录”、“修改密码”）
     */
    @TableField("operation")
    private String operation;

    /**
     * 请求参数（JSON格式，可选）
     */
    @TableField("params")
    private String params;

    /**
     * 操作结果（成功/失败）
     */
    @TableField("result")
    private String result;

    /**
     * 客户端IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * 逻辑删除时间，NULL未删除，非NULL已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
