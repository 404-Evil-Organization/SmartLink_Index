package com.zhilian.zhilianbackend.service.algorithm.impl;

import com.zhilian.zhilianbackend.service.algorithm.DiagnosisAlgorithm;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/17 21:13
 * @Param:
 * @Return:
 * @Description: 诊断算法临时实现类（供测试用），实现DiagnosisAlgorithm接口，提供总分计算、等级判断、改进建议生成和雷达图数据转换的临时实现。
 **/
@Component
public class DiagnosisAlgorithmTempImpl implements DiagnosisAlgorithm {

    @Override
    public int calculateTotalScore(int infoScore, int autoScore, int dataScore, int serviceScore) {
        // 简单算法：各维度得分转换为百分比后加权平均
        // 1. 约定各维度原始得分范围为 1-5（与 DTO 校验和数据库 CHECK 约束保持一致），先进行边界裁剪，防止异常值影响计算
        int infoClamped = clampScore(infoScore);
        int autoClamped = clampScore(autoScore);
        int dataClamped = clampScore(dataScore);
        int serviceClamped = clampScore(serviceScore);

        // 2. 将 1-5 映射到 0-100 百分比（1 分约等于 20 分，5 分为 100 分）
        double infoPercent = infoClamped / 5.0 * 100.0;
        double autoPercent = autoClamped / 5.0 * 100.0;
        double dataPercent = dataClamped / 5.0 * 100.0;
        double servicePercent = serviceClamped / 5.0 * 100.0;

        // 3. 按权重加权平均：信息化30%、自动化30%、数据应用20%、服务协同20%
        double total = infoPercent * 0.3
                + autoPercent * 0.3
                + dataPercent * 0.2
                + servicePercent * 0.2;

        // 4. 安全裁剪到 0-100，避免违反数据库 CHECK(total_score BETWEEN 0 AND 100)
        total = Math.max(0.0, Math.min(100.0, total));

        return (int) Math.round(total);
    }

    @Override
    public String getLevel(int totalScore) {
        if (totalScore < 40) {
            return "起步期";
        } else if (totalScore < 60) {
            return "成长期";
        } else if (totalScore < 80) {
            return "成熟期";
        } else {
            return "引领期";
        }
    }

    @Override
    public List<String> generateSuggestions(int infoScore, int autoScore, int dataScore, int serviceScore) {
        List<String> suggestions = new ArrayList<>();

        if (infoScore <= 2) {
            suggestions.add("建议引入ERP、MES等信息化系统，提升生产管理效率");
        } else if (infoScore <= 4) {
            suggestions.add("可考虑打通各业务系统数据，实现信息互联互通");
        }

        if (autoScore <= 2) {
            suggestions.add("建议引入自动化设备，减少人工操作环节");
        } else if (autoScore <= 4) {
            suggestions.add("可考虑建设自动化产线，进一步提升生产效率");
        }

        if (dataScore <= 2) {
            suggestions.add("建议建立数据采集体系，开始积累生产数据");
        } else if (dataScore <= 4) {
            suggestions.add("可引入数据分析工具，挖掘数据价值辅助决策");
        }

        if (serviceScore <= 2) {
            suggestions.add("建议加强与外部服务商的合作，拓展业务渠道");
        } else if (serviceScore <= 4) {
            suggestions.add("可考虑将非核心业务外包，聚焦核心能力建设");
        }

        // 如果建议太少，添加通用建议
        if (suggestions.isEmpty()) {
            suggestions.add("继续保持数字化建设，探索更多创新应用场景");
        }

        return suggestions;
    }

    /**
     * 将单项诊断得分裁剪到 1-5 区间。
     * 说明：为防止历史脏数据或算法调整导致得分越界，这里做一次防御性裁剪，
     * 确保后续雷达图百分比转换始终落在 20%-100% 之间。
     */
    private int clampScore(int score) {
        if (score < 1) {
            return 1;
        }
        if (score > 5) {
            return 5;
        }
        return score;
    }

    @Override
    public Map<String, Integer> getRadarData(int infoScore, int autoScore, int dataScore, int serviceScore) {
        Map<String, Integer> radarData = new HashMap<>();
        // 对输入得分做边界裁剪，确保在 1-5 分区间内
        int clampedInfoScore = clampScore(infoScore);
        int clampedAutoScore = clampScore(autoScore);
        int clampedDataScore = clampScore(dataScore);
        int clampedServiceScore = clampScore(serviceScore);
        // 转换为百分比显示（1-5分对应20%-100%）
        radarData.put("信息化", clampedInfoScore * 20);
        radarData.put("自动化", clampedAutoScore * 20);
        radarData.put("数据应用", clampedDataScore * 20);
        radarData.put("服务协同", clampedServiceScore * 20);
        return radarData;
    }
}
