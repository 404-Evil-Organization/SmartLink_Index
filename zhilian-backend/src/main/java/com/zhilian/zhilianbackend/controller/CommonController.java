package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.service.impl.OssServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/11
 * @Description: 通用接口控制器 - 提供文件上传、删除等通用功能
 **/
@Slf4j
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Tag(name = "通用接口", description = "文件上传、删除等通用功能")
public class CommonController {

    private final OssServiceImpl ossService;

    /**
     * 1.5.4 OSS文件上传
     * URL: /api/common/upload
     * Method: POST
     */
    @PostMapping("/upload")
    @Operation(summary = "OSS文件上传", description = "上传文件到阿里云OSS，返回文件访问URL")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("接收文件上传请求，文件名：{}，文件大小：{}KB",
                file.getOriginalFilename(), file.getSize() / 1024);

        try {
            // 调用OSS服务上传文件
            String fileUrl = ossService.uploadFile(file);

            // 构建返回数据（符合接口文档格式）
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

        try {
            // 调用OSS服务删除文件
            boolean success = ossService.deleteFile(fileUrl);

            if (success) {
                log.info("文件删除成功，URL：{}", fileUrl);
                return Result.success("文件删除成功", null);
            } else {
                log.error("文件删除失败，URL：{}", fileUrl);
                return Result.error(500, "文件删除失败");
            }

        } catch (Exception e) {
            log.error("文件删除异常：{}", e.getMessage(), e);
            return Result.error(500, "文件删除异常：" + e.getMessage());
        }
    }

}