package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "合作记录列表请求参数")
public class CooperationListRequest {

    @Schema(description = "合作状态筛选：ongoing/completed/cancelled，默认返回所有")
    private String status;

    @Schema(description = "企业ID（制造企业ID或服务商ID），不传时自动关联当前用户的默认企业")
    private Long enterpriseId;

    @Schema(description = "页码，默认1")
    private Integer page = 1;

    @Schema(description = "每页条数，默认10")
    private Integer size = 10;
}