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
 * @Description: 标签类别枚举（value存储英文，description存储中文）
 **/
@Getter
public enum TagCategory {

    SERVICE("service", "服务类型"),
    CERTIFICATION("certification", "认证类型"),
    PRODUCT("product", "产品类型"),
    GENERAL("general", "其他类型");  // 只保留 GENERAL，删除 RESTS

    private static final Logger LOGGER = LoggerFactory.getLogger(TagCategory.class);

    @EnumValue
    @JsonValue
    private final String value;

    private final String description;

    TagCategory(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: value 英文枚举值
     * @Return: 对应的 TagCategory 枚举
     * @Description: 将字符串值安全地转换为 TagCategory
     */
    public static TagCategory fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return GENERAL;
        }

        String normalized = value.trim().toLowerCase();

        // 兼容旧数据中的 rests，映射到 GENERAL
        if ("rests".equals(normalized)) {
            LOGGER.info("兼容处理：将 rests 映射为 general");
            return GENERAL;
        }

        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(normalized)) {
                return category;
            }
        }
        throw new IllegalArgumentException("无效的标签类别: " + value);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: value 待校验的类别值
     * @Return: true-有效 false-无效
     * @Description: 判断是否为有效的类别值
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim().toLowerCase();
        // 兼容 rests
        if ("rests".equals(normalized)) {
            return true;
        }
        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Return: 所有类别的英文值列表
     * @Description: 获取所有有效类别的英文值
     */
    public static List<String> getAllValues() {
        return Arrays.stream(TagCategory.values())
                .map(TagCategory::getValue)
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Return: 所有类别的中文描述列表
     * @Description: 获取所有有效类别的中文描述
     */
    public static List<String> getAllDescriptions() {
        return Arrays.stream(TagCategory.values())
                .map(TagCategory::getDescription)
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Return: 标签类别选项列表（用于前端下拉框）
     * @Description: 获取所有有效类别的键值对
     */
    public static List<Map<String, String>> getOptions() {
        return Arrays.stream(TagCategory.values())
                .map(category -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("value", category.getValue());
                    map.put("label", category.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:07
     * @Param: value 标签枚举的英文值
     * @Return: 对应的中文描述
     * @Description: 根据枚举 value 获取中文描述
     */
    public static String getDescriptionByValue(String value) {
        if (value == null) {
            return GENERAL.getDescription();
        }

        String normalized = value.trim().toLowerCase();

        // 兼容 rests
        if ("rests".equals(normalized)) {
            return GENERAL.getDescription();
        }

        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(normalized)) {
                return category.getDescription();
            }
        }

        LOGGER.warn("未找到匹配枚举，value: {}，使用默认值", value);
        return GENERAL.getDescription();
    }
}