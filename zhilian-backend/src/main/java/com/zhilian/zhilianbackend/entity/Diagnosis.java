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
 * @Date: 2026/3/9 21:46
 * @Param: 
 * @Return: 
 * @Description: 诊断记录表实体类，对应diagnosis表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("diagnosis")
public class Diagnosis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 诊断唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联manufacture.id
     */
    @TableField("manu_id")
    private Long manuId;

    /**
     * 信息化得分（1-5）
     */
    @TableField("info_score")
    private Byte infoScore;

    /**
     * 自动化得分（1-5）
     */
    @TableField("auto_score")
    private Byte autoScore;

    /**
     * 数据应用得分（1-5）
     */
    @TableField("data_score")
    private Byte dataScore;

    /**
     * 服务协同得分（1-5）
     */
    @TableField("service_score")
    private Byte serviceScore;

    /**
     * 总分（0-100）
     */
    @TableField("total_score")
    private Byte totalScore;

    /**
     * 等级（起步期/成长期/成熟期/引领期）
     */
    @TableField("level")
    private String level;

    /**
     * 改进建议（可JSON存储多条）
     */
    @TableField("suggestions")
    private String suggestions;

    /**
     * 诊断日期
     */
    @TableField("diagnosis_date")
    private Date diagnosisDate;

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
