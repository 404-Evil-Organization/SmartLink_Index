package com.zhilian.zhilianbackend.common.constant;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/21 11:40
 * @Param:
 * @Return:
 * @Description: 日期时间常量类，集中管理系统中与日期时间相关的常量，避免魔法值散落在业务代码中，提高代码可读性和可维护性。
 **/
public final class DateConstants {

    /**
     * 私有构造方法，防止实例化
     */
    private DateConstants() {
    }

    /**
     * 逻辑删除字段的“未删除”标记值（1970-01-01 00:00:00）
     */
    private static final String NOT_DELETED_TIME_STR = "1970-01-01 00:00:00";
    
    /**
     * @Author: taciturn-hg
     * @Date: 2026/3/21 13:37
     * @Param: 
     * @Return: 新创建的代表“未删除”时间点的 Date 实例
     * @Description: 获取逻辑删除字段的“未删除”标记时间
    **/
    public static Date getNotDeletedTime() {
        return Timestamp.valueOf(NOT_DELETED_TIME_STR);
    }
}