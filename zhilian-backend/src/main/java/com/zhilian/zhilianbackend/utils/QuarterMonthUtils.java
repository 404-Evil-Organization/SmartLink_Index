package com.zhilian.zhilianbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

public class QuarterMonthUtils {

    @Data
    @AllArgsConstructor
    public static class QuarterInfo {
        private int year;
        private int quarter;
    }

    /**
     * 解析季度字符串，格式如 "2025Q1"
     * @param quarterStr 季度字符串
     * @return QuarterInfo 包含年份和季度
     * @throws IllegalArgumentException 格式错误时抛出
     */
    public static QuarterInfo parseQuarter(String quarterStr) {
        if (quarterStr == null || quarterStr.length() != 6 || quarterStr.charAt(4) != 'Q') {
            throw new IllegalArgumentException("季度格式错误，应为 '2025Q1' 格式");
        }
        String yearPart = quarterStr.substring(0, 4);
        String qPart = quarterStr.substring(5, 6);
        try {
            int year = Integer.parseInt(yearPart);
            int quarter = Integer.parseInt(qPart);
            if (quarter < 1 || quarter > 4) {
                throw new IllegalArgumentException("季度值必须在1-4之间");
            }
            return new QuarterInfo(year, quarter);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("季度格式错误，应为 '2025Q1' 格式", e);
        }
    }
}