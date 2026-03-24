package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:34
 * @Param: 
 * @Return: 
 * @Description: 企业审核请求参数，用于管理员审核企业时传递审核信息
**/
@Data
@Schema(description = "企业审核请求参数")
public class EnterpriseApproveRequest {

    @Schema(description = "企业类型：manufacture 或 service", example = "manufacture", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "企业类型不能为空")
    @Pattern(regexp = "^(manufacture|service)$", message = "企业类型只能是 manufacture 或 service")
    private String type;

    @Schema(description = "审核状态：approved 通过 / rejected 驳回", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "^(approved|rejected)$", message = "审核状态只能是 approved 或 rejected")
    private String status;

    @Schema(description = "审核意见（驳回时建议填写）", example = "营业执照不清晰，请重新上传")
    private String remark;
}