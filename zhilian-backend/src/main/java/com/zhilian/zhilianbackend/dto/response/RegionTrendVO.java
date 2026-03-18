package com.zhilian.zhilianbackend.dto.response;

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
public class RegionTrendVO {
    /**
     * 区域名称
     */
    private String region;

    /**
     * 趋势数据列表
     */
    private List<TrendPoint> trendData;

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:05
     * @Param: 
     * @Return: 
     * @Description: 趋势点数据内部类
    **/
    @Data
    public static class TrendPoint {
        /**
         * 时间标签（如 "2024Q1", "2024Q2"）
         */
        private String period;

        /**
         * 综合指数
         */
        private BigDecimal totalIndex;

        /**
         * 合作密度
         */
        private BigDecimal coopDensity;

        /**
         * 服务渗透率
         */
        private BigDecimal serviceRate;

        /**
         * 跨域协同度
         */
        private BigDecimal crossRate;
    }
}
