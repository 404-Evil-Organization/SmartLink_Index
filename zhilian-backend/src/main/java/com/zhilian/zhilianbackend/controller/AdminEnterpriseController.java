package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.EnterpriseApproveRequest;
import com.zhilian.zhilianbackend.dto.request.EnterprisePendingRequest;
import com.zhilian.zhilianbackend.dto.response.PendingEnterpriseResponse;
import com.zhilian.zhilianbackend.service.AdminEnterpriseService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:38
 * @Param: 
 * @Return: 
 * @Description: 管理员企业审核控制器，提供企业审核相关接口
**/
@Slf4j
@RestController
@RequestMapping("/admin/enterprise")
@RequiredArgsConstructor
@Tag(name = "企业审核接口", description = "管理员企业审核相关接口")
public class AdminEnterpriseController {

    private final AdminEnterpriseService adminEnterpriseService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: 6017
     * @Date: 2026/3/24 23:39
     * @Param: request 分页查询请求参数
     * @Return: Result<IPage<PendingEnterpriseResponse>> 待审核企业列表
     * @Description: 获取待审核企业列表，包含制造企业和服务商
    **/
    @GetMapping("/pending")
    @Operation(summary = "获取待审核企业列表")
    public Result<IPage<PendingEnterpriseResponse>> getPendingEnterpriseList(
            @Valid EnterprisePendingRequest request) {
        log.info("获取待审核企业列表: page={}, size={}", request.getPage(), request.getSize());
        IPage<PendingEnterpriseResponse> page = adminEnterpriseService.getPendingEnterpriseList(
                request.getPage(), request.getSize());
        return Result.success(page);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/24 23:39
     * @Param: id 企业ID  request 审核请求参数（包含企业类型、审核状态、审核意见）
     * @Return: Result<Void> 审核结果
     * @Description: 审核企业，支持通过或驳回，需管理员权限
    **/
    @PostMapping("/approve/{id}")
    @Operation(summary = "审核企业")
    public Result<Void> approveEnterprise(
            @PathVariable Long id,
            @Valid @RequestBody EnterpriseApproveRequest request) {

        log.info("审核企业: enterpriseId={}, type={}, status={}, remark={}",
                id, request.getType(), request.getStatus(), request.getRemark());

        // 获取当前管理员ID
        Long auditUserId = securityUtils.getCurrentUserId();

        adminEnterpriseService.approveEnterprise(
                id,
                request.getType(),
                request.getStatus(),
                request.getRemark(),
                auditUserId
        );

        return Result.success();
    }
}