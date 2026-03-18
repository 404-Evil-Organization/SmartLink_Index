package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "趋势数据项")
public class TrendItemVO {

    @Schema(description = "日期（季度格式如 2025Q1，月份格式如 2025-03）", example = "2025Q1")
    private String date;

    @Schema(description = "协同指数综合得分", example = "75.8")
    private BigDecimal totalIndex;
}