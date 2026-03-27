package com.zhilian.zhilianbackend.annotation;

import java.lang.annotation.*;

/**
 * @Author: taciturn-hg
 * @Date: 2026/03/27 9:05
 * @Description: 自定义操作日志注解，标记在 Controller 方法上用于自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    /**
     * @Return: String 操作描述
     * @Description: 获取操作描述，如 "用户登录"、"新增需求" 等。此描述将被记录到日志表中的 operation 字段。
     */
    String value() default "";
}