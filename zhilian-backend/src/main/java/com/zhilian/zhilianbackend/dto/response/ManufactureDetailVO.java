package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 22:00
 * @Param:
 * @Return:
 * @Description: 制造企业详情返回对象
 **/
@Getter
@Setter
@ToString
@Schema(description = "制造企业详情返回对象")
public class ManufactureDetailVO {

    @Schema(description = "企业ID")
    private Long id;

    @Schema(description = "关联的用户ID")
    private Long userId;

    @Schema(description = "企业全称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "规模：micro/small/medium/large")
    private String scale;

    @Schema(description = "员工人数")
    private Integer employeeCount;

    @Schema(description = "年营收（万元）")
    private BigDecimal annualRevenue;

    @Schema(description = "主营产品类型")
    private String productType;

    @Schema(description = "企业简介")
    private String description;

    @Schema(description = "Logo图片URL")
    private String logo;

    @Schema(description = "成立日期")
    private Date establishedDate;

    @Schema(description = "审核状态：pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private Date auditTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}