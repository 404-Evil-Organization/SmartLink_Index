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

    // 注入OssService接口
    private final OssService ossService;

    // 允许的文件扩展名列表
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg");

    // 允许的Content-Type列表
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    /**
     * 测试接口 - 用于检查服务是否正常
     */
    @GetMapping("/test")
    @Operation(summary = "测试接口", description = "用于测试服务是否正常")
    public String getCommon() {
        log.info("测试接口被调用");
        return "common service is running";
    }

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
            @RequestParam("file") MultipartFile file) {

        log.info("接收文件上传请求，文件名：{}，文件大小：{}KB，Content-Type：{}",
                file.getOriginalFilename(),
                file.getSize() / 1024,
                file.getContentType());

        // ============= 文件基础校验 =============

        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            log.warn("文件上传失败：文件不能为空");
            return Result.badRequest("文件不能为空");
        }

        // 2. 检查文件大小（Spring的multipart配置作为后备，这里提供友好的错误提示）
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("文件上传失败：文件大小超过限制，当前大小：{}MB，最大允许：10MB",
                    file.getSize() / (1024 * 1024.0));
            return Result.badRequest("文件大小超过限制，最大允许10MB");
        }

        // 3. 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
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

        // 4. 检查Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("文件上传失败：不支持的Content-Type，Content-Type：{}，允许的类型：{}",
                    contentType, ALLOWED_CONTENT_TYPES);
            return Result.badRequest("不支持的文件类型，请上传有效的图片文件");
        }

        // 5. 扩展名和Content-Type一致性校验（防止伪装文件）
        boolean isValidContentType = false;
        if (extension.equals("png") && "image/png".equals(contentType)) {
            isValidContentType = true;
        } else if ((extension.equals("jpg") || extension.equals("jpeg")) &&
                ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType))) {
            isValidContentType = true;
        }

        if (!isValidContentType) {
            log.warn("文件上传失败：扩展名与Content-Type不匹配，扩展名：{}，Content-Type：{}",
                    extension, contentType);
            return Result.badRequest("文件格式不匹配，请上传正确的图片文件");
        }

        // ============= 执行上传 =============
        try {
            // 调用OSS服务上传文件
            String fileUrl = ossService.uploadFile(file);

            // 构建返回数据
            Map<String, String> data = new HashMap<>();
            data.put("fileUrl", fileUrl);

            log.info("文件上传成功，URL：{}", fileUrl);
            return Result.success(data);

        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            return Result.error(500, "文件上传失败：" + e.getMessage());
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

        // 参数校验
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("文件删除失败：文件URL不能为空");
            return Result.badRequest("文件URL不能为空");
        }

        // 安全校验：防止目录穿越
        if (fileUrl.contains("..") || fileUrl.contains("./") || fileUrl.contains("/.")) {
            log.warn("文件删除失败：检测到非法路径，fileUrl={}", fileUrl);
            return Result.badRequest("非法的文件URL");
        }

        try {
            // 调用OSS服务删除文件
            ossService.deleteFile(fileUrl);

            log.info("文件删除成功，URL：{}", fileUrl);
            return Result.success("文件删除成功", null);

        } catch (Exception e) {
            log.error("文件删除异常：{}", e.getMessage(), e);
            return Result.error(500, "文件删除异常：" + e.getMessage());
        }
    }
}