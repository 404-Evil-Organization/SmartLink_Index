package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import com.zhilian.zhilianbackend.service.AbroadServiceProviderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private final CountryGuideService countryGuideService;

    /**
     * @Author: 6017
     * @Date: 2026/3/25 00:01
     * @Param: country 国家名称（路径参数）
     * @Return: Result<CountryGuideResponse> 统一响应结果，包含国家准入指南数据
     * @Description: 获取特定国家准入指南，根据国家名称返回该国的市场准入要求、办理流程和所需材料
    **/
    @GetMapping("/country/{country}")
    @Operation(summary = "获取特定国家准入指南", description = "根据国家名称获取该国的市场准入要求、办理流程和所需材料")
    public Result<CountryGuideResponse> getCountryGuide(
            @Parameter(description = "国家名称", required = true, example = "美国")
            @PathVariable String country) {

        log.info("接收到获取国家准入指南请求，country: {}", country);

        CountryGuideResponse response = countryGuideService.getByCountry(country);
        return Result.success(response);
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/3/27 18:45
     * @Param: page 页码，默认1
     * @Param: size 每页条数，默认10
     * @Param: keyword 国家名称关键词（模糊匹配）
     * @Return: Result<PageResult<CountryGuideResponse>> 分页结果
     * @Description: 获取国家准入指南列表（公开接口）
     **/
    @GetMapping("/country-guide/list")
    @Operation(summary = "获取国家准入指南列表", description = "支持分页和按国家名称模糊搜索，公开接口")
    public Result<PageResult<CountryGuideResponse>> getCountryGuideList(
            @Parameter(description = "页码，默认1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页条数，默认10") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "国家名称关键词") @RequestParam(required = false) String keyword) {
        log.debug("接收到获取国家准入指南列表请求，page: {}, size: {}, keyword: {}", page, size, keyword);
        PageResult<CountryGuideResponse> pageResult = countryGuideService.publicListByPage(page, size, keyword);
        return Result.success(pageResult);
    }

    private final AbroadServiceProviderService abroadServiceProviderService;
    private final AbroadCaseService abroadCaseService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 服务商查询请求参数（含服务类型、区域筛选及分页）
     * @Return: Result<PageResult<AbroadServiceVO>> 统一响应结果，包含分页的服务商数据
     * @Description: 分页获取提供出海服务且审核通过的服务商列表，支持按服务类型、区域筛选（公开接口）
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