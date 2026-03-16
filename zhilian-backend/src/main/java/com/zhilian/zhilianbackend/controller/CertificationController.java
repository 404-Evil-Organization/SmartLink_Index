package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CertificationQueryRequest;
import com.zhilian.zhilianbackend.dto.request.CertificationUpdateRequest;
import com.zhilian.zhilianbackend.dto.request.CertificationUploadRequest;
import com.zhilian.zhilianbackend.dto.response.CertificationVO;
import com.zhilian.zhilianbackend.entity.Certification;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.CertificationService;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import com.zhilian.zhilianbackend.service.OssService;  // 导入OssService
import com.zhilian.zhilianbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 资质证书管理控制器，提供证书的增删改查接口
 **/
@Slf4j
@RestController
@RequestMapping("/certification")
@RequiredArgsConstructor
@Tag(name = "资质证书管理", description = "证书上传、列表、修改、删除等接口")
public class CertificationController {

    private final CertificationService certificationService;
    private final ServiceProviderService serviceProviderService;
    private final JwtUtil jwtUtil;
    private final OssService ossService;  // 注入OssService

    /**
     * 从请求中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        if (!bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        String token = bearerToken.substring(7);
        if (token.isBlank()) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return token;
    }

    /**
     * 从token中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("解析token获取用户ID失败: {}", e.getMessage());
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
    }

    /**
     * 从token中获取当前用户角色
     */
    private String getCurrentUserRole(HttpServletRequest request) {
        String token = extractToken(request);
        try {
            Claims claims = jwtUtil.parseToken(token);
            return claims.get(JwtUtil.CLAIM_ROLE, String.class);
        } catch (Exception e) {
            log.warn("解析token获取用户角色失败: {}", e.getMessage());
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
    }

    /**
     * 获取当前登录用户对应的服务商ID
     * @return 服务商ID，如果不是服务商角色则返回null
     */
    private Long getCurrentServiceProviderId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        log.info("获取服务商ID - 用户ID: {}, 角色: {}", userId, role);

        if (userId == null) {
            log.warn("用户ID为空");
            return null;
        }

        // 只有服务商角色才能获取服务商ID
        if (!"service".equals(role)) {
            log.warn("用户角色不是服务商: {}", role);
            return null;
        }

        // 根据userId查询服务商信息
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getUserId, userId);
        wrapper.isNull(ServiceProvider::getDeleted); // 明确指定只查询未删除的

        log.info("执行查询: user_id = {}, deleted IS NULL", userId);
        // 使用 getOne(wrapper, false) 避免当存在多条记录时抛出运行时异常，防止接口直接返回 500
        ServiceProvider serviceProvider = serviceProviderService.getOne(wrapper, false);

        if (serviceProvider == null) {
            log.warn("未找到user_id={}的服务商记录", userId);
        } else {
            log.info("找到服务商记录: id={}, company_name={}", serviceProvider.getId(), serviceProvider.getCompanyName());
        }

