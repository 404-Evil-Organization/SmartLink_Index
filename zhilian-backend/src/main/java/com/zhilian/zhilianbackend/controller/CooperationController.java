package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CancelCooperationRequest;
import com.zhilian.zhilianbackend.dto.request.CooperationListRequest;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.service.CooperationService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 19:00
 * @Description: 合作记录模块控制器，提供合作记录列表和详情查询接口
 */
@Slf4j
@RestController
@RequestMapping("/cooperation")
@RequiredArgsConstructor
@Tag(name = "合作记录模块")
public class CooperationController {

    private final CooperationService cooperationService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: request 合作记录列表请求参数（包含企业ID、状态、分页信息）
     * @Return: 分页的合作记录列表
     * @Description: 获取当前用户的合作记录列表，管理员可查看所有合作或按企业筛选
     */
    @GetMapping("/my-list")
    @Operation(summary = "获取我的合作记录列表")
    public Result<PageResult<CooperationRecordVO>> getMyCooperations(@Valid @ModelAttribute CooperationListRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        String userRole = securityUtils.getCurrentUserRole();

        // 管理员：可查看所有合作，或按指定企业查看
        if (securityUtils.isAdmin()) {
            PageResult<CooperationRecordVO> pageResult = cooperationService.pageMyCooperationsAdmin(
                    userId, request.getEnterpriseId(), request.getStatus(), request.getPage(), request.getSize()
            );
            return Result.success(pageResult);
        }

        // 非管理员：原逻辑
        PageResult<CooperationRecordVO> pageResult = cooperationService.pageMyCooperations(
                userId, userRole, request.getEnterpriseId(), request.getStatus(), request.getPage(), request.getSize()
        );
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: id 合作记录ID
     * @Return: 合作记录详情
     * @Description: 获取合作记录详情，非管理员只能查看自己参与的合作，管理员可查看任意
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取合作记录详情")
    public Result<CooperationDetailVO> getCooperationDetail(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        String userRole = securityUtils.getCurrentUserRole();

        // 管理员可以直接查看任何详情
        if (securityUtils.isAdmin()) {
            CooperationDetailVO detail = cooperationService.getCooperationDetailAdmin(id, userId);
            return Result.success(detail);
        }

        CooperationDetailVO detail = cooperationService.getCooperationDetail(id, userId, userRole);
        return Result.success(detail);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: id 合作记录ID
     * @Param: request 取消原因（可选）
     * @Return: 无返回数据
     * @Description: 取消合作，仅合作双方或管理员可操作，取消后将关联需求状态恢复为已发布
     */
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消合作")
    public Result<Void> cancelCooperation(
            @PathVariable Long id,
            @RequestBody(required = false) CancelCooperationRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        String currentUserRole = securityUtils.getCurrentUserRole();

        if (request != null && request.getReason() != null) {
            log.info("用户取消合作，合作ID: {}, 原因: {}", id, request.getReason());
        }

        cooperationService.cancelCooperation(id, currentUserId, currentUserRole);
        return Result.success();
    }
}