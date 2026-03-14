package com.zhilian.zhilianbackend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: 6017
 * @Date: 2026/3/13 23:04
 * @Param:
 * @Return:
 * @Description: 标签类别枚举（value存储英文，description存储中文）
 **/
@Getter
public enum TagCategory {

    /**
     * 日志对象：用于记录枚举解析过程中的告警信息（如空值或未命中时的回退行为）
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TagCategory.class);

    SERVICE("service", "服务类型"),
    CERTIFICATION("certification", "认证类型"),
    PRODUCT("product", "产品类型"),
    // 兼容历史库中使用的 rests 取值，仅用于反向解析输入，不应在新逻辑中直接使用
    RESTS("rests", "其他类型"),
    // 为了与数据库 schema.sql 中 tag.category 默认值 general 对齐，引入 GENERAL 枚举常量，作为唯一主值
    GENERAL("general", "其他类型");

    @EnumValue  // MyBatis-Plus 存储时使用这个值（英文）
    @JsonValue  // JSON 序列化时使用这个值（英文）
    private final String value;

    private final String description;  // 中文描述，用于前端显示

    TagCategory(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: value 英文枚举值（如 service / certification / product / general / rests）
     * @Return: 对应的 TagCategory 枚举
     * @Description:
     *  将字符串值安全地转换为 TagCategory：
     *  - value 为空时，默认返回 GENERAL（与数据库默认值 general 对齐）
     *  - 支持 existing 值 rests（RESTS）和 general（GENERAL），兼容历史与当前取值
     */
    public static TagCategory fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            // 当未显式指定标签类别时，统一视为 GENERAL（其他类型），与 schema.sql 默认值 general 一致
            return GENERAL;
        }

        // 统一进行大小写与空白处理，避免因为大小写或前后空格导致匹配失败
        String normalized = value.trim().toLowerCase();

        // 兼容历史值：rests 与当前 schema 默认值 general 统一映射为 GENERAL
        if ("rests".equals(normalized) || "general".equals(normalized)) {
            return GENERAL;
        }

        // 其他值（service / certification / product 等）按枚举 value 精确匹配
        for (TagCategory category : TagCategory.values()) {
            // 避免误将 RESTS 作为可返回值，如后续再引入其他别名可继续在此排除
            if (category == RESTS) {
                continue;
            }
            if (category.getValue().equals(normalized)) {
                return category;
            }
        }
        throw new IllegalArgumentException("无效的标签类别: " + value);
    }

  /**
   * @Author: 6017
   * @Date: 2026/3/14 01:06
   * @Param: 
   * @Return: 
   * @Description: 
  **/
    public static boolean isValid(String value) {
        if (value == null) {
            return true;  // null 会被处理为默认值
        }
        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Param: 
     * @Return: 
     * @Description: 
    **/
    public static List<String> getAllValues() {
        return Arrays.stream(TagCategory.values())
                .map(TagCategory::getValue)
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Param: 
     * @Return: 
     * @Description: 
    **/
    public static List<String> getAllDescriptions() {
        return Arrays.stream(TagCategory.values())
                .map(TagCategory::getDescription)
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Param: 
     * @Return: 
     * @Description: 
    **/
    public static List<Map<String, String>> getOptions() {
        return Arrays.stream(TagCategory.values())
                .map(category -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("value", category.getValue());        // 英文值：service
                    map.put("label", category.getDescription());  // 中文标签：服务类型
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Param: value 标签枚举的英文值（如 "general"）
     * @Return: 对应的中文描述；当 value 为空或未命中时，统一回退到 GENERAL 的描述
     * @Description: 根据枚举 value 获取中文描述，空值/未命中时与 fromValue() 保持一致，默认 GENERAL，并记录告警日志
    **/
    public static String getDescriptionByValue(String value) {
        // 与 fromValue() 以及数据库默认值保持一致：统一使用 GENERAL 作为兜底枚举
        TagCategory defaultCategory = GENERAL;

        if (value == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("TagCategory.getDescriptionByValue 接收到空 value，使用默认枚举值: {}", defaultCategory.getValue());
            }
            return defaultCategory.getDescription();
        }

        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(value)) {
                return category.getDescription();
            }
        }

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("TagCategory.getDescriptionByValue 未找到匹配枚举，value: {}，使用默认枚举值: {}", value, defaultCategory.getValue());
        }
        return defaultCategory.getDescription();
    }
}
