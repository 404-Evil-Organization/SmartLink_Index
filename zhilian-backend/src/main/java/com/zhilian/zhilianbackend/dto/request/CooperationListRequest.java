package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 19:00
 * @Description: 合作记录列表请求参数
 */
@Data
@Schema(description = "合作记录列表请求参数")
public class CooperationListRequest {

    @Schema(description = "合作状态筛选：ongoing/completed/cancelled，默认返回所有")
    private String status;

    @Schema(description = "企业ID（制造企业ID或服务商ID），不传时自动关联当前用户的默认企业")
    private Long enterpriseId;

    @Min(value = 1, message = "页码最小为1")
    @Max(value = 1000, message = "页码最大为1000")
    @Schema(description = "页码，默认1")
    private int page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数，默认10")
    private int size = 10;
}