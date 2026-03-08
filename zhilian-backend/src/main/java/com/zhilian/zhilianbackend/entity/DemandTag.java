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
 * 需求标签关系表
 * </p>
 *
 * @author 智链团队
 * @since 2026-03-08
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("demand_tag")
public class DemandTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联demand.id
     */
    @TableField("demand_id")
    private Long demandId;

    /**
     * 关联tag.id
     */
    @TableField("tag_id")
    private Long tagId;

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
