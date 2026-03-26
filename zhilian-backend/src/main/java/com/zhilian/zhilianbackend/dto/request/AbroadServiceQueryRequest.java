package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "出海服务商查询请求")
public class AbroadServiceQueryRequest {

    @Schema(description = "服务类型（如：检测认证、物流供应链）", example = "检测认证")
    private String serviceType;
}