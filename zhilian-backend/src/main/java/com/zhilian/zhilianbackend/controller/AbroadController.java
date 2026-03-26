package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import com.zhilian.zhilianbackend.service.AbroadServiceProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/26 15:51
 * @Description: 出海业务控制器
 */
@Slf4j
@RestController
@RequestMapping("/abroad")
@RequiredArgsConstructor
@Tag(name = "出海业务模块", description = "出海服务商、成功案例")
public class AbroadController {

    private final AbroadServiceProviderService abroadServiceProviderService;
    private final AbroadCaseService abroadCaseService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 服务商查询请求参数（含服务类型筛选）
     * @Return: Result<List<AbroadServiceVO>> 统一响应结果，包含服务商列表
     * @Description: 获取提供出海服务的服务商列表（公开接口）
     */
    @GetMapping("/services")
    @Operation(summary = "获取提供出海服务的服务商列表", description = "支持按服务类型、区域筛选，支持分页，公开接口")
    public Result<PageResult<AbroadServiceVO>> getAbroadServices(@Validated AbroadServiceQueryRequest request) {
        log.debug("接收到获取出海服务商请求，参数：{}", request);
        PageResult<AbroadServiceVO> pageResult = abroadServiceProviderService.getAbroadServices(request);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 成功案例查询请求参数（含分页、筛选条件）
     * @Return: Result<PageResult<AbroadCaseVO>> 统一响应结果，包含分页案例数据
     * @Description: 分页获取成功案例列表，支持按国家、服务类型筛选（公开接口）
     */
    @GetMapping("/cases")
    @Operation(summary = "获取成功案例列表", description = "支持按国家、服务类型筛选，支持分页，公开接口")
    public Result<PageResult<AbroadCaseVO>> getAbroadCases(@Validated AbroadCaseQueryRequest request) {
        log.debug("接收到获取成功案例请求，参数：{}", request);
        PageResult<AbroadCaseVO> pageResult = abroadCaseService.getAbroadCases(request);
        return Result.success(pageResult);
    }
}