        return serviceProvider != null ? serviceProvider.getId() : null;
    }

    /**
     * 判断当前用户是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "admin".equals(role);
    }

    /**
     * 检查当前用户是否有权限操作指定的证书
     * @param certification 证书实体
     * @return true-有权限 false-无权限
     */
    private boolean hasPermission(HttpServletRequest request, Certification certification) {
        if (certification == null) {
            return false;
        }
        // 管理员有所有权限
        if (isAdmin(request)) {
            return true;
        }
        // 非管理员，检查是否是证书所属的服务商
        Long currentServiceId = getCurrentServiceProviderId(request);
        return currentServiceId != null && currentServiceId.equals(certification.getServiceId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: request 证书查询请求（含serviceId筛选）
     * @Return: Result<Map<String, Object>> 证书列表（带分页信息）
     * @Description: 获取资质证书列表（可按serviceId筛选）
     **/
    @GetMapping("/list")
    @Operation(summary = "获取证书列表", description = "可按serviceId筛选证书列表")
    public Result<Map<String, Object>> list(@Valid CertificationQueryRequest request) {
        log.info("查询证书列表, 请求参数: serviceId={}, page={}, size={}",
                request.getServiceId(), request.getPage(), request.getSize());

        // 构建查询条件
        LambdaQueryWrapper<Certification> wrapper = new LambdaQueryWrapper<>();
        if (request.getServiceId() != null) {
            wrapper.eq(Certification::getServiceId, request.getServiceId());
        }
        wrapper.orderByDesc(Certification::getCreateTime);

        // 分页查询
        Page<Certification> page = new Page<>(request.getPage(), request.getSize());
        Page<Certification> pageResult = certificationService.page(page, wrapper);

        // 转换为VO
        List<CertificationVO> records = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 封装分页数据到Map中
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("records", records);
        data.put("page", request.getPage());
        data.put("size", request.getSize());

        log.info("查询证书列表成功, 总记录数: {}, 当前页记录数: {}", pageResult.getTotal(), records.size());
        return Result.success(data);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: request 证书上传请求（含文件URL）
     * @Return: Result<Long> 新创建的证书ID
     * @Description: 上传资质证书（文件URL由前端通过/common/upload接口获取）
     **/
    @PostMapping("/upload")
    @Operation(summary = "上传证书", description = "创建证书记录，文件URL需先通过/common/upload接口获取")
    public Result<Long> upload(HttpServletRequest request, @Valid @RequestBody CertificationUploadRequest uploadRequest) {
        log.info("上传证书, 请求参数: {}", uploadRequest);

        // 获取当前登录用户对应的服务商ID
        Long serviceId = getCurrentServiceProviderId(request);
        if (serviceId == null) {
            log.warn("上传证书失败：当前用户不是服务商角色或未找到对应的服务商信息");
            return Result.forbidden("只有服务商才能上传证书");
        }

        // 创建证书实体
        Certification certification = new Certification();
        BeanUtils.copyProperties(uploadRequest, certification);

        // 设置服务商ID（从认证信息中获取，忽略请求中的serviceId）
        certification.setServiceId(serviceId);
        certification.setStatus((byte) 1); // 默认有效

        // 保存到数据库
        certificationService.save(certification);

        log.info("证书上传成功, 证书ID: {}, 文件URL: {}", certification.getId(), uploadRequest.getCertFileUrl());
        return Result.success(certification.getId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: id 证书ID
     * @Param: request 证书更新请求（可选文件URL）
     * @Return: Result<Void>
     * @Description: 更新证书信息（如需更换文件，需先上传新文件获取URL）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新证书", description = "修改证书信息，如需更换文件需先上传新文件获取URL")
    public Result<Void> update(
            HttpServletRequest request,
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CertificationUpdateRequest updateRequest) {
        log.info("更新证书, 证书ID: {}, 请求参数: {}", id, updateRequest);

        // 检查证书是否存在
        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        // 权限检查
        if (!hasPermission(request, existing)) {
            log.warn("更新证书失败：无权限操作此证书, 证书ID: {}", id);
            return Result.forbidden("无权限操作此证书");
        }

        // 如果更换了文件，删除旧文件
        if (updateRequest.getCertFileUrl() != null &&
                !updateRequest.getCertFileUrl().equals(existing.getCertFileUrl())) {

            log.info("证书文件被替换，删除旧文件: {}", existing.getCertFileUrl());
            // 删除旧文件（不阻塞主流程，即使删除失败也继续更新）
            try {
                // 这里使用注入的ossService实例，不是静态调用
                ossService.deleteFile(existing.getCertFileUrl());
            } catch (Exception e) {
                log.error("删除旧证书文件失败, URL: {}", existing.getCertFileUrl(), e);
                // 继续执行，不影响主流程
            }
        }

        // 更新字段
        Certification certification = new Certification();
        BeanUtils.copyProperties(updateRequest, certification);
        certification.setId(id);
        certification.setServiceId(existing.getServiceId()); // 保持原有的serviceId不变

        // 更新到数据库
        certificationService.updateById(certification);

        log.info("证书更新成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: id 证书ID
     * @Return: Result<Void>
     * @Description: 删除证书（逻辑删除，同时删除OSS文件）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除证书", description = "逻辑删除证书记录，同时删除OSS上的文件")
    public Result<Void> delete(
            HttpServletRequest request,
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        log.info("删除证书, 证书ID: {}", id);

        // 检查证书是否存在
        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        // 权限检查
        if (!hasPermission(request, existing)) {
            log.warn("删除证书失败：无权限操作此证书, 证书ID: {}", id);
            return Result.forbidden("无权限操作此证书");
        }

        // 先删除OSS上的文件（物理删除）
        if (existing.getCertFileUrl() != null && !existing.getCertFileUrl().isEmpty()) {
            log.info("删除证书关联的OSS文件: {}", existing.getCertFileUrl());
            try {
                // 这里使用注入的ossService实例，不是静态调用
                ossService.deleteFile(existing.getCertFileUrl());
            } catch (Exception e) {
                log.error("删除OSS文件失败, URL: {}", existing.getCertFileUrl(), e);
                // 即使OSS删除失败，也继续逻辑删除数据库记录
                // 因为OSS文件删除失败可能由网络等原因导致，可以后续通过定时任务清理
            }
        }

        // 逻辑删除数据库记录
        certificationService.removeById(id);

        log.info("证书删除成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: certification 证书实体
     * @Return: CertificationVO 证书VO对象
     * @Description: 将证书实体转换为VO对象
     **/
    private CertificationVO convertToVO(Certification certification) {
        if (certification == null) {
            return null;
        }
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(certification, vo);
        return vo;
    }
}