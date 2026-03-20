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
 * @Description: 热力图数据响应DTO
**/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDataResponse {
    /**
     * 区域名称
     */
    private String region;

    /**
     * 合作次数/指数值
     */
    private Integer value;
}