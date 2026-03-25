package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AcceptDemandRequest;
import com.zhilian.zhilianbackend.dto.response.AcceptDemandResult;
import com.zhilian.zhilianbackend.dto.response.DemandMarketVO;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25
 * @Description: 需求模块控制器，提供市场需求列表、接取需求等接口
 */
@Slf4j
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
@Tag(name = "需求模块")
public class DemandController {

    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: keyword 标题关键词
     * @Param: tagIds 标签ID，逗号分隔
     * @Param: expectedBudgetMin 最小预算
     * @Param: expectedBudgetMax 最大预算
     * @Param: deadlineStart 截止日期开始范围
     * @Param: deadlineEnd 截止日期结束范围
     * @Return: 分页的市场需求列表
     * @Description: 获取合作市场需求列表，仅服务商或管理员可访问
     */
    @GetMapping("/market")
    @Operation(summary = "获取合作市场需求列表")
    public Result<PageResult<DemandMarketVO>> getMarketDemands(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) BigDecimal expectedBudgetMin,
            @RequestParam(required = false) BigDecimal expectedBudgetMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineEnd) {

        // 权限校验：服务商或管理员
        String role = securityUtils.getCurrentUserRole();
        if (!"service".equals(role) && !securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 解析标签ID列表
        List<Long> tagIdList = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            try {
                tagIdList = java.util.Arrays.stream(tagIds.split(","))
                        .map(Long::parseLong)
                        .toList();
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "标签ID格式错误");
            }
        }

        PageResult<DemandMarketVO> result = demandService.pageMarketDemands(
                page, size, keyword, tagIdList,
                expectedBudgetMin, expectedBudgetMax,
                deadlineStart, deadlineEnd
        );
        return Result.success(result);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: request 接取需求请求参数（包含需求ID、服务商企业ID）
     * @Return: 新创建的合作记录ID
     * @Description: 服务商接取需求，使用行锁防止并发，成功后创建合作记录
     */
    @PostMapping("/accept")
    @Operation(summary = "服务商接取需求")
    public Result<AcceptDemandResult> acceptDemand(@Valid @RequestBody AcceptDemandRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        String role = securityUtils.getCurrentUserRole();
        if (!"service".equals(role)) {
            throw new BusinessException(403, "只有服务商可以接取需求");
        }

        Long cooperationId = demandService.acceptDemand(
                request.getDemandId(),
                request.getServiceId(),
                currentUserId
        );
        return Result.success(AcceptDemandResult.builder().cooperationId(cooperationId).build());
    }
}