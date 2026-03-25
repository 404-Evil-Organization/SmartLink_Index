package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 00:20
 * @Param:
 * @Return:
 * @Description: 服务商详情返回对象
 **/
@Getter
@Setter
@ToString
@Schema(description = "服务商详情返回对象")
public class ServiceProviderDetailVO {

    @Schema(description = "服务商ID")
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

    @Schema(description = "服务大类")
    private String serviceType;

    @Schema(description = "服务介绍")
    private String description;

    @Schema(description = "Logo图片URL")
    private String logo;

    @Schema(description = "企业官网")
    private String website;

    @Schema(description = "成立日期")
    private Date establishedDate;

    @Schema(description = "员工人数")
    private Integer employeeCount;

    @Schema(description = "资质概述")
    private String qualification;

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

    @Schema(description = "是否提供出海服务（0否 1是）")
    private Byte isAbroad;

    @Schema(description = "覆盖国家/地区，多个用逗号分隔")
    private String countryCoverage;

    @Schema(description = "资质证书列表")
    private java.util.List<CertificationVO> certifications;
}