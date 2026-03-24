package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "新增区域指数请求")
public class RegionIndexCreateRequest {

    @NotBlank(message = "区域不能为空")
    @Schema(description = "区域", example = "深圳")
    private String region;

    @NotNull(message = "年份不能为空")
    @Min(value = 2000, message = "年份最小2000")
    @Max(value = 2100, message = "年份最大2100")
    @Schema(description = "年份", example = "2026")
    private Integer year;

    @NotNull(message = "季度不能为空")
    @Min(value = 1, message = "季度必须为1-4")
    @Max(value = 4, message = "季度必须为1-4")
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