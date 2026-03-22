package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "区域指标列表项")
public class RegionListItemVO {

    @Schema(description = "区域名称", example = "深圳")
    private String region;

    @Schema(description = "合作密度", example = "0.85")
    private BigDecimal coopDensity;

    @Schema(description = "服务渗透率", example = "0.72")
    private BigDecimal serviceRate;

    @Schema(description = "跨域协同度", example = "0.45")
    private BigDecimal crossRate;

    @Schema(description = "协同指数综合得分", example = "75.8")
    private BigDecimal totalIndex;
}