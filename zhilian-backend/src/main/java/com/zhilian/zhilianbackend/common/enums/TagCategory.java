package com.zhilian.zhilianbackend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/13 23:04
 * @Param:
 * @Return:
 * @Description: 标签类别枚举（value存储英文，description存储中文）
 **/
@Getter
public enum TagCategory {

    SERVICE("service", "服务类型"),
    CERTIFICATION("certification", "认证类型"),
    PRODUCT("product", "产品类型"),
    RESTS("rests", "其他类型");

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
     * @Param: 
     * @Return: 
     * @Description: 
    **/
    public static TagCategory fromValue(String value) {
        if (value == null) {
            return RESTS;  // 默认返回其他标签
        }
        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(value)) {
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
     * @Param: 
     * @Return: 
     * @Description: 
    **/
    public static String getDescriptionByValue(String value) {
        if (value == null) return RESTS.getDescription();
        for (TagCategory category : TagCategory.values()) {
            if (category.getValue().equals(value)) {
                return category.getDescription();
            }
        }
        return RESTS.getDescription();
    }
}
