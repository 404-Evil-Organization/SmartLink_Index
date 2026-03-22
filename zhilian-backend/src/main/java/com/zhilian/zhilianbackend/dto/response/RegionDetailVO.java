package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "特定区域指数详情")
public class RegionDetailVO {

    @Schema(description = "记录ID", example = "101")
    private Long id;

    @Schema(description = "区域名称", example = "深圳")
    private String region;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "周期类型：quarter/month", example = "quarter")
    private String periodType;

    @Schema(description = "周期值（季度1-4，月份1-12）", example = "1")
    private Integer periodValue;

    @Schema(description = "合作密度", example = "0.85")
    private BigDecimal coopDensity;

    @Schema(description = "服务渗透率", example = "0.72")
    private BigDecimal serviceRate;

    @Schema(description = "跨域协同度", example = "0.45")
    private BigDecimal crossRate;

    @Schema(description = "协同指数综合得分", example = "75.8")
    private BigDecimal totalIndex;

    @Schema(description = "计算时间", example = "2026-04-01 00:00:00")
    private Date calcTime;

    @Schema(description = "创建时间", example = "2026-04-01 00:10:00")
    private Date createTime;

    @Schema(description = "更新时间", example = "2026-04-01 00:10:00")
    private Date updateTime;
}