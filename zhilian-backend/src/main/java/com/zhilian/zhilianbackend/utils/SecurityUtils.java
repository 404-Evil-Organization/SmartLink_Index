package com.zhilian.zhilianbackend.utils;

import com.zhilian.zhilianbackend.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 17:55
 * @Description: 统一从 SecurityContext 中提取当前用户 ID 和角色，避免重复代码
 */
@Component
public class SecurityUtils {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 18:46
     * @Param:
     * @Return: Authentication 已认证的 Authentication 对象
     * @Description: 获取当前认证的 Authentication 对象，如果未认证或为匿名则抛出异常。
     */
    private Authentication getAuthenticatedAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return authentication;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 18:46
     * @Param:
     * @Return: Long 当前登录用户的 ID
     * @Description: 获取当前登录用户的 ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = getAuthenticatedAuthentication();
        String name = authentication.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(401, "登录信息无效，请重新登录");
        }
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录信息无效，请重新登录");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 18:46
     * @Param:
     * @Return: String 角色名称（小写，不带 ROLE_ 前缀）
     * @Description: 获取当前登录用户的角色，支持多角色场景下返回第一个有效角色。
     *              若未找到有效角色则抛出 403 异常。
     */
    public String getCurrentUserRole() {
        Authentication authentication = getAuthenticatedAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            throw new BusinessException(403, "无法获取用户角色");
        }

        Optional<String> roleOpt = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role != null && !role.trim().isEmpty())
                .findFirst();

        if (!roleOpt.isPresent()) {
            throw new BusinessException(403, "用户角色信息为空");
        }

        String roleWithPrefix = roleOpt.get();
        if (roleWithPrefix.startsWith("ROLE_")) {
            return roleWithPrefix.substring(5).toLowerCase();
        }
        return roleWithPrefix.toLowerCase();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Return: boolean true-当前用户为管理员，false-非管理员
     * @Description: 判断当前登录用户是否为管理员角色
     */
    public boolean isAdmin() {
        try {
            String role = getCurrentUserRole();
            return "admin".equals(role);
        } catch (BusinessException e) {
            return false;
        }
    }
}