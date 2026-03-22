package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:46
 * @Param:
 * @Return:
 * @Description: 热门需求响应DTO
**/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDemandResponse {
    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 需求数量
     */
    private Integer count;
}