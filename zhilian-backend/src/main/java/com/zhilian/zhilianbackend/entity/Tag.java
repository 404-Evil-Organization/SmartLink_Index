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
 * <p>
 * 标签字典表
 * </p>
 *
 * @author 智链团队
 * @since 2026-03-08
 */
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
     * 标签类别（如“服务类型”、“认证类型”）
     */
    @TableField("category")
    private String category;

    /**
     * 标签说明
     */
    @TableField("description")
    private String description;

    /**
     * 逻辑删除标记，NULL代表未删除，非NULL代表删除时间
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
