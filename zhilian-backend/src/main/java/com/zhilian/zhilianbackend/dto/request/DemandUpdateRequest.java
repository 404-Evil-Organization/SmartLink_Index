package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "编辑需求请求参数")
public class DemandUpdateRequest {

    @Schema(description = "需求标题", example = "更新后的标题")
    private String title;

    @Schema(description = "详细描述", example = "更新后的描述")
    private String description;

    @DecimalMin(value = "0.0", message = "预算必须大于等于0")
    @Schema(description = "预算金额（万元）", example = "20.0")
    private BigDecimal expectedBudget;

    @Schema(description = "期望完成日期", example = "2026-07-01")
    private Date deadline;

    @Schema(description = "标签名称列表", example = "[\"新标签1\", \"新标签2\"]")
    private List<String> tags;
}