package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/25 17:55
 * @Param:
 * @Return:
 * @Description: 国家准入指南实体类，对应country_guide表
 **/
@Getter
@Setter
@Accessors(chain = true)
@TableName("country_guide")
public class CountryGuide implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 国家名称
     */
    @TableField("country")
    private String country;

    /**
     * 准入要求
     */
    @TableField("requirements")
    private String requirements;

    /**
     * 办理流程
     */
    @TableField("process")
    private String process;

    /**
     * 所需材料（可存JSON或文本）
     */
    @TableField("documents")
    private String documents;

    /**
     * 逻辑删除时间，'1970-01-01 00:00:00' 表示未删除，其他时间表示已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
