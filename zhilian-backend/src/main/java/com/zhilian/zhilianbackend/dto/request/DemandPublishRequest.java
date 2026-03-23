package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "发布需求请求参数")
public class DemandPublishRequest {

    @NotBlank(message = "需求标题不能为空")
    @Schema(description = "需求标题", example = "寻求PCB设计服务")
    private String title;

    @Schema(description = "详细描述", example = "需要专业PCB设计公司，有高速PCB设计经验者优先。")
    private String description;

    @Range(min = 0, message = "预算必须大于等于0")
    @Schema(description = "预算金额（万元）", example = "10.0")
    private BigDecimal expectedBudget;

    @Schema(description = "期望完成日期", example = "2026-04-01")
    private Date deadline;

    @Schema(description = "标签名称列表", example = "[\"检测认证\", \"PCB电路板\"]")
    private List<String> tags;  // 注意：由 Long 类型改为 String 类型
}