package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 19:00
 * @Description: 合作记录详情响应对象
 */
@Data
@Builder
@Schema(description = "合作记录详情响应")
public class CooperationDetailVO {

    @Schema(description = "合作记录ID")
    private Long id;

    @Schema(description = "制造企业ID")
    private Long manuId;

    @Schema(description = "制造企业名称")
    private String manuName;

    @Schema(description = "服务商ID")
    private Long serviceId;

    @Schema(description = "服务商名称")
    private String serviceName;

    @Schema(description = "需求ID")
    private Long demandId;

    @Schema(description = "需求标题")
    private String demandTitle;

    @Schema(description = "需求详情")
    private String demandDescription;

    @Schema(description = "合同金额（万元）")
    private BigDecimal amount;

    @Schema(description = "合作开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Schema(description = "合作结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Schema(description = "合作内容描述")
    private String description;

    @Schema(description = "合作状态：ongoing/completed/cancelled")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "当前用户是否已评价")
    private Boolean hasEvaluated;
}