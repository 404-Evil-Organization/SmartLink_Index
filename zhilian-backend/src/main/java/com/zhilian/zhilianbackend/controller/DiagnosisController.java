package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DiagnosisSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.DiagnosisReportVO;
import com.zhilian.zhilianbackend.service.DiagnosisService;
import com.zhilian.zhilianbackend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final JwtUtil jwtUtil;

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:47
     * @Param: request 诊断提交请求参数 authHeader Authorization头，包含JWT token
     * @Return: Result<DiagnosisReportVO> 统一返回格式的诊断报告
     * @Description: 提交诊断问卷接口
    **/
    @PostMapping("/submit")
    public Result<DiagnosisReportVO> submitDiagnosis(
            @Valid @RequestBody DiagnosisSubmitRequest request,
            @RequestHeader("Authorization") String authHeader) {

        log.info("接收到诊断问卷提交请求: manuId={}", request.getManuId());

        // 从token中获取用户ID
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        DiagnosisReportVO response = diagnosisService.submitDiagnosis(request, userId);

        return Result.success(response);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:50
     * @Param: id 诊断记录ID authHeader Authorization头，包含JWT token
     * @Return: Result<DiagnosisReportVO> 统一返回格式的诊断报告
     * @Description: 根据ID获取诊断报告接口
    **/
    @GetMapping("/result/{id}")
    public Result<DiagnosisReportVO> getDiagnosisById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {

        log.info("接收到获取诊断报告请求: id={}", id);

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        DiagnosisReportVO response = diagnosisService.getDiagnosisById(id, userId);

        return Result.success(response);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:51
     * @Param: manuId 制造企业ID authHeader Authorization头，包含JWT token
     * @Return: Result<DiagnosisReportVO> 统一返回格式的最新诊断报告
     * @Description: 获取企业最新诊断报告接口
    **/
    @GetMapping("/latest")
    public Result<DiagnosisReportVO> getLatestDiagnosis(
            @RequestParam("manuId") Long manuId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("接收到获取企业最新诊断报告请求: manuId={}", manuId);

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);

        DiagnosisReportVO response = diagnosisService.getLatestDiagnosis(manuId, userId);

        return Result.success(response);
    }
}
