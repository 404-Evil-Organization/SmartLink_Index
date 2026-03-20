package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:51
 * @Param:
 * @Return:
 * @Description: 服务商列表返回对象
 **/
@Getter
@Setter
@ToString
@Schema(description = "服务商列表返回对象")
public class ServiceProviderListVO {

    @Schema(description = "服务商ID")
    private Long id;

    @Schema(description = "企业全称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "服务大类")
    private String serviceType;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "资质概述")
    private String qualification;

    @Schema(description = "审核状态：pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "Logo图片URL")
    private String logo;
}