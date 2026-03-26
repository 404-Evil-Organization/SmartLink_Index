package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25
 * @Description: 市场需求列表项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "市场需求列表项")
public class DemandMarketVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "需求标题")
    private String title;

    @Schema(description = "需求描述")
    private String description;

    @Schema(description = "预算金额（万元）")
    private BigDecimal expectedBudget;

    @Schema(description = "期望完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deadline;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "发布企业信息")
    private ManufactureListVO manufacture;

    @Schema(description = "需求标签列表")
    private List<TagResponse> tags;
}