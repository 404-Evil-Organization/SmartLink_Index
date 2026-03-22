package com.zhilian.zhilianbackend.service.algorithm;

import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/17 21:12
 * @Param:
 * @Return:
 * @Description: 诊断算法接口，定义诊断相关的计算逻辑
 **/
public interface DiagnosisAlgorithm {
    /**
     * 计算总分
     * @param infoScore 信息化得分
     * @param autoScore 自动化得分
     * @param dataScore 数据应用得分
     * @param serviceScore 服务协同得分
     * @return 总分（0-100）
     */
    int calculateTotalScore(int infoScore, int autoScore, int dataScore, int serviceScore);

    /**
     * 获取等级
     * @param totalScore 总分
     * @return 等级（起步期/成长期/成熟期/引领期）
     */
    String getLevel(int totalScore);

    /**
     * 生成改进建议
     * @param infoScore 信息化得分
     * @param autoScore 自动化得分
     * @param dataScore 数据应用得分
     * @param serviceScore 服务协同得分
     * @return 改进建议列表
     */
    List<String> generateSuggestions(int infoScore, int autoScore, int dataScore, int serviceScore);

    /**
     * 获取雷达图数据
     * @param infoScore 信息化得分
     * @param autoScore 自动化得分
     * @param dataScore 数据应用得分
     * @param serviceScore 服务协同得分
     * @return 雷达图数据（维度名 -> 得分百分比）
     */
    Map<String, Integer> getRadarData(int infoScore, int autoScore, int dataScore, int serviceScore);
}
