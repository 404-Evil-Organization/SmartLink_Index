package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *@Author: 6017
 *@Date: 2026/3/18 21:32
 *@Param: 
 *@Return: 
 *@Description: 区域趋势数据响应VO，用于返回区域趋势数据给前端
 **/
@Data
@Schema(description = "区域趋势数据响应VO")
public class RegionTrendVO {
    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "深圳")
    private String region;

    /**
     * 趋势数据列表
     */
    @Schema(description = "趋势数据列表")
    private List<TrendPoint> trendData;

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:05
     * @Param: 
     * @Return: 
     * @Description: 趋势点数据内部类
    **/
    @Data
    @Schema(description = "趋势点数据")
    public static class TrendPoint {
        /**
         * 时间标签（如 "2024Q1", "2024Q2"）
         */
        @Schema(description = "时间标签（如 '2024Q1', '2024-03'）", example = "2024Q1")
        private String period;

        /**
         * 综合指数
         */
        @Schema(description = "综合指数", example = "75.8")
        private BigDecimal totalIndex;

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
    }
}
