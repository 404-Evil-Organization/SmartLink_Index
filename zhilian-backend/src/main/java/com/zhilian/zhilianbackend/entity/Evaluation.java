package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:46
 * @Param: 
 * @Return: 
 * @Description: 评价表实体类，对应evaluation表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("evaluation")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联cooperation.id，一次合作可多条评价（按角色区分）
     */
    @TableField("coop_id")
    private Long coopId;

    /**
     * 评价人 user.id
     */
    @TableField("evaluator_id")
    private Long evaluatorId;

    /**
     * 评价人角色（制造企业/服务商）
     */
    @TableField("evaluator_role")
    private String evaluatorRole;

    /**
     * 评分（1-5星）
     */
    @TableField("score")
    private Byte score;

    /**
     * 评价内容
     */
    @TableField("content")
    private String content;

    /**
     * 是否匿名（0否 1是）
     */
    @TableField("is_anonymous")
    private Byte isAnonymous;

    /**
     * 逻辑删除时间，''1970-01-01 00:00:00'' 表示未删除，其他时间表示已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 评价时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
