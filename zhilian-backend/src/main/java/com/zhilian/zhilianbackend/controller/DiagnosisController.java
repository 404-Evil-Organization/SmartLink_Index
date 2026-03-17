package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DiagnosisSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.DiagnosisReportVO;
import com.zhilian.zhilianbackend.service.DiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class DiagnosisController {
    private final DiagnosisService diagnosisService;

    /**
     * 从 Spring Security 的 SecurityContext 中获取当前登录用户 ID。
     * 说明：需与全局认证逻辑保持一致，避免与 JwtAuthenticationFilter 行为不一致。
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("当前请求未找到认证信息，无法获取用户ID");
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.valueOf(username);
            } catch (NumberFormatException e) {
                log.warn("无法从 UserDetails.username 解析为用户ID: {}", username);
                return null;
            }
        }
        if (principal instanceof String) {
            try {
                return Long.valueOf((String) principal);
            } catch (NumberFormatException e) {
                log.warn("无法从 String principal 解析为用户ID: {}", principal);
                return null;
            }
        }
        log.warn("未知类型的 principal: {}", principal);
        return null;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:47
     * @Param: request 诊断提交请求参数
     * @Return: Result<DiagnosisReportVO> 统一返回格式的诊断报告
     * @Description: 提交诊断问卷接口
    **/
    @PostMapping("/submit")
    public Result<DiagnosisReportVO> submitDiagnosis(
            @Valid @RequestBody DiagnosisSubmitRequest request) {

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
    public Result<DiagnosisReportVO> getLatestDiagnosis(
            @RequestParam("manuId") Long manuId) {

        log.info("接收到获取企业最新诊断报告请求: manuId={}", manuId);

        Long userId = getCurrentUserId();

        DiagnosisReportVO response = diagnosisService.getLatestDiagnosis(manuId, userId);

        return Result.success(response);
    }
}
