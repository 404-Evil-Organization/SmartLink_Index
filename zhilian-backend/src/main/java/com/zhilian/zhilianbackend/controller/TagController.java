package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.service.TagService;
import com.zhilian.zhilianbackend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collection;

//@Author: 周冠杰
@Tag(name = "标签管理接口")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;
    private final UserService userService;

    /**
     * 从 Authentication 的 principal 中尽可能提取当前登录用户的 userId。
     * <p>
     * 支持以下几种常见情况：
     * <ul>
     *     <li>principal 为 Long / Integer：直接作为 userId 使用；</li>
     *     <li>principal 为 String：尝试解析为 Long；</li>
     *     <li>principal 为自定义用户对象，且包含 getUserId() 方法：通过反射调用获取。</li>
     * </ul>
     * 提取失败时返回 null，由调用方决定是否拒绝访问。
     */
    private Long extractUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof Integer) {
            return ((Integer) principal).longValue();
        }
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        // 兼容自定义用户对象：优先尝试调用 getUserId() 方法
        try {
            Method getUserIdMethod = principal.getClass().getMethod("getUserId");
            Object userIdValue = getUserIdMethod.invoke(principal);
            if (userIdValue instanceof Number) {
                return ((Number) userIdValue).longValue();
            }
        } catch (Exception ignored) {
            // 忽略反射异常，返回 null 由上层处理
        }
        return null;
    }

    /**
     * 从 SecurityContext 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }
        return Long.parseLong(authentication.getName());
    }

    /**
     * 校验当前用户是否为管理员
     */
    private void checkAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 检查是否已登录
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "请先登录");
        }

        // 2. 检查权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            throw new BusinessException(403, "权限不足");
        }

        // 3. 检查是否包含 ADMIN 角色
        boolean isAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.toUpperCase())
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ADMIN"));

        if (!isAdmin) {
            log.warn("非管理员用户尝试访问管理接口");
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:44
     * @Param: queryRequest 分页查询参数（包含页码、每页条数、名称模糊查询、类别筛选）
     * @Return: Result<IPage<TagResponse>> 分页后的标签列表
     * @Description: 获取标签列表（分页），支持按名称模糊查询和按类别筛选
     **/
    @Operation(summary = "获取标签列表（分页）")
    @GetMapping("/list")
    public Result<IPage<TagResponse>> list(@Valid TagQueryRequest queryRequest) {
        return Result.success(tagService.pageQuery(queryRequest));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:57
     * @Param: id 标签ID
     * @Return: Result<TagResponse> 标签详情
     * @Description: 根据ID获取标签详细信息
     **/
    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<TagResponse> detail(@PathVariable Long id) {
        return Result.success(tagService.getTagDetail(id));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:57
     * @Param: request 新增标签请求参数
     * @Return: Result<Long> 新增标签的ID
     * @Description: 新增标签，会校验标签名称是否已存在
     **/
    @Operation(summary = "新增标签")
    @PostMapping
    public Result<Long> add(@RequestBody @Validated(TagRequest.Create.class) TagRequest request) {
        checkAdmin();
        return Result.success(tagService.addTag(request));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:58
     * @Param: id 要修改的标签ID;request 修改标签请求参数
     * @Return: Result<Void>
     * @Description: 修改标签信息，如果修改名称会检查新名称是否与其他标签冲突
     **/
    @Operation(summary = "修改标签")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Validated(TagRequest.Update.class) TagRequest request) {
        checkAdmin();
        tagService.updateTag(id, request);
        return Result.success();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:58
     * @Param: id 要删除的标签ID
     * @Return: Result<Void>
     * @Description: 逻辑删除标签
     **/
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        tagService.deleteTag(id);
        return Result.success();
    }
}