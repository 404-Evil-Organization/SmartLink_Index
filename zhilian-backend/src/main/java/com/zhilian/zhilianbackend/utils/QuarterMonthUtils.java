package com.zhilian.zhilianbackend.utils;

import com.zhilian.zhilianbackend.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuarterMonthUtils {

    @Data
    @AllArgsConstructor
    public static class QuarterInfo {
        private int year;
        private int quarter;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: quarterStr 季度字符串
     * @Return: QuarterInfo 包含年份和季度
     * @Description: 解析季度字符串，格式如 "2025Q1"
     **/
    public static QuarterInfo parseQuarter(String quarterStr) {
        if (quarterStr == null || quarterStr.length() != 6 || quarterStr.charAt(4) != 'Q') {
            throw new BusinessException(400, "季度格式错误，应为 '2025Q1' 格式");
        }
        String yearPart = quarterStr.substring(0, 4);
        String qPart = quarterStr.substring(5, 6);
        try {
            int year = Integer.parseInt(yearPart);
            int quarter = Integer.parseInt(qPart);
            if (quarter < 1 || quarter > 4) {
                throw new BusinessException(400, "季度值必须在1-4之间");
            }
            return new QuarterInfo(year, quarter);
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("季度字符串解析失败（debug 堆栈），原始入参: {}", quarterStr, e);
            }
            throw new BusinessException(400, "季度格式错误，应为 '2025Q1' 格式");
        }
    }
}