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
 * @Description: 制造企业表实体类，对应manufacture表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("manufacture")
public class Manufacture implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联user.id，不可为空，确保企业有归属
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 企业全称
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 所在区域（深圳/东莞/惠州/广州等）
     */
    @TableField("region")
    private String region;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系人电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 规模：微型、小型、中型、大型
     */
    @TableField("scale")
    private String scale;

    /**
     * 员工人数
     */
    @TableField("employee_count")
    private Integer employeeCount;

    /**
     * 年营收（万元）
     */
    @TableField("annual_revenue")
    private BigDecimal annualRevenue;

    /**
     * 主营产品类型（如PCB、半导体、消费电子）
     */
    @TableField("product_type")
    private String productType;

    /**
     * 企业简介
     */
    @TableField("description")
    private String description;

    /**
     * Logo图片URL
     */
    @TableField("logo")
    private String logo;

    /**
     * 成立日期
     */
    @TableField("established_date")
    private Date establishedDate;

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
     * 逻辑删除时间，''1970-01-01 00:00:00'' 表示未删除，其他时间表示已删除
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
