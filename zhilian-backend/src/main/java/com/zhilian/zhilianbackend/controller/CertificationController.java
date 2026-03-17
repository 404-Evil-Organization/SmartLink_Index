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
import com.zhilian.zhilianbackend.service.OssService;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
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
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
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
    private final OssService ossService;

    // ---------- 辅助方法 ----------

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest
     * @Return: String
     * @Description: 从请求头中提取 JWT token
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
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest
     * @Return: Long
     * @Description: 从 token 中获取当前用户ID
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
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest
     * @Return: String
     * @Description: 从 token 中获取当前用户角色
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
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest
     * @Return: Long
     * @Description: 获取当前登录用户对应的服务商ID
     */
    private Long getCurrentServiceProviderId(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        log.info("获取服务商ID - 用户ID: {}, 角色: {}", userId, role);

        if (userId == null || !"service".equals(role)) {
            return null;
        }

        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getUserId, userId);

        log.info("执行查询: user_id = {}", userId);
        // 使用 getOne(wrapper, false) 避免当存在多条记录时抛出运行时异常，防止接口直接返回 500
        ServiceProvider serviceProvider = serviceProviderService.getOne(wrapper, false);

        return serviceProvider != null ? serviceProvider.getId() : null;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest
     * @Return: boolean
     * @Description: 判断当前用户是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "admin".equals(role);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest, certification Certification
     * @Return: boolean
     * @Description: 检查当前用户是否有权限操作指定的证书
     */
    private boolean hasPermission(HttpServletRequest request, Certification certification) {
        if (certification == null) return false;
        if (isAdmin(request)) return true;
        Long currentServiceId = getCurrentServiceProviderId(request);
        return currentServiceId != null && currentServiceId.equals(certification.getServiceId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: file MultipartFile
     * @Return: void
     * @Description: 校验证书文件（大小、扩展名、Content-Type）
     */
    private void validateCertificationFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传证书文件不能为空");
        }
        // 大小限制：10MB
        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new BusinessException("证书文件过大，单个文件不能超过10MB");
        }
        // 扩展名白名单
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(ext)) {
            throw new BusinessException("证书文件扩展名不允许");
        }
        ext = ext.toLowerCase();
        List<String> allowedExt = Arrays.asList("pdf", "jpg", "jpeg", "png");
        if (!allowedExt.contains(ext)) {
            throw new BusinessException("证书文件类型不支持，只允许上传 PDF 或图片文件");
        }
        // Content-Type 白名单 + 归一化 + 扩展名一致性校验
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException("证书文件 Content-Type 为空或不合法");
        }
        // 统一将 Content-Type 转为小写，避免大小写差异导致合法请求被拒
        contentType = contentType.toLowerCase();
        List<String> allowedContentTypes = Arrays.asList(
                MediaType.APPLICATION_PDF_VALUE,
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.IMAGE_PNG_VALUE,
                "image/jpg"
        );
        if (!allowedContentTypes.contains(contentType)) {
            throw new BusinessException("证书文件 Content-Type 不合法");
        }
        // 扩展名与 Content-Type 一致性校验，防止伪造后缀或类型不匹配
        if ("pdf".equals(ext)) {
            if (!MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
                throw new BusinessException("证书文件扩展名与 Content-Type 不匹配，PDF 文件仅支持 application/pdf");
            }
        } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType) && !"image/jpg".equals(contentType)) {
                throw new BusinessException("证书文件扩展名与 Content-Type 不匹配，JPG 文件仅支持 image/jpeg 或 image/jpg");
            }
        } else if ("png".equals(ext)) {
            if (!MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
                throw new BusinessException("证书文件扩展名与 Content-Type 不匹配，PNG 文件仅支持 image/png");
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: file MultipartFile
     * @Return: String
     * @Description: 上传文件到 OSS，失败时抛出业务异常
     */
    private String uploadFileWithException(MultipartFile file) {
        try {
            return ossService.uploadFile(file);
        } catch (Exception e) {
            log.error("OSS文件上传失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("证书文件上传失败，请稍后重试");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: fileUrl String, operationDesc String
     * @Return: void
     * @Description: 静默删除 OSS 文件，只记录日志，不抛出异常
     */
    private void deleteFileQuietly(String fileUrl, String operationDesc) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        try {
            boolean deleted = ossService.deleteFile(fileUrl);
            if (!deleted) {
                log.warn("{} 失败(OSS返回false), URL: {}", operationDesc, fileUrl);
            } else {
                log.info("{} 成功, URL: {}", operationDesc, fileUrl);
            }
        } catch (Exception e) {
            log.error("{} 异常, URL: {}", operationDesc, fileUrl, e);
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: certification Certification
     * @Return: CertificationVO
     * @Description: 将证书实体转换为VO对象
     */
    private CertificationVO convertToVO(Certification certification) {
        if (certification == null) return null;
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(certification, vo);
        return vo;
    }

    // ---------- 接口方法 ----------

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request CertificationQueryRequest
     * @Return: Result<Map<String, Object>> 分页证书列表
     * @Description: 获取证书列表，可按serviceId筛选
     */
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
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest, uploadRequest CertificationUploadRequest
     * @Return: Result<Long> 新创建的证书ID
     * @Description: 上传证书，包含文件上传
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传证书", description = "上传证书文件，后端自动保存到OSS并记录URL")
    public Result<Long> upload(
            HttpServletRequest request,
            @Valid @ModelAttribute CertificationUploadRequest uploadRequest) {
        log.info("上传证书, 证书名称: {}, 文件大小: {}",
                uploadRequest.getCertName(),
                uploadRequest.getFile() == null ? 0L : uploadRequest.getFile().getSize());

        Long serviceId = getCurrentServiceProviderId(request);
        if (serviceId == null) {
            log.warn("上传证书失败：当前用户不是服务商角色或未找到对应的服务商信息");
            return Result.forbidden("只有服务商才能上传证书");
        }

        // 文件安全校验
        MultipartFile file = uploadRequest.getFile();
        validateCertificationFile(file);

        // 1. 上传文件到OSS
        String fileUrl = uploadFileWithException(file);

        // 2. 创建证书实体
        Certification certification = new Certification();
        BeanUtils.copyProperties(uploadRequest, certification);
        certification.setServiceId(serviceId);
        certification.setCertFileUrl(fileUrl);
        certification.setStatus((byte) 1);

        // 3. 保存到数据库（若失败则补偿删除已上传的文件）
        boolean saved;
        try {
            saved = certificationService.save(certification);
        } catch (Exception e) {
            log.error("证书数据库保存失败，尝试删除已上传的OSS文件: {}", fileUrl, e);
            deleteFileQuietly(fileUrl, "补偿删除上传失败的文件");
            throw new BusinessException("证书记录保存失败，请稍后重试");
        }

        if (!saved) {
            log.error("证书数据库保存返回 false，删除已上传的OSS文件: {}", fileUrl);
            deleteFileQuietly(fileUrl, "补偿删除上传失败的文件");
            throw new BusinessException("证书记录保存失败，请稍后重试");
        }

        log.info("证书上传成功, 证书ID: {}, 文件URL: {}", certification.getId(), fileUrl);
        return Result.success(certification.getId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest, id Long, updateRequest CertificationUpdateRequest
     * @Return: Result<Void>
     * @Description: 更新证书信息，可选择替换文件
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新证书", description = "修改证书信息，可选择替换文件")
    public Result<Void> update(
            HttpServletRequest request,
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Valid @ModelAttribute CertificationUpdateRequest updateRequest) {
        log.info("更新证书, 证书ID: {}", id);

        // 1. 检查证书是否存在
        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        // 2. 权限检查
        if (!hasPermission(request, existing)) {
            log.warn("更新证书失败：无权限操作此证书, 证书ID: {}", id);
            return Result.forbidden("无权限操作此证书");
        }

        // 3. 准备更新的字段
        Certification certification = new Certification();
        BeanUtils.copyProperties(updateRequest, certification);
        certification.setId(id);
        certification.setServiceId(existing.getServiceId()); // 保持原有服务商ID

        // 4. 处理文件替换
        MultipartFile newFile = updateRequest.getFile();
        boolean needReplaceFile = (newFile != null && !newFile.isEmpty());

        String newFileUrl = null;
        if (needReplaceFile) {
            // 4.1 校验新文件
            validateCertificationFile(newFile);
            // 4.2 上传新文件到OSS
            newFileUrl = uploadFileWithException(newFile);
            certification.setCertFileUrl(newFileUrl);
        }

        // 5. 更新数据库
        boolean updated;
        try {
            updated = certificationService.updateById(certification);
        } catch (Exception e) {
            // 5.1 数据库更新异常，补偿删除新上传的文件（如果有）
            if (needReplaceFile && newFileUrl != null) {
                deleteFileQuietly(newFileUrl, "补偿删除新上传的文件（数据库异常）");
            }
            log.error("更新证书数据库异常, 证书ID: {}", id, e);
            throw new BusinessException("证书更新失败，请稍后重试");
        }

        if (!updated) {
            // 5.2 更新返回false，同样补偿删除新文件
            if (needReplaceFile && newFileUrl != null) {
                deleteFileQuietly(newFileUrl, "补偿删除新上传的文件（更新失败）");
            }
            log.error("更新证书数据库返回false, 证书ID: {}", id);
            throw new BusinessException("证书更新失败，请稍后重试");
        }

        // 6. 数据库更新成功，此时才删除旧文件（如果有替换）
        if (needReplaceFile && StringUtils.hasText(existing.getCertFileUrl())) {
            deleteFileQuietly(existing.getCertFileUrl(), "删除旧证书文件");
        }

        log.info("证书更新成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/17 8:28
     * @Param: request HttpServletRequest, id Long
     * @Return: Result<Void>
     * @Description: 逻辑删除证书，同时删除OSS上的文件
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

        // 先逻辑删除数据库记录
        try {
            boolean removed = certificationService.removeById(id);
            if (!removed) {
                log.error("删除证书数据库记录失败, 证书ID: {}", id);
                throw new BusinessException("证书删除失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("删除证书数据库记录异常, 证书ID: {}", id, e);
            throw new BusinessException("证书删除失败，请稍后重试");
        }

        // 数据库记录删除成功后，再尝试删除 OSS 文件（失败仅记录日志）
        if (StringUtils.hasText(existing.getCertFileUrl())) {
            deleteFileQuietly(existing.getCertFileUrl(), "删除证书关联的OSS文件");
        }

        log.info("证书删除成功, 证书ID: {}", id);
        return Result.success();
    }
}