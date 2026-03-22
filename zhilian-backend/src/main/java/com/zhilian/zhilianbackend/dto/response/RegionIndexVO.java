package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *@Author: 6017
 *@Date: 2026/3/18 21:32
 *@Param: 
 *@Return: 
 *@Description: 区域指数响应VO，用于返回区域指数数据给前端
 **/
@Data
@Schema(description = "区域指数响应VO")
public class RegionIndexVO {
    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "深圳")
    private String region;

    /**
     * 年份
     */
    @Schema(description = "年份", example = "2026")
    private Integer year;

    /**
     * 季度（1-4）
     */
    @Schema(description = "季度（1-4）", example = "1")
    private Integer quarter;

    /**
     * 合作密度
     */
    @Schema(description = "合作密度", example = "0.85")
    private BigDecimal coopDensity;

    /**
     * 服务渗透率
     */
    @Schema(description = "服务渗透率", example = "0.72")
    private BigDecimal serviceRate;

    /**
     * 跨域协同度
     */
    @Schema(description = "跨域协同度", example = "0.45")
    private BigDecimal crossRate;

    /**
     * 综合指数
     */
    @Schema(description = "综合指数", example = "75.8")
    private BigDecimal totalIndex;

    /**
     * 计算时间
     */
    @Schema(description = "计算时间", example = "2026-04-01 00:00:00")
    private Date calcTime;
}
