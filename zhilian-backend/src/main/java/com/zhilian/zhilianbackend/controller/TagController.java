package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.service.TagService;
import com.zhilian.zhilianbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.zhilian.zhilianbackend.exception.BusinessException;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.zhilian.zhilianbackend.util.JwtUtil;
/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:55
 * @Param:
 * @Return:
 * @Description: 标签管理控制器，提供标签的增删改查接口
 **/
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
     * 校验当前登录用户是否为管理员。
     * 说明：
     * 1. 由于当前项目未启用 @EnableMethodSecurity，方法上的 @PreAuthorize 暂时不会生效，
     *    因此这里通过显式读取 SecurityContext 做一次兜底校验，避免任意携带 token 的用户越权调用管理接口。
     * 2. 优先根据 Authentication 中的 authorities 判断是否包含 ADMIN 角色；
     *    若 authorities 为空或未标识为管理员，则退回到基于 userId 查询数据库校验角色；
     * 3. 若无法确认当前用户为管理员，则一律按非管理员处理，抛出 403，避免放宽权限。
     */
    private void checkAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            // 显式拒绝匿名认证，避免匿名用户继续走后续 JWT/角色解析逻辑
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }

        // 从 principal 中提取当前登录用户的 userId
        Long userId = extractUserId(authentication.getPrincipal());
        if (userId == null) {
            // 无法识别当前用户身份，按未授权处理
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }

        boolean isAdmin = false;

        // 1. 优先从 authorities 中判断角色（适配标准 Spring Security 使用方式）
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority == null) {
                    continue;
                }
                String role = authority.getAuthority();
                if (role == null) {
                    continue;
                }
                String normalized = role.toUpperCase();
                if ("ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized)) {
                    isAdmin = true;
                    break;
                }
            }
        }

        // 2. 若 authorities 未标识为管理员，则基于 userId 查询数据库确认角色
        if (!isAdmin) {
            // 这里依赖 UserService 根据 userId 判断是否为管理员，实现细节在服务层完成
            isAdmin = userService.isAdmin(userId);
        }

        if (!isAdmin) {
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }
    }

        // 3. 若仍未标识为管理员，则从 details 中尝试解析 JWT 中的角色信息
        if (!isAdmin) {
            String detailRole = extractRoleFromObject(authentication.getDetails());
            if (detailRole != null) {
                String normalized = detailRole.toUpperCase();
                if ("ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized)) {
                    isAdmin = true;
                }
            }
        }

        // 4. 兜底方案：从当前请求头中的 JWT 解析角色，解决 JwtAuthenticationFilter 未注入 authorities 的问题
        if (!isAdmin) {
            String tokenRole = extractRoleFromJwtToken();
            if (tokenRole != null) {
                String normalized = tokenRole.toUpperCase();
                if ("ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized)) {
                    isAdmin = true;
                }
            }
        }

        if (!isAdmin) {
            // 安全兜底：无法确认管理员身份时，统一拒绝访问
            throw new BusinessException(403, "仅管理员可以执行该操作");
        }
    }

    /**
     * 通过 JWT Token 提取角色信息。
     *
     * 说明：ObjectMapper 通过 Spring 单例注入，避免每次 new 带来的性能开销，
     *       同时复用全局 Jackson 配置（时间格式、反序列化特性等）。
     */
    private final ObjectMapper objectMapper;

    /**
     * 从当前 HTTP 请求头中的 JWT（Authorization: Bearer xxx）中解析角色信息。
     *
     * 说明：
     * - 仅作为兜底逻辑使用，用于解决 JwtAuthenticationFilter 未正确注入 authorities 的场景；
     * - 通过 JwtUtil.parseToken(...) 对 JWT 做签名/过期等校验，避免直接 Base64 解码 payload 带来的伪造风险；
     * - 解析失败时返回 null，不抛出异常。
     *
     * @return 角色字符串（如 "ADMIN"、"ROLE_ADMIN"），解析失败返回 null
     */
    private String extractRoleFromJwtToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            if (request == null) {
                return null;
            }

            String authorization = request.getHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                return null;
            }

            String prefix = "Bearer ";
            if (!authorization.regionMatches(true, 0, prefix, 0, prefix.length())) {
                // 非 Bearer Token，直接返回
                return null;
            }

            String token = authorization.substring(prefix.length()).trim();
            if (token.isEmpty()) {
                return null;
            }

            // 使用统一的 JwtUtil 进行解析和签名/过期校验，避免直接 Base64 解码 payload 形成信任边界
            Object claims = JwtUtil.parseToken(token);

            // 复用已有的角色提取逻辑，从 claims 中抽取 role 信息
            return extractRoleFromObject(claims);
        } catch (Exception ex) {
            // 作为兜底逻辑，不因解析异常影响主流程，直接返回 null
            return null;
        }
    }

    /**
     * 从给定对象中提取角色信息。
     *
     * 兼容场景：
     * - 对象为 Map：从 key 为 "role"/"roles" 的字段读取；
     * - 对象为 String：直接视为角色字符串；
     * - 对象为自定义用户实体：通过反射调用 getRole()/getRoles() 方法获取角色字符串。
     *
     * @param source 可能包含角色信息的对象（principal 或 details）
     * @return 角色字符串（如 "ADMIN"、"ROLE_ADMIN"），无法解析则返回 null
     */
    private String extractRoleFromObject(Object source) {
        if (source == null) {
            return null;
        }

        // 场景一：JWT 解析后放入 Map 结构
        if (source instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) source;
            Object roleVal = map.get("role");
            if (roleVal == null) {
                roleVal = map.get("roles");
            }
            return roleVal != null ? String.valueOf(roleVal) : null;
        }

        // 场景二：直接为字符串
        if (source instanceof String) {
            return (String) source;
        }

        // 场景三：自定义用户对象，尝试通过反射读取 getRole()/getRoles()
        try {
            Method getRoleMethod;
            try {
                getRoleMethod = source.getClass().getMethod("getRole");
            } catch (NoSuchMethodException e) {
                getRoleMethod = source.getClass().getMethod("getRoles");
            }
            Object roleVal = getRoleMethod.invoke(source);
            if (roleVal != null) {
                return String.valueOf(roleVal);
            }
        } catch (Exception ignored) {
            // 反射失败不影响主流程，直接视为未找到角色信息
        }

        return null;
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Long> add(@RequestBody @Validated(TagRequest.Create.class) TagRequest request) {
        // 兜底管理员校验：在未启用方法级安全或 Jwt 未正确注入角色时，防止任意用户越权新增标签
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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated(TagRequest.Update.class) TagRequest request) {
        // 兜底管理员校验，防止非管理员用户修改标签信息
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 兜底管理员校验，防止非管理员用户逻辑删除标签
        checkAdmin();
        tagService.deleteTag(id);
        return Result.success();
    }
}
