package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 20:17
 * @Param:
 * @Return:
 * @Description: 制造企业列表返回对象
 **/
@Getter
@Setter
@ToString
@Schema(description = "制造企业列表返回对象")
public class ManufactureListVO {

    @Schema(description = "企业ID")
    private Long id;

    @Schema(description = "企业全称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "规模：micro/small/medium/large")
    private String scale;

    @Schema(description = "主营产品类型")
    private String productType;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "审核状态：pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "Logo图片URL")
    private String logo;
}