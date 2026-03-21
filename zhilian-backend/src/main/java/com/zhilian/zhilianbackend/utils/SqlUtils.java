package com.zhilian.zhilianbackend.utils;

import org.apache.commons.lang3.StringUtils;

public class SqlUtils {

    /**
     * 转义SQL LIKE特殊字符
     * @param keyword 原始关键字
     * @return 转义后的关键字
     */
    public static String escapeSqlLike(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return keyword;
        }
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }
}
