package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.EvaluationSubmitResponse;
import com.zhilian.zhilianbackend.service.EvaluationService;
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
 * @Description: 评价模块控制器，提供提交评价接口
 */
@Slf4j
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
@Tag(name = "评价模块")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: request 评价提交请求参数（包含合作ID、评分、内容、是否匿名）
     * @Return: 评价ID
     * @Description: 提交评价，仅制造企业可评价，管理员禁止评价（需使用企业账号）
     */
    @PostMapping("/submit")
    @Operation(summary = "提交评价")
    public Result<EvaluationSubmitResponse> submitEvaluation(@Valid @RequestBody EvaluationSubmitRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        String role = securityUtils.getCurrentUserRole();

        // 评价人角色（用于入库），只能是 manufacture 或 service
        String evaluatorRole;
        // 出于数据准确性考虑，禁止管理员直接写入评价表，避免占用真实企业评价名额
        if (securityUtils.isAdmin()) {
            return Result.forbidden("管理员不能直接提交评价，请使用企业账号登录后再评价");
        } else {
            // 非管理员仅允许制造企业提交评价
            if (!"manufacture".equals(role)) {
                return Result.forbidden("只有制造企业可以提交评价");
            }
            // 此处 role 一定为 manufacture，将其作为评价人角色入库
            evaluatorRole = role;
        }
        Long evaluationId = evaluationService.submitEvaluation(request, userId, evaluatorRole);
        return Result.success(new EvaluationSubmitResponse(evaluationId));
    }
}