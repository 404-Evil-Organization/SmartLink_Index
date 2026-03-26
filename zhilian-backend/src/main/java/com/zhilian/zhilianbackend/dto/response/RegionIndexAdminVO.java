package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "区域指数管理响应VO")
public class RegionIndexAdminVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "区域")
    private String region;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "周期类型")
    private String periodType;

    @Schema(description = "周期值")
    private Integer periodValue;

    @Schema(description = "合作密度")
    private BigDecimal coopDensity;

    @Schema(description = "服务渗透率")
    private BigDecimal serviceRate;

    @Schema(description = "跨域协同度")
    private BigDecimal crossRate;

    @Schema(description = "综合得分")
    private BigDecimal totalIndex;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计算时间")
    private Date calcTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;
}