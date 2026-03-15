package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderAddVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderDetailVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderListVO;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 01:02
 * @Param:
 * @Return:
 * @Description: 服务商管理 Controller
 **/
@Slf4j
@RestController
@RequestMapping("/service-provider")
@RequiredArgsConstructor
@Tag(name = "服务商管理", description = "服务商相关接口")
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;


    /**
     * 从 Spring Security 上下文中获取当前登录用户的 ID。
     * 约定：认证成功后，用户的主标识（如 userId）存放在 Authentication 的 name 或 UserDetails.username 中，
     * 且可以被解析为 Long 类型。
     */
    private Long getCurrentUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("未登录或登录状态已失效，禁止访问该接口");
        }
        Object principal = authentication.getPrincipal();
        String identifier;
        if (principal instanceof UserDetails userDetails) {
            identifier = userDetails.getUsername();
        } else {
            identifier = authentication.getName();
        }
        try {
            return Long.parseLong(identifier);
        } catch (NumberFormatException ex) {
            log.error("无法从认证信息中解析当前用户ID，identifier={}", identifier, ex);
            throw new AccessDeniedException("无法识别当前用户身份，禁止访问该接口");
        }
    }
    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: requestDTO 查询请求参数
     * @Return: Result<IPage<ServiceProviderListVO>> 分页列表结果
     * @Description: 获取服务商列表
     **/
    @GetMapping("/list")
    @Operation(summary = "获取服务商列表", description = "分页查询服务商列表，支持区域、服务大类筛选")
    public Result<IPage<ServiceProviderListVO>> getServiceProviderList(ServiceProviderListRequestDTO requestDTO) {
        // 为避免敏感信息（如联系人电话）落盘，这里不再直接打印完整请求 DTO
        log.info("获取服务商列表，请求参数已接收");
        IPage<ServiceProviderListVO> pageResult = serviceProviderService.getServiceProviderList(requestDTO);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Return: Result<ServiceProviderDetailVO> 服务商详情
     * @Description: 获取服务商详情
     **/
    @GetMapping("/{id}")
    @Operation(summary = "获取服务商详情", description = "根据ID获取服务商详细信息")
    public Result<ServiceProviderDetailVO> getServiceProviderDetail(
            @Parameter(description = "服务商ID", required = true, example = "2001")
            @PathVariable("id") Long id) {
        log.info("获取服务商详情，服务商ID：{}", id);
        ServiceProviderDetailVO detail = serviceProviderService.getServiceProviderDetail(id);
        return Result.success(detail);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param:  requestDTO 新增服务商请求参数（userId 将被服务端根据当前登录用户强制覆盖）
     * @Return: Result<ServiceProviderAddVO> 新增结果（返回新ID）
     * @Description: 新增服务商，仅允许服务商角色或管理员调用
     **/
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'service')")
    @Operation(summary = "新增服务商", description = "创建新的服务商信息")
    public Result<ServiceProviderAddVO> addServiceProvider(@Valid @RequestBody ServiceProviderAddRequestDTO requestDTO) {
        // 从当前登录用户的认证信息中获取 userId，防止客户端伪造 userId 越权创建服务商
        Long currentUserId = getCurrentUserIdFromSecurityContext();
        requestDTO.setUserId(currentUserId);
        // 为保护联系人电话等敏感信息，不在日志中直接输出完整请求 DTO
        log.info("新增服务商，请求参数已接收");
        ServiceProviderAddVO result = serviceProviderService.addServiceProvider(requestDTO);
        return Result.success("新增成功", result);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Param: requestDTO 修改服务商请求参数
     * @Return: Result<Void> 修改结果
     * @Description: 修改服务商信息，仅允许管理员或服务商自身修改
     **/
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'service')")
    @Operation(summary = "修改服务商", description = "根据ID修改服务商信息，只传需要修改的字段，仅允许管理员或服务商自身修改")
    public Result<Void> updateServiceProvider(
            @Parameter(description = "服务商ID", required = true, example = "2010")
            @PathVariable("id") Long id,
            @Valid @RequestBody ServiceProviderUpdateRequestDTO requestDTO) {
        // 获取当前登录用户ID，传递给service层进行权限校验
        Long currentUserId = getCurrentUserIdFromSecurityContext();
        log.info("修改服务商，服务商ID：{}，操作人ID：{}", id, currentUserId);
        serviceProviderService.updateServiceProvider(id, requestDTO, currentUserId);
        return Result.success("修改成功");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Return: Result<Void> 删除结果
     * @Description: 删除服务商（逻辑删除），仅允许管理员或服务商自身删除
     **/
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'service')")
    @Operation(summary = "删除服务商", description = "根据ID删除服务商（逻辑删除），仅允许管理员或服务商自身删除")
    public Result<Void> deleteServiceProvider(
            @Parameter(description = "服务商ID", required = true, example = "2010")
            @PathVariable("id") Long id) {
        // 获取当前登录用户ID，传递给service层进行权限校验
        Long currentUserId = getCurrentUserIdFromSecurityContext();
        log.info("删除服务商，服务商ID：{}，操作人ID：{}", id, currentUserId);
        serviceProviderService.deleteServiceProvider(id, currentUserId);
        return Result.success("删除成功");
    }
}