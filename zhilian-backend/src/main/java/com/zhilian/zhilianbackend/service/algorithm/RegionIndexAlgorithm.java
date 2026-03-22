package com.zhilian.zhilianbackend.service.algorithm;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/18 21:27
 * @Param:
 * @Return:
 * @Description: 区域指数算法接口，定义区域指数相关的计算方法
 **/
public interface RegionIndexAlgorithm {
    /**
     * @Author: 6017
     * @Date: 2026/3/18 22:58
     * @Param: coopCount 合作次数 manufactureCount 制造企业总数
     * @Return: BigDecimal 合作密度
     * @Description: 计算合作密度（合作次数/制造企业总数）
    **/
    BigDecimal calculateCoopDensity(int coopCount, int manufactureCount);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 22:58
     * @Param: serviceUserCount 使用服务的企业数 manufactureCount 制造企业总数
     * @Return: BigDecimal 服务渗透率
     * @Description: 计算服务渗透率（使用服务企业数/制造企业总数）
    **/
    BigDecimal calculateServiceRate(int serviceUserCount, int manufactureCount);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 22:59
     * @Param: crossRegionCoopCount 跨区域合作次数 totalCoopCount 总合作次数
     * @Return: BigDecimal 跨域协同度
     * @Description: 计算跨域协同度（跨区域合作次数/总合作次数）
    **/
    BigDecimal calculateCrossRate(int crossRegionCoopCount, int totalCoopCount);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:01
     * @Param: coopDensity 合作密度 serviceRate 服务渗透率 crossRate 跨域协同度
     * @Return: BigDecimal 综合得分
     * @Description: 计算综合指数（加权计算后乘以100）
    **/
    BigDecimal calculateTotalIndex(BigDecimal coopDensity, BigDecimal serviceRate, BigDecimal crossRate);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:02
     * @Param: 
     * @Return: Map<String, Double> 权重配置
     * @Description: 获取各指标的权重配置
    **/
    Map<String, Double> getWeights();
}
