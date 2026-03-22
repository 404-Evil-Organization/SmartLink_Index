package com.zhilian.zhilianbackend.service.algorithm.impl;

import com.zhilian.zhilianbackend.service.algorithm.RegionIndexAlgorithm;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/18 21:28
 * @Param:
 * @Return:
 * @Description: 区域指数算法实现类，实现区域指数相关的计算方法
 **/
@Component
public class RegionIndexAlgorithmImpl implements RegionIndexAlgorithm {
    // 权重配置：合作密度40%，服务渗透率35%，跨域协同度25%
    private static final double COOP_DENSITY_WEIGHT = 0.4;
    private static final double SERVICE_RATE_WEIGHT = 0.35;
    private static final double CROSS_RATE_WEIGHT = 0.25;

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:02
     * @Param: coopCount 合作次数 manufactureCount 制造企业总数
     * @Return: BigDecimal 合作密度，保留4位小数
     * @Description: 计算合作密度，如果制造企业总数为0则返回0
    **/
    @Override
    public BigDecimal calculateCoopDensity(int coopCount, int manufactureCount) {
        if (manufactureCount == 0) {
            return BigDecimal.ZERO;
        }
        // 合作密度 = 合作次数 / 制造企业总数
        return BigDecimal.valueOf(coopCount)
                .divide(BigDecimal.valueOf(manufactureCount), 4, RoundingMode.HALF_UP);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:03
     * @Param: serviceUserCount 使用服务的企业数 manufactureCount 制造企业总数
     * @Return: BigDecimal 服务渗透率，保留4位小数
     * @Description: 计算服务渗透率，如果制造企业总数为0则返回0
    **/
    @Override
    public BigDecimal calculateServiceRate(int serviceUserCount, int manufactureCount) {
        if (manufactureCount == 0) {
            return BigDecimal.ZERO;
        }
        // 服务渗透率 = 使用服务的企业数 / 制造企业总数
        return BigDecimal.valueOf(serviceUserCount)
                .divide(BigDecimal.valueOf(manufactureCount), 4, RoundingMode.HALF_UP);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:03
     * @Param: crossRegionCoopCount 跨区域合作次数 totalCoopCount 总合作次数
     * @Return: BigDecimal 跨域协同度，保留4位小数
     * @Description: 计算跨域协同度，如果总合作为0则返回0
    **/
    @Override
    public BigDecimal calculateCrossRate(int crossRegionCoopCount, int totalCoopCount) {
        if (totalCoopCount == 0) {
            return BigDecimal.ZERO;
        }
        // 跨域协同度 = 跨区域合作次数 / 总合作次数
        return BigDecimal.valueOf(crossRegionCoopCount)
                .divide(BigDecimal.valueOf(totalCoopCount), 4, RoundingMode.HALF_UP);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:04
     * @Param: coopDensity 合作密度 serviceRate 服务渗透率 crossRate 跨域协同度
     * @Return: BigDecimal 综合得分，保留2位小数
     * @Description: 计算综合指数，加权后乘以100
    **/
    @Override
    public BigDecimal calculateTotalIndex(BigDecimal coopDensity, BigDecimal serviceRate, BigDecimal crossRate) {
        // 综合指数 = (合作密度 * 40% + 服务渗透率 * 35% + 跨域协同度 * 25%) * 100
        BigDecimal total = coopDensity.multiply(BigDecimal.valueOf(COOP_DENSITY_WEIGHT))
                .add(serviceRate.multiply(BigDecimal.valueOf(SERVICE_RATE_WEIGHT)))
                .add(crossRate.multiply(BigDecimal.valueOf(CROSS_RATE_WEIGHT)))
                .multiply(BigDecimal.valueOf(100));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:04
     * @Param: 
     * @Return: Map<String, Double> 权重配置
     * @Description: 获取各指标的权重配置
    **/
    @Override
    public Map<String, Double> getWeights() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("coopDensity", COOP_DENSITY_WEIGHT);
        weights.put("serviceRate", SERVICE_RATE_WEIGHT);
        weights.put("crossRate", CROSS_RATE_WEIGHT);
        return weights;
    }
}
