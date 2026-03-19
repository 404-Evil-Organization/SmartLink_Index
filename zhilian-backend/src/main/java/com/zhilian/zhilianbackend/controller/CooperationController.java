package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CooperationListRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/cooperation")
@RequiredArgsConstructor
@Tag(name = "合作记录模块")
public class CooperationController {

    private final CooperationService cooperationService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 17:30
     * @return 当前登录用户ID
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
     * @Date: 2026/3/19 17:30
     * @return 当前登录用户角色（小写，无 ROLE_ 前缀）
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
     * @Date: 2026/3/19 17:30
     * @Param: request 查询参数
     * @return 分页合作记录列表
     * @Description: 获取当前用户的合作记录列表（分页）
     */
    @GetMapping("/my-list")
    @Operation(summary = "获取我的合作记录列表")
    public Result<PageResult<CooperationRecordVO>> getMyCooperations(@ModelAttribute CooperationListRequest request) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        PageResult<CooperationRecordVO> pageResult = cooperationService.pageMyCooperations(
                userId, role, request.getStatus(), request.getPage(), request.getSize()
        );
        return Result.success(pageResult);
    }
}