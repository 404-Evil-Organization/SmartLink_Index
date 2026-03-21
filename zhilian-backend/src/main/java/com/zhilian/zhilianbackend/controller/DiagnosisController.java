package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DiagnosisSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.DiagnosisReportVO;
import com.zhilian.zhilianbackend.service.DiagnosisService;
import com.zhilian.zhilianbackend.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/17 21:18
 * @Param:
 * @Return:
 * @Description: 诊断模块控制器，处理诊断相关的HTTP请求
 **/
@Slf4j
@RestController
@RequestMapping("/diagnosis")
@RequiredArgsConstructor
@Tag(name = "数字化诊断模块", description = "企业数字化诊断相关接口")
public class DiagnosisController {
    private final DiagnosisService diagnosisService;

    /**
     * 从 Spring Security 的 SecurityContext 中获取当前登录用户 ID。
     * 说明：需与全局认证逻辑保持一致，避免与 JwtAuthenticationFilter 行为不一致。
     * 当认证信息缺失、为匿名用户或无法解析出合法用户 ID 时，统一抛出 401 业务异常。
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("当前请求未找到认证信息或未通过认证，拒绝访问诊断接口");
            throw new BusinessException(401, "请先登录");
        }
        Object principal = authentication.getPrincipal();
        // 与全局安全配置保持一致：anonymousUser 视为未登录
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            log.warn("当前请求为匿名用户访问诊断接口，拒绝访问");
            throw new BusinessException(401, "请先登录");
        }
        // 统一使用 authentication.getName() 解析当前登录用户标识
        String userIdStr = authentication.getName();
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.warn("无法从 authentication.getName() 解析用户ID，name={}", userIdStr, e);
            throw new BusinessException(401, "请先登录");
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:47
     * @Param: request 诊断提交请求参数
     * @Return: Result<DiagnosisReportVO> 统一返回格式的诊断报告
     * @Description: 提交诊断问卷接口
    **/
    @PostMapping("/submit")
    @Operation(summary = "提交诊断问卷", description = "制造企业填写问卷，系统计算诊断得分并生成报告")
    public Result<DiagnosisReportVO> submitDiagnosis(
            @Valid @RequestBody(required = false) DiagnosisSubmitRequest request) {

        // 空 body 时 request 会为 null，这里主动抛出业务异常映射为 400，避免返回 500
        if (request == null) {
            log.warn("诊断问卷提交请求体为空");
            throw new BusinessException(400, "诊断提交请求体不能为空");
        }

        log.info("接收到诊断问卷提交请求: manuId={}", request.getManuId());

        // 从 SecurityContext 中获取当前登录用户ID，避免在 Controller 内重复解析 JWT
        Long userId = getCurrentUserId();

        DiagnosisReportVO response = diagnosisService.submitDiagnosis(request, userId);

        return Result.success(response);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:50
     * @Param: id 诊断记录ID
     * @Return: Result<DiagnosisReportVO> 统一返回格式的诊断报告
     * @Description: 根据ID获取诊断报告接口
    **/
    @GetMapping("/result/{id}")
    @Operation(summary = "获取诊断报告", description = "根据诊断ID获取诊断详情，包含各维度得分、总分、等级和建议")
    public Result<DiagnosisReportVO> getDiagnosisById(
            @PathVariable("id") Long id) {

        log.info("接收到获取诊断报告请求: id={}", id);

        Long userId = getCurrentUserId();

        DiagnosisReportVO response = diagnosisService.getDiagnosisById(id, userId);

        return Result.success(response);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:51
     * @Param: manuId 制造企业ID
     * @Return: Result<DiagnosisReportVO> 统一返回格式的最新诊断报告
     * @Description: 获取企业最新诊断报告接口
    **/
    @GetMapping("/latest")
    @Operation(summary = "获取最新诊断报告", description = "获取指定企业的最新诊断报告")
    public Result<DiagnosisReportVO> getLatestDiagnosis(
            @RequestParam(name = "manuId", required = false) Long manuId) {

        // 参数为空时避免抛出 MissingServletRequestParameterException，由业务异常统一处理返回 4xx
        if (manuId == null) {
            log.warn("获取企业最新诊断报告缺少必填参数：manuId");
            throw new BusinessException("缺少必填参数：manuId");
        }

        log.info("接收到获取企业最新诊断报告请求: manuId={}", manuId);

        Long userId = getCurrentUserId();

        DiagnosisReportVO response = diagnosisService.getLatestDiagnosis(manuId, userId);

        return Result.success(response);
    }
}
