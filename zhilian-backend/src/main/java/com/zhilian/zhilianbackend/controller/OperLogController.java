package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.response.OperLogVO;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.OperLogService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 22:45
 * @Description: 操作日志管理接口（仅管理员可见）
 */
@Slf4j
@RestController
@RequestMapping("/admin/log")
@RequiredArgsConstructor
@Tag(name = "操作日志管理", description = "管理员查询系统操作日志")
public class OperLogController {

    private final OperLogService operLogService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25 22:45
     * @Param: page 页码，从1开始，默认1
     * @Param: size 每页条数，默认10，最大100
     * @Param: username 操作人用户名（模糊匹配）
     * @Param: operation 操作类型（精确匹配）
     * @Param: startTime 开始时间，格式 yyyy-MM-dd HH:mm:ss
     * @Param: endTime 结束时间，格式 yyyy-MM-dd HH:mm:ss
     * @Return: Result<PageResult<OperLogVO>> 分页日志数据
     * @Description: 分页获取操作日志列表，支持按用户名、操作类型、时间范围筛选，仅管理员可访问
     */
    @GetMapping("/list")
    @Operation(summary = "分页获取操作日志列表", description = "支持按用户名、操作类型、时间范围筛选")
    public Result<PageResult<OperLogVO>> listOperLogs(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "操作人用户名（模糊匹配）") @RequestParam(required = false) String username,
            @Parameter(description = "操作类型（精确匹配）") @RequestParam(required = false) String operation,
            @Parameter(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 权限校验：仅管理员可访问
        if (!securityUtils.isAdmin()) {
            log.warn("非管理员用户尝试访问操作日志列表");
            throw new BusinessException(403, "权限不足，仅管理员可查看操作日志");
        }

        // 分页参数校验
        if (page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }

        // 时间范围合法性校验
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            log.warn("开始时间 {} 大于结束时间 {}", startTime, endTime);
            return Result.badRequest("开始时间不能大于结束时间");
        }

        IPage<OperLogVO> operLogPage = operLogService.listOperLogs(page, size, username, operation, startTime, endTime);
        return Result.success(PageResult.from(operLogPage));
    }
}