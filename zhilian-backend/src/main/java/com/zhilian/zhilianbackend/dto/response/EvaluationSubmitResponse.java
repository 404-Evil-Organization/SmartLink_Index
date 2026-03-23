package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 提交评价响应对象
 */
@Data
@AllArgsConstructor
@Schema(description = "提交评价响应")
public class EvaluationSubmitResponse {
    @Schema(description = "评价ID")
    private Long evaluationId;
}