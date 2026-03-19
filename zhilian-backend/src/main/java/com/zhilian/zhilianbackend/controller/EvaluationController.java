package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.EvaluationSubmitResponse;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 评价模块控制器，提供提交评价接口
 */
@Slf4j
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
@Tag(name = "评价模块")
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param:
     * @Return: 当前登录用户ID
     * @Description: 从 SecurityContext 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return Long.parseLong(authentication.getName());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param:
     * @Return: 当前登录用户角色（小写，无 ROLE_ 前缀）
     * @Description: 从 authorities 中提取用户角色
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "请先登录");
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            throw new BusinessException(401, "无法获取用户角色");
        }
        String roleWithPrefix = authorities.iterator().next().getAuthority();
        if (roleWithPrefix.startsWith("ROLE_")) {
            return roleWithPrefix.substring(5).toLowerCase();
        }
        return roleWithPrefix.toLowerCase();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: request 评价请求参数
     * @Return: 包含评价ID的统一响应
     * @Description: 提交评价接口，仅制造企业可调用
     */
    @PostMapping("/submit")
    @Operation(summary = "提交评价")
    public Result<EvaluationSubmitResponse> submitEvaluation(@Valid @RequestBody EvaluationSubmitRequest request) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        if (!"manufacture".equals(role)) {
            return Result.forbidden("只有制造企业可以提交评价");
        }

        Long evaluationId = evaluationService.submitEvaluation(request, userId, role);
        return Result.success(new EvaluationSubmitResponse(evaluationId));
    }
}