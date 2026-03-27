package com.zhilian.zhilianbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 服务商接取需求请求参数
 */
@Data
public class AcceptDemandRequest {
    @NotNull(message = "需求ID不能为空")
    private Long demandId;

    @NotNull(message = "服务商企业ID不能为空")
    private Long serviceId;
}