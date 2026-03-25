package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.entity.OperLog;
import com.zhilian.zhilianbackend.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 操作日志管理接口（仅管理员可见）
 */
@Slf4j
@RestController
@RequestMapping("/admin/log")
@RequiredArgsConstructor
@Tag(name = "操作日志管理", description = "管理员查询系统操作日志")
public class OperLogController {

    private final OperLogService operLogService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "分页获取操作日志列表", description = "支持按用户名、操作类型、时间范围筛选")
    public Result<PageResult<OperLog>> listOperLogs(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "操作人用户名（模糊匹配）") @RequestParam(required = false) String username,
            @Parameter(description = "操作类型（精确匹配）") @RequestParam(required = false) String operation,
            @Parameter(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 分页参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 500) {
            size = 10;
        }

        IPage<OperLog> operLogPage = operLogService.listOperLogs(page, size, username, operation, startTime, endTime);
        return Result.success(PageResult.from(operLogPage));
    }
}