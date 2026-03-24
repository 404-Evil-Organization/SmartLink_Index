package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "修改区域指数请求")
public class RegionIndexUpdateRequest {

    @Schema(description = "区域", example = "深圳")
    private String region;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "季度", example = "1")
    private Integer quarter;

    @Schema(description = "合作密度", example = "0.85")
    private BigDecimal coopDensity;

    @Schema(description = "服务渗透率", example = "0.72")
    private BigDecimal serviceRate;

    @Schema(description = "跨域协同度", example = "0.45")
    private BigDecimal crossRate;

    @Schema(description = "综合得分", example = "75.8")
    private BigDecimal totalIndex;
}