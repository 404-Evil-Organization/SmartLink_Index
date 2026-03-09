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
 * @Date: 2026/3/9 21:47
 * @Param: 
 * @Return: 
 * @Description: 信用分记录表实体类，对应credit_score表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("credit_score")
public class CreditScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联service_provider.id
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 综合信用分（0-100）
     */
    @TableField("score")
    private Byte score;

    /**
     * 资质分
     */
    @TableField("qual_score")
    private Byte qualScore;

    /**
     * 案例分
     */
    @TableField("case_score")
    private Byte caseScore;

    /**
     * 评价分
     */
    @TableField("eval_score")
    private Byte evalScore;

    /**
     * 计算时间
     */
    @TableField("calc_time")
    private Date calcTime;

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
