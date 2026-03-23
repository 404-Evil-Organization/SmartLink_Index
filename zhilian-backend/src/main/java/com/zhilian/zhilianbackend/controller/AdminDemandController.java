package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DemandApproveRequest;
import com.zhilian.zhilianbackend.dto.response.DemandPendingVO;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026/03/23
 * @Description: 管理员需求审核控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/demand")
@RequiredArgsConstructor
@Validated // 启用方法参数校验
public class AdminDemandController {

    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: page 页码，默认1，最小1
     * @Param: size 每页条数，默认10，最小1，最大100
     * @Return: Result<PageResult<DemandPendingVO>> 分页的待审核需求列表
     * @Description: 获取待审核需求列表，仅管理员可访问
     */
    @GetMapping("/pending")
    public Result<PageResult<DemandPendingVO>> getPendingDemandList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer size) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        PageResult<DemandPendingVO> pageResult = demandService.getPendingDemandList(page, size);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: id 需求ID
     * @Param: request 审核请求参数
     * @Return: Result<Void> 无数据返回
     * @Description: 审核需求（通过/驳回），仅管理员可操作
     */
    @PostMapping("/approve/{id}")
    public Result<Void> approveDemand(@PathVariable Long id,
                                      @Valid @RequestBody DemandApproveRequest request) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        Long adminUserId = securityUtils.getCurrentUserId();
        demandService.approveDemand(id, request, adminUserId);
        return Result.success();
    }
}