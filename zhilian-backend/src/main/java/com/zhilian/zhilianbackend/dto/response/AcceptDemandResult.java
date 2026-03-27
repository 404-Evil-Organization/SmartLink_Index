package com.zhilian.zhilianbackend.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 接取需求响应结果
 */
@Data
@Builder
public class AcceptDemandResult {
    private Long cooperationId;
}