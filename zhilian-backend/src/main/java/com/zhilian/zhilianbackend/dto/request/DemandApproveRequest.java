package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "需求审核请求参数")
public class DemandApproveRequest {

    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "approved|rejected", message = "审核状态必须为 approved 或 rejected")
    @Schema(description = "审核状态", allowableValues = {"approved", "rejected"}, example = "approved")
    private String status;

    @Schema(description = "审核意见（驳回时填写）", example = "需求描述不清晰")
    private String remark;
}