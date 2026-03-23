package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录列表项视图对象
 */
@Data
@Schema(description = "合作记录列表项")
public class CooperationRecordVO {

    @Schema(description = "合作记录ID")
    private Long id;

    @Schema(description = "合作对方企业名称")
    private String opponentName;

    @Schema(description = "需求标题")
    private String demandTitle;

    @Schema(description = "合同金额（万元）")
    private BigDecimal amount;

    @Schema(description = "合作开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Schema(description = "合作结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Schema(description = "合作状态：ongoing/completed/cancelled")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "当前用户是否已评价")
    private Boolean hasEvaluated;
}