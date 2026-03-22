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
 * @Description: 统计卡片数据响应DTO
**/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    /**
     * 制造企业数量
     */
    private Long manufactureCount;

    /**
     * 服务商数量
     */
    private Long serviceCount;

    /**
     * 需求数量
     */
    private Long demandCount;

    /**
     * 合作数量
     */
    private Long cooperationCount;
}