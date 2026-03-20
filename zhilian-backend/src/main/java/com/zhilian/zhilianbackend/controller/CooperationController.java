package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CooperationListRequest;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.CooperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/cooperation")
@RequiredArgsConstructor
@Tag(name = "合作记录模块")
public class CooperationController {

    private final CooperationService cooperationService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return Long.parseLong(authentication.getName());
    }

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

    @GetMapping("/my-list")
    @Operation(summary = "获取我的合作记录列表")
    public Result<PageResult<CooperationRecordVO>> getMyCooperations(@ModelAttribute CooperationListRequest request) {
        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();
        PageResult<CooperationRecordVO> pageResult = cooperationService.pageMyCooperations(
                userId, userRole, request.getEnterpriseId(), request.getStatus(), request.getPage(), request.getSize()
        );
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取合作记录详情")
    public Result<CooperationDetailVO> getCooperationDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();
        CooperationDetailVO detail = cooperationService.getCooperationDetail(id, userId, userRole);
        return Result.success(detail);
    }
}