package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:45
 * @Param: 
 * @Return: 
 * @Description: 标签字典表实体类，对应tag表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称（如“PCB设计”、“CE认证”）
     */
    @TableField("name")
    private String name;

    /**
     * 标签类别（英文）：service, certification, product, general
     * 前端展示时需要根据枚举转换为中文
     */
    @TableField("category")
    private String category;

    /**
     * 标签说明
     */
    @TableField("description")
    private String description;

    /**
     * 逻辑删除时间，'1970-01-01 00:00:00' 表示未删除，其他时间表示已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 记录创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
