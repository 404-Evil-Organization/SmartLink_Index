package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "需求详情")
public class DemandDetailVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "制造企业ID")
    private Long manuId;

    @Schema(description = "需求标题")
    private String title;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "预算金额（万元）")
    private BigDecimal expectedBudget;

    @Schema(description = "期望完成日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deadline;

    @Schema(description = "业务状态：draft/published/matched/closed")
    private String status;

    @Schema(description = "审核状态：pending/approved/rejected")
    private String auditStatus;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "制造企业信息")
    private ManufactureInfo manufacture;

    @Schema(description = "标签列表")
    private List<TagSimpleVO> tags;

    @Data
    @Schema(description = "制造企业信息")
    public static class ManufactureInfo {
        private Long id;
        private String companyName;
        private String region;
        private String contactPerson;
        private String contactPhone;
    }

    @Data
    @Schema(description = "标签简化信息")
    public static class TagSimpleVO {
        private Long id;
        private String name;
    }
}
