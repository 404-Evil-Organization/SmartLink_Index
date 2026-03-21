package com.zhilian.zhilianbackend.common.constant;

import java.time.LocalDateTime;

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
     * 逻辑删除字段的“未删除”标记值
     * <p>
     * 当 deleted 字段类型为 DATETIME 时，约定该值表示数据未被逻辑删除。
     * 注意：此值应与数据库中的实际值保持一致（如 1970-01-01 00:00:00）。
     * </p>
     */
    public static final LocalDateTime NOT_DELETED_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
}