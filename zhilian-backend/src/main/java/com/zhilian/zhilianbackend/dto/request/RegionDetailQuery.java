package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获取特定区域指数请求参数")
public class RegionDetailQuery {

    @Schema(description = "年份，与 quarter 或 month 配合使用", example = "2026")
    private Integer year;

    @Schema(description = "季度 (1-4)，与 year 配合使用，此时不能传 month", example = "1")
    private Integer quarter;

    @Schema(description = "月份 (1-12)，与 year 配合使用，此时不能传 quarter", example = "3")
    private Integer month;
}