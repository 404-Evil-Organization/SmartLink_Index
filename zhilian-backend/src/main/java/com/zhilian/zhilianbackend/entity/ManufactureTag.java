package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/15 11:12
 * @Param:
 * @Return:
 * @Description: 制造企业能力标签表实体类，对应manufacture_tag表
 **/
@Getter
@Setter
@Accessors(chain = true)
@TableName("manufacture_tag")
public class ManufactureTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 关联manufacture.id
     */
    @TableField("manufacture_id")
    private Long manufactureId;

    /**
     * 关联tag.id
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 逻辑删除时间，NULL未删除，非NULL已删除
     */
    @TableField("deleted")
    @TableLogic(value = "null", delval = "now()")
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