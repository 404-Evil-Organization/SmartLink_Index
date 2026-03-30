package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获取趋势数据请求参数")
public class TrendQuery {

    @Schema(description = "区域", required = true, example = "深圳")
    private String region;

    @Schema(description = "开始时间，格式 'yyyy-MM-dd'", example = "2025-01-01")
    private String start;

    @Schema(description = "结束时间，格式 'yyyy-MM-dd'", example = "2026-12-31")
    private String end;

    @Schema(description = "周期类型，quarter 或 month", example = "quarter")
    private String periodType;
}