package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CooperationListRequest;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.service.CooperationService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cooperation")
@RequiredArgsConstructor
@Tag(name = "合作记录模块")
public class CooperationController {

    private final CooperationService cooperationService;
    private final SecurityUtils securityUtils; // 注入

    // ... getCurrentUserId, getCurrentUserRole 方法可以移除，改用 SecurityUtils

    @GetMapping("/my-list")
    @Operation(summary = "获取我的合作记录列表")
    public Result<PageResult<CooperationRecordVO>> getMyCooperations(@ModelAttribute CooperationListRequest request) {
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

    @GetMapping("/{id}")
    @Operation(summary = "获取合作记录详情")
    public Result<CooperationDetailVO> getCooperationDetail(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        String userRole = securityUtils.getCurrentUserRole();

        // 管理员可以直接查看任何详情
        if (securityUtils.isAdmin()) {
            CooperationDetailVO detail = cooperationService.getCooperationDetailAdmin(id);
            return Result.success(detail);
        }

        CooperationDetailVO detail = cooperationService.getCooperationDetail(id, userId, userRole);
        return Result.success(detail);
    }
}