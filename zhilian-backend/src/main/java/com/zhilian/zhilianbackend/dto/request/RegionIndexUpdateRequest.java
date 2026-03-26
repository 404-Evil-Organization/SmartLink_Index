package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "修改区域指数请求")
public class RegionIndexUpdateRequest {

    @Schema(description = "区域", example = "深圳")
    private String region;

    @Schema(description = "年份", example = "2026")
    @Min(value = 1900, message = "年份最小为1900")
    @Max(value = 2200, message = "年份最大为2200")
    private Integer year;

    @Schema(description = "周期类型：quarter(季度) 或 month(月份)", example = "quarter")
    private String periodType;

    @Min(value = 1, message = "周期值最小为1")
    @Max(value = 12, message = "周期值最大为12")
    @Schema(description = "周期值", example = "1")
    private Integer periodValue;

    @Schema(description = "合作密度", example = "0.85")
    private BigDecimal coopDensity;

    @Schema(description = "服务渗透率", example = "0.72")
    private BigDecimal serviceRate;

    @Schema(description = "跨域协同度", example = "0.45")
    private BigDecimal crossRate;

    @Schema(description = "综合得分", example = "75.8")
    private BigDecimal totalIndex;
}