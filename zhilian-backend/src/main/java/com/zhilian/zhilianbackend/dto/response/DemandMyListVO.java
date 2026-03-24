package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "我的需求列表项")
public class DemandMyListVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "需求标题")
    private String title;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "预算金额（万元）")
    private BigDecimal expectedBudget;

    @Schema(description = "期望完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    @Schema(description = "业务状态：draft/published/matched/closed")
    private String status;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "标签列表")
    private List<TagSimpleVO> tags;

    @Data
    @Schema(description = "标签简化信息")
    public static class TagSimpleVO {
        private Long id;
        private String name;
    }
}