package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 6017
 * @Date: 2026/3/20 23:57
 * @Param:
 * @Return:
 * @Description: 个人制造企业列表响应DTO
**/
@Data
@Schema(description = "个人制造企业列表响应")
public class EnterpriseManufactureVO {

    @Schema(description = "企业ID")
    private Long id;

    @Schema(description = "企业全称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "规模")
    private String scale;

    @Schema(description = "主营产品类型")
    private String productType;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}