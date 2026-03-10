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
 * @Date: 2026/3/9 21:45
 * @Param: 
 * @Return: 
 * @Description: 合作记录表实体类，对应cooperation表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("cooperation")
public class Cooperation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 合作唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联manufacture.id
     */
    @TableField("manu_id")
    private Long manuId;

    /**
     * 关联service_provider.id
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 关联demand.id（可选）
     */
    @TableField("demand_id")
    private Long demandId;

    /**
     * 合作开始日期
     */
    @TableField("start_date")
    private Date startDate;

    /**
     * 合作结束日期
     */
    @TableField("end_date")
    private Date endDate;

    /**
     * 合同金额（万元）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 合作内容简述
     */
    @TableField("description")
    private String description;

    /**
     * 合作状态
     */
    @TableField("status")
    private String status;

    /**
     * 逻辑删除标记，NULL代表未删除，非NULL代表删除时间
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
