package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;

/**
 * 取消合作请求参数（可选）
 */
@Data
public class CancelCooperationRequest {
    private String reason; // 取消原因，仅用于日志，不持久化
}