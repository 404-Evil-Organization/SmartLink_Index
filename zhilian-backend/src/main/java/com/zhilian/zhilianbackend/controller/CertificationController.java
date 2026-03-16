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
import com.zhilian.zhilianbackend.service.OssService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/certification")
@RequiredArgsConstructor
@Tag(name = "资质证书管理", description = "证书上传、列表、修改、删除等接口")
public class CertificationController {

    private final CertificationService certificationService;
    private final ServiceProviderService serviceProviderService;
    private final JwtUtil jwtUtil;
    private final OssService ossService;

    // ---------- 辅助方法（保持不变） ----------
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

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("解析token获取用户ID失败: {}", e.getMessage());
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
    }

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

    private Long getCurrentServiceProviderId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        log.info("获取服务商ID - 用户ID: {}, 角色: {}", userId, role);

        if (userId == null || !"service".equals(role)) {
            return null;
        }

        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getUserId, userId);
        ServiceProvider serviceProvider = serviceProviderService.getOne(wrapper, false);

        return serviceProvider != null ? serviceProvider.getId() : null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "admin".equals(role);
    }

    private boolean hasPermission(HttpServletRequest request, Certification certification) {
        if (certification == null) return false;
        if (isAdmin(request)) return true;
        Long currentServiceId = getCurrentServiceProviderId(request);
        return currentServiceId != null && currentServiceId.equals(certification.getServiceId());
    }

    // ---------- 接口方法 ----------

    @GetMapping("/list")
    @Operation(summary = "获取证书列表", description = "可按serviceId筛选证书列表")
    public Result<Map<String, Object>> list(@Valid CertificationQueryRequest request) {
        log.info("查询证书列表, 请求参数: serviceId={}, page={}, size={}",
                request.getServiceId(), request.getPage(), request.getSize());

        LambdaQueryWrapper<Certification> wrapper = new LambdaQueryWrapper<>();
        if (request.getServiceId() != null) {
            wrapper.eq(Certification::getServiceId, request.getServiceId());
        }
        wrapper.orderByDesc(Certification::getCreateTime);

        Page<Certification> page = new Page<>(request.getPage(), request.getSize());
        Page<Certification> pageResult = certificationService.page(page, wrapper);

        List<CertificationVO> records = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("records", records);
        data.put("page", request.getPage());
        data.put("size", request.getSize());

        log.info("查询证书列表成功, 总记录数: {}, 当前页记录数: {}", pageResult.getTotal(), records.size());
        return Result.success(data);
    }

    /**
     * 上传证书（包含文件）
     */
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传证书", description = "上传证书文件，后端自动保存到OSS并记录URL")
    public Result<Long> upload(
            HttpServletRequest request,
            @Valid @ModelAttribute CertificationUploadRequest uploadRequest) {
        log.info("上传证书, 证书名称: {}, 文件大小: {}",
                uploadRequest.getCertName(), uploadRequest.getFile().getSize());

        Long serviceId = getCurrentServiceProviderId(request);
        if (serviceId == null) {
            log.warn("上传证书失败：当前用户不是服务商角色或未找到对应的服务商信息");
            return Result.forbidden("只有服务商才能上传证书");
        }

        // 1. 上传文件到OSS
        String fileUrl = ossService.uploadFile(uploadRequest.getFile());

        // 2. 创建证书实体
        Certification certification = new Certification();
        BeanUtils.copyProperties(uploadRequest, certification);
        certification.setServiceId(serviceId);
        certification.setCertFileUrl(fileUrl);
        certification.setStatus((byte) 1); // 默认有效

        // 3. 保存到数据库
        certificationService.save(certification);

        log.info("证书上传成功, 证书ID: {}, 文件URL: {}", certification.getId(), fileUrl);
        return Result.success(certification.getId());
    }

    /**
     * 更新证书（可替换文件）
     */
    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新证书", description = "修改证书信息，可选择替换文件")
    public Result<Void> update(
            HttpServletRequest request,
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Valid @ModelAttribute CertificationUpdateRequest updateRequest) {
        log.info("更新证书, 证书ID: {}", id);

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

        // 处理文件替换
        MultipartFile newFile = updateRequest.getFile();
        String newFileUrl = null;
        if (newFile != null && !newFile.isEmpty()) {
            // 上传新文件
            newFileUrl = ossService.uploadFile(newFile);
            // 删除旧文件（不阻塞主流程）
            if (StringUtils.hasText(existing.getCertFileUrl())) {
                try {
                    ossService.deleteFile(existing.getCertFileUrl());
                } catch (Exception e) {
                    log.error("删除旧证书文件失败, URL: {}", existing.getCertFileUrl(), e);
                }
            }
        }

        // 更新字段
        Certification certification = new Certification();
        BeanUtils.copyProperties(updateRequest, certification);
        certification.setId(id);
        certification.setServiceId(existing.getServiceId()); // 保持原有服务商ID
        if (newFileUrl != null) {
            certification.setCertFileUrl(newFileUrl);
        }

        // 更新到数据库
        certificationService.updateById(certification);

        log.info("证书更新成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * 删除证书（逻辑删除，同时删除OSS文件）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除证书", description = "逻辑删除证书记录，同时删除OSS上的文件")
    public Result<Void> delete(
            HttpServletRequest request,
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        log.info("删除证书, 证书ID: {}", id);

        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        if (!hasPermission(request, existing)) {
            log.warn("删除证书失败：无权限操作此证书, 证书ID: {}", id);
            return Result.forbidden("无权限操作此证书");
        }

        // 删除OSS文件
        if (StringUtils.hasText(existing.getCertFileUrl())) {
            log.info("删除证书关联的OSS文件: {}", existing.getCertFileUrl());
            try {
                boolean deleted = ossService.deleteFile(existing.getCertFileUrl());
                if (!deleted) {
                    log.warn("OSS文件删除失败(未抛出异常), URL: {}", existing.getCertFileUrl());
                }
            } catch (Exception e) {
                log.error("删除OSS文件异常, URL: {}", existing.getCertFileUrl(), e);
            }
        }

        // 逻辑删除数据库记录
        certificationService.removeById(id);

        log.info("证书删除成功, 证书ID: {}", id);
        return Result.success();
    }

    private CertificationVO convertToVO(Certification certification) {
        if (certification == null) return null;
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(certification, vo);
        return vo;
    }
}