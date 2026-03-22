package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获取所有区域指标请求参数")
public class RegionListQuery {

    @Schema(description = "季度，格式如 '2025Q1'，与 year/month 互斥，优先使用", example = "2025Q1")
    private String quarter;

    @Schema(description = "年份，与 month 配合使用（不能与 quarter 同时使用）", example = "2026")
    private Integer year;

    @Schema(description = "月份 (1-12)，与 year 配合使用（不能与 quarter 同时使用）", example = "3")
    private Integer month;
}