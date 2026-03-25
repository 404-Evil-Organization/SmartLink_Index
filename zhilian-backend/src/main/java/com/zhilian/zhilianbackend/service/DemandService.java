package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.DemandMarketVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25
 * @Description: 需求业务逻辑接口
 */
public interface DemandService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: keyword 标题关键词
     * @Param: tagIds 标签ID列表
     * @Param: budgetMin 最小预算
     * @Param: budgetMax 最大预算
     * @Param: deadlineStart 截止日期开始范围
     * @Param: deadlineEnd 截止日期结束范围
     * @Return: 分页的市场需求列表
     * @Description: 分页查询市场需求列表（已审核通过且已发布的需求）
     */
    PageResult<DemandMarketVO> pageMarketDemands(
            Integer page, Integer size,
            String keyword, List<Long> tagIds,
            BigDecimal budgetMin, BigDecimal budgetMax,
            LocalDate deadlineStart, LocalDate deadlineEnd
    );

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: demandId 需求ID
     * @Param: serviceId 服务商企业ID
     * @Param: currentUserId 当前用户ID
     * @Return: 新创建的合作记录ID
     * @Description: 服务商接取需求，使用行锁防止并发
     */
    Long acceptDemand(Long demandId, Long serviceId, Long currentUserId);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: demandId 需求ID
     * @Return: 无
     * @Description: 将需求状态重置为已发布（用于取消合作时）
     */
    void resetDemandStatusToPublished(Long demandId);
}