package com.zhilian.zhilianbackend.utils;

import com.zhilian.zhilianbackend.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 17:55
 * @Param:
 * @Return:
 * @Description: 统一从 SecurityContext 中提取当前用户 ID 和角色，避免重复代码
 *  */
@Component
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户 ID
     * @throws BusinessException 如果未登录或用户信息无效
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
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
     * 获取当前登录用户的角色
     *
     * @return 角色名称（小写，不带 ROLE_ 前缀）
     * @throws BusinessException 如果未登录或无法获取角色
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 与 getCurrentUserId() 保持一致：排除匿名登录态，避免返回 ROLE_ANONYMOUS 误导业务逻辑
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
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
}