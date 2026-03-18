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
     * 解析季度字符串，格式如 "2025Q1"
     * @param quarterStr 季度字符串
     * @return QuarterInfo 包含年份和季度
     * @throws BusinessException 当季度字符串格式错误或季度值不在 1-4 之间时抛出，HTTP 语义为 400
     */
    public static QuarterInfo parseQuarter(String quarterStr) {
        if (quarterStr == null || quarterStr.length() != 6 || quarterStr.charAt(4) != 'Q') {
            // 前端传入的季度字符串格式错误，属于客户端参数错误，抛出 BusinessException(400)
            throw new BusinessException(400, "季度格式错误，应为 '2025Q1' 格式");
        }
        String yearPart = quarterStr.substring(0, 4);
        String qPart = quarterStr.substring(5, 6);
        try {
            int year = Integer.parseInt(yearPart);
            int quarter = Integer.parseInt(qPart);
            if (quarter < 1 || quarter > 4) {
                // 季度值超出合法范围，同样视为参数错误
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