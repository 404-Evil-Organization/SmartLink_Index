package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AcceptDemandRequest;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.request.DemandUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.*;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
@Tag(name = "需求模块")
@Validated
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
    @GetMapping("/market/list")
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

        // 分页参数合法性校验与范围限制，防止 page/size 为 0、负数或过大导致分页异常或一次性返回过多数据
        if (page == null || page < 1) {
            log.warn("收到非法分页参数 page: {}，已重置为 1", page);
            page = 1;
        }
        if (size == null || size <= 0) {
            log.warn("收到非法分页参数 size: {}，已重置为默认值 10", size);
            size = 10;
        }
        // 可以根据项目统一规范调整最大分页大小，这里以 100 作为示例上限
        int maxPageSize = 100;
        if (size > maxPageSize) {
            log.warn("收到超大分页参数 size: {}，已限制为最大值 {}", size, maxPageSize);
            size = maxPageSize;
        }

        // 权限校验：服务商或管理员
        String role = securityUtils.getCurrentUserRole();
        if (!"service".equals(role) && !securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 区间参数合法性校验
        if (expectedBudgetMin != null && expectedBudgetMax != null && expectedBudgetMin.compareTo(expectedBudgetMax) > 0) {
            throw new BusinessException(400, "预算最小值不能大于最大值");
        }
        if (deadlineStart != null && deadlineEnd != null && deadlineStart.isAfter(deadlineEnd)) {
            throw new BusinessException(400, "截止日期开始范围不能大于结束范围");
        }

        // 解析标签ID列表（支持带空格、尾逗号等格式）
        List<Long> tagIdList = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            try {
                tagIdList = Arrays.stream(tagIds.split(","))
                        .map(String::trim)                 // 去除前后空格
                        .filter(s -> !s.isEmpty())         // 过滤空字符串（如 "1,2," 中的最后一个空串）
                        .map(Long::parseLong)              // 转换为 Long
                        .distinct()                        // 去重
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "标签ID格式错误，请使用数字ID，用英文逗号分隔");
            }
        }

        PageResult<DemandMarketVO> result = demandService.pageMarketDemands(
                page, size, keyword, tagIdList,
                expectedBudgetMin, expectedBudgetMax,
                deadlineStart, deadlineEnd
        );
        return Result.success(result);}

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: request 发布需求请求参数
     * @Return: Result<DemandPublishResponse> 包含需求ID和审核状态
     * @Description: 发布需求，仅制造企业可操作，且只能为自己的企业发布
     */
    @PostMapping("/publish")
    public Result<DemandPublishResponse> publishDemand(@Valid @RequestBody DemandPublishRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        DemandPublishResponse response = demandService.publishDemand(request, userId);
        return Result.success(response);
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
        return Result.success(AcceptDemandResult.builder().cooperationId(cooperationId).build());}
     
    /*
    * @Date: 2026/03/24
     * @Param: id 需求ID
     * @Param: request 编辑需求请求参数
     * @Return: Result<Void> 无数据返回
     * @Description: 编辑需求，仅制造企业可操作自己发布的需求，管理员可操作任意需求
     */
    @PutMapping("/{id}")
    public Result<Void> updateDemand(@PathVariable Long id,
                                     @Valid @RequestBody DemandUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.updateDemand(id, request, userId);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: id 需求ID
     * @Return: Result<Void> 无数据返回
     * @Description: 逻辑删除需求，仅制造企业可删除自己发布的需求，管理员可删除任意需求
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDemand(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.deleteDemand(id, userId);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: page 页码，最小1
     * @Param: size 每页条数，最小1，最大100
     * @Param: manuId 制造企业ID
     * @Param: status 需求状态筛选（可选）
     * @Return: Result<PageResult<DemandMyListVO>> 分页的需求列表
     * @Description: 获取当前用户的需求列表，制造企业只能查看自己的需求，管理员可查看任意企业需求
     */
    @GetMapping("/my-list")
    public Result<PageResult<DemandMyListVO>> getMyDemandList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer size,
            @RequestParam Long manuId,
            @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        PageResult<DemandMyListVO> pageResult = demandService.getMyDemandList(page, size, manuId, status, userId);
        return Result.success(pageResult);
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/26 18:29
     * @Param: id 需求ID
     * @Return: Result<DemandDetailVO> 需求详情
     * @Description: 获取需求详情
     */
    @GetMapping("/{id}")
    public Result<DemandDetailVO> getDemandDetail(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        return Result.success(demandService.getDemandDetail(id, userId));
    }
}