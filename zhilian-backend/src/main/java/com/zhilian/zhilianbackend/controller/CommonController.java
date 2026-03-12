package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 通用接口控制器 - 提供文件上传、删除等通用功能
 *
 * @Author: xiaodengyou
 * @Date: 2026/3/11
 */
@Slf4j
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Tag(name = "通用接口", description = "文件上传、删除等通用功能")
public class CommonController {

    // 注入OssService接口（面向接口编程，避免与具体实现耦合）
    private final OssService ossService;

    // 允许的文件扩展名列表（统一小写）
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg");

    // 允许的Content-Type列表（统一小写）
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    // OSS文件URL的正则表达式：用于验证URL格式并提取路径
    // 示例格式：https://<bucket>.oss-<region>.aliyuncs.com/uploads/xxx.jpg
    private static final Pattern OSS_URL_PATTERN = Pattern.compile(
            "^https?://[^/]+/uploads/[a-zA-Z0-9/\\-_]+\\.(png|jpg|jpeg)$"
    );

    /**
     * 1.5.4 OSS文件上传
     * URL: /api/common/upload
     * Method: POST
     * 文件限制：png、jpg、jpeg格式，最大10MB
     */
    @PostMapping("/upload")
    @Operation(summary = "OSS文件上传", description = "上传文件到阿里云OSS，返回文件访问URL。仅支持png、jpg、jpeg格式，最大10MB")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "要上传的文件（仅支持png、jpg、jpeg格式，最大10MB）", required = true)
            @RequestParam(value = "file", required = true) MultipartFile file) {

        // ============= 文件基础校验 =============

        // 1. 先进行空文件校验，避免NPE
        if (file == null || file.isEmpty()) {
            log.warn("文件上传失败：文件不能为空（file参数缺失或为空）");
            return Result.badRequest("文件不能为空");
        }

        // 2. 文件存在，安全地获取文件信息并记录日志
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();
        String originalContentType = file.getContentType();
        // 统一归一化为小写，用于后续所有校验
        String contentType = originalContentType != null ? originalContentType.toLowerCase() : null;

        log.info("接收文件上传请求，文件名：{}，文件大小：{}KB，原始Content-Type：{}",
                originalFilename,
                fileSize / 1024,
                originalContentType);

        // 3. 检查文件大小（Spring的multipart配置作为后备，这里提供友好的错误提示）
        if (fileSize > MAX_FILE_SIZE) {
            log.warn("文件上传失败：文件大小超过限制，当前大小：{}MB，最大允许：10MB",
                    fileSize / (1024 * 1024.0));
            return Result.badRequest("文件大小超过限制，最大允许10MB");
        }

        // 4. 检查文件扩展名
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("文件上传失败：文件名无效或无扩展名，filename={}", originalFilename);
            return Result.badRequest("文件名无效，请提供有效的图片文件");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("文件上传失败：不支持的扩展名，扩展名：{}，允许的扩展名：{}",
                    extension, ALLOWED_EXTENSIONS);
            return Result.badRequest("不支持的文件格式，仅支持：png、jpg、jpeg");
        }

        // 5. 检查Content-Type（使用已归一化的小写contentType）
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("文件上传失败：不支持的Content-Type，Content-Type：{}，允许的类型：{}",
                    originalContentType, ALLOWED_CONTENT_TYPES);
            return Result.badRequest("不支持的文件类型，请上传有效的图片文件");
        }

        // 6. 扩展名和Content-Type一致性校验（使用归一化后的小写值进行比较）
        boolean isValidContentType = false;
        if (extension.equals("png") && "image/png".equals(contentType)) {
            isValidContentType = true;
        } else if ((extension.equals("jpg") || extension.equals("jpeg")) &&
                ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType))) {
            isValidContentType = true;
        }

        if (!isValidContentType) {
            log.warn("文件上传失败：扩展名与Content-Type不匹配，扩展名：{}，原始Content-Type：{}",
                    extension, originalContentType);
            return Result.badRequest("文件格式不匹配，请上传正确的图片文件");
        }

        // ============= 执行上传 =============
        try {
            // 调用OSS服务上传文件（面向接口编程）
            String fileUrl = ossService.uploadFile(file);

            // 构建返回数据
            Map<String, String> data = new HashMap<>();
            data.put("fileUrl", fileUrl);

            log.info("文件上传成功，URL：{}", fileUrl);
            return Result.success(data);

        } catch (Exception e) {
            // 记录完整的异常信息到日志，但不暴露给客户端
            log.error("文件上传处理异常，文件名：{}，文件大小：{}KB，Content-Type：{}，异常信息：",
                    originalFilename, fileSize / 1024, originalContentType, e);

            // 返回统一的业务提示，不暴露内部细节
            return Result.error(500, "文件上传失败，请稍后重试或联系管理员");
        }
    }

    /**
     * 1.5.5 OSS文件删除
     * URL: /api/common/delete
     * Method: POST
     */
    @PostMapping("/delete")
    @Operation(summary = "OSS文件删除", description = "根据文件URL删除阿里云OSS上的文件")
    public Result<Void> deleteFile(
            @Parameter(description = "删除请求参数", required = true)
            @RequestBody Map<String, String> request) {

        String fileUrl = request.get("fileUrl");
        log.info("接收文件删除请求，URL：{}", fileUrl);

        // ============= 统一的URL校验 =============

        // 1. 非空校验
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("文件删除失败：文件URL不能为空");
            return Result.badRequest("文件URL不能为空");
        }

        // 2. 统一的URL格式和安全性校验
        String validationError = validateFileUrl(fileUrl);
        if (validationError != null) {
            return Result.badRequest(validationError);
        }

        // ============= 执行删除 =============
        try {
            // 调用OSS服务删除文件（面向接口编程）
            boolean deleted = ossService.deleteFile(fileUrl);
            if (!deleted) {
                // OSS 未能成功删除文件，可能原因：文件不存在、已被删除或服务暂时不可用
                log.warn("文件删除失败：OSS 服务返回删除失败，URL：{}", fileUrl);
                return Result.error(500, "文件删除失败，请检查文件是否存在或稍后重试");
            }

            log.info("文件删除成功，URL：{}", fileUrl);
            return Result.success("文件删除成功", null);

        } catch (Exception e) {
            // 记录完整的异常信息到日志，但不暴露给客户端
            log.error("文件删除处理异常，URL：{}，异常信息：", fileUrl, e);

            // 返回统一的业务提示，不暴露内部细节
            return Result.error(500, "文件删除失败，请稍后重试或联系管理员");
        }
    }

    /**
     * 统一的文件URL校验方法
     * @param fileUrl 待校验的文件URL
     * @return 校验失败时的错误信息，校验通过返回null
     */
    private String validateFileUrl(String fileUrl) {
        // 基础校验：必须是 http/https URL，避免传入本地路径或其他非法格式
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            log.warn("URL格式非法，仅支持http/https，fileUrl={}", fileUrl);
            return "非法的文件URL";
        }

        // 安全校验：防止目录穿越和非法路径（统一校验所有危险模式）
        // 检查的危险模式包括：.. 、 ./ 、 /. 、 // 、 \ 等
        String[] dangerousPatterns = {"..", "./", "/.", "//", "\\", "%2e", "%2f"};
        for (String pattern : dangerousPatterns) {
            if (fileUrl.toLowerCase().contains(pattern)) {
                log.warn("检测到非法路径模式[{}]，fileUrl={}", pattern, fileUrl);
                return "非法的文件URL";
            }
        }

        // 路径规范：确保URL格式符合OSS的预期格式
        // 这里使用正则表达式进行严格校验，确保URL格式正确且只包含允许的字符
        if (!OSS_URL_PATTERN.matcher(fileUrl).matches()) {
            log.warn("URL格式不符合OSS规范，fileUrl={}", fileUrl);
            return "非法的文件URL";
        }

        // 可以添加更多业务相关的校验，例如：
        // - 校验文件是否属于当前用户
        // - 校验文件是否在允许的目录下
        // - 校验文件扩展名是否在白名单内

        return null;
    }
}