package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 提交评价请求参数
 */
@Data
@Schema(description = "提交评价请求")
public class EvaluationSubmitRequest {

    @NotNull(message = "合作记录ID不能为空")
    @Schema(description = "合作记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long coopId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "评分（1-5星）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "是否匿名，默认false")
    private Boolean isAnonymous = false;
}