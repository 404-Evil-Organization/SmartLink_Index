package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.response.DemandPublishResponse;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026/03/23
 * @Description: 需求相关控制器
 */
@Slf4j
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: request 发布需求请求参数
     * @Return: Result<DemandPublishResponse> 包含需求ID和审核状态
     * @Description: 发布需求，仅制造企业可操作
     */
    @PostMapping("/publish")
    public Result<DemandPublishResponse> publishDemand(@Valid @RequestBody DemandPublishRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        DemandPublishResponse response = demandService.publishDemand(request, userId);
        return Result.success(response);
    }
}