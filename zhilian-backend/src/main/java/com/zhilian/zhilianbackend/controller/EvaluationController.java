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

@Slf4j
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
@Tag(name = "评价模块")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final SecurityUtils securityUtils;

    @PostMapping("/submit")
    @Operation(summary = "提交评价")
    public Result<EvaluationSubmitResponse> submitEvaluation(@Valid @RequestBody EvaluationSubmitRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        String role = securityUtils.getCurrentUserRole();

        // 管理员可以直接评价，不受角色限制
        if (!securityUtils.isAdmin() && !"manufacture".equals(role)) {
            return Result.forbidden("只有制造企业可以提交评价");
        }

        Long evaluationId = evaluationService.submitEvaluation(request, userId, role);
        return Result.success(new EvaluationSubmitResponse(evaluationId));
    }
}