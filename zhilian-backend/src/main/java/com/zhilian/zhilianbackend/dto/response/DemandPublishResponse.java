package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发布需求响应")
public class DemandPublishResponse {
    @Schema(description = "需求ID")
    private Long demandId;

    @Schema(description = "审核状态", example = "pending")
    private String auditStatus;
}