package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:46
 * @Param: 
 * @Return: 
 * @Description: 区域指数表实体类，对应region_index表
**/
@Getter
@Setter
@Accessors(chain = true)
@TableName("region_index")
public class RegionIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域名称（深圳/东莞/惠州/广州等）
     */
    @TableField("region")
    private String region;

    /**
     * 年份
     */
    @TableField("year")
    private Short year;

    /**
     * 统计周期类型：quarter季度、month月度
     */
    @TableField("period_type")
    private String periodType;

    /**
     * 周期值：季度1-4，月份1-12
     */
    @TableField("period_value")
    private Byte periodValue;

    /**
     * 合作密度（合作次数/企业总数）
     */
    @TableField("coop_density")
    private BigDecimal coopDensity;

    /**
     * 服务渗透率（使用服务企业数/制造企业总数）
     */
    @TableField("service_rate")
    private BigDecimal serviceRate;

    /**
     * 跨域协同度（跨区域合作次数/总合作次数）
     */
    @TableField("cross_rate")
    private BigDecimal crossRate;

    /**
     * 协同指数综合得分
     */
    @TableField("total_index")
    private BigDecimal totalIndex;

    /**
     * 计算时间
     */
    @TableField("calc_time")
    private Date calcTime;

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
