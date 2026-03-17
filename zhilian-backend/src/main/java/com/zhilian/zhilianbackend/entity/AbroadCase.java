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
 * @Description: 出海成功案例表实体类，对应abroad_case表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("abroad_case")
public class AbroadCase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 案例唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 案例标题
     */
    @TableField("title")
    private String title;

    /**
     * 企业名称（可为制造企业或服务商）
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 企业类型
     */
    @TableField("company_type")
    private String companyType;

    /**
     * 目标国家
     */
    @TableField("country")
    private String country;

    /**
     * 涉及服务类型（如CE认证、物流）
     */
    @TableField("service_type")
    private String serviceType;

    /**
     * 案例详情
     */
    @TableField("description")
    private String description;

    /**
     * 封面图URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 状态：0草稿 1发布
     */
    @TableField("status")
    private Byte status;

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
