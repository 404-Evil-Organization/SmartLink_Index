package com.zhilian.zhilianbackend.common.constant;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * 逻辑删除字段的"未删除"标记值（1970-01-01 00:00:00）
     */
    private static final String NOT_DELETED_TIME_STR = "1970-01-01 00:00:00";

    /**
     * 逻辑删除字段的"未删除"标记时间常量（缓存的 Timestamp 实例）
     */
    private static final Timestamp NOT_DELETED_TIME = Timestamp.valueOf(NOT_DELETED_TIME_STR);

    /**
     * 逻辑删除字段的"未删除"标记时间常量（LocalDateTime 实例，用于 MyBatis 参数）
     */
    private static final LocalDateTime NOT_DELETED_LOCAL_DATE_TIME = NOT_DELETED_TIME.toLocalDateTime();
    /**
     * 日期时间格式化器（yyyy-MM-dd HH:mm:ss）
     */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * @Author: taciturn-hg
     * @Date: 2026/3/21 13:37
     * @Param:
     * @Return: 代表"未删除"时间点的 Date 常量实例
     * @Description: 获取逻辑删除字段的"未删除"标记时间（Date 类型）
     **/
    public static Date getNotDeletedTime() {
        // 返回防御性拷贝，避免外部修改污染全局常量
        return new Date(NOT_DELETED_TIME.getTime());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/22
     * @Param:
     * @Return: 代表"未删除"时间点的字符串
     * @Description: 获取逻辑删除字段的"未删除"标记时间字符串
     **/
    public static String getNotDeletedTimeStr() {
        return NOT_DELETED_TIME_STR;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/22
     * @Param:
     * @Return: 代表"未删除"时间点的 LocalDateTime 实例
     * @Description: 获取逻辑删除字段的"未删除"标记时间（LocalDateTime 类型，用于 MyBatis 参数）
     **/
    public static LocalDateTime getNotDeletedLocalDateTime() {
        return NOT_DELETED_LOCAL_DATE_TIME;
    }
}