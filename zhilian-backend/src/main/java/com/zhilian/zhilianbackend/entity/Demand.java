package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:44
 * @Param: 
 * @Return: 
 * @Description: 需求表实体类，对应demand表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("demand")
public class Demand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 需求唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联manufacture.id
     */
    @TableField("manu_id")
    private Long manuId;

    /**
     * 需求标题
     */
    @TableField("title")
    private String title;

    /**
     * 详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 预算金额（万元）
     */
    @TableField("expected_budget")
    private BigDecimal expectedBudget;

    /**
     * 期望完成日期
     */
    @TableField("deadline")
    private Date deadline;

    /**
     * 状态：草稿、已发布、已匹配、已关闭
     */
    @TableField("status")
    private String status;

    /**
     * 浏览次数
     */
    @TableField("views")
    private Integer views;

    /**
     * 审核状态：待审核、通过、驳回
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 审核意见（驳回时填写）
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private Date auditTime;

    /**
     * 审核人ID，关联user.id
     */
    @TableField("audit_user_id")
    private Long auditUserId;

    /**
     * 逻辑删除时间，NULL未删除，非NULL已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 发布时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
