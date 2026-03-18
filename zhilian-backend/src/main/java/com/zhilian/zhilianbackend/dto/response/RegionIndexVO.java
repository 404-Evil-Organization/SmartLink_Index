package com.zhilian.zhilianbackend.dto.response;

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
public class RegionIndexVO {
    /**
     * 区域名称
     */
    private String region;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 季度（1-4）
     */
    private Integer quarter;

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

    /**
     * 综合指数
     */
    private BigDecimal totalIndex;

    /**
     * 计算时间
     */
    private Date calcTime;
}
