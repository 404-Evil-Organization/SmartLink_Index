package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录列表请求参数
 */
@Data
@Schema(description = "合作记录列表请求参数")
public class CooperationListRequest {

    @Schema(description = "合作状态筛选：ongoing/completed/cancelled，默认返回所有")
    private String status;

    @Schema(description = "页码，默认1")
    private Integer page = 1;

    @Schema(description = "每页条数，默认10")
    private Integer size = 10;
}