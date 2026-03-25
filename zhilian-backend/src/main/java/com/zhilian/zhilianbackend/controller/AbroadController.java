package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/26 00:03
 * @Param:
 * @Return:
 * @Description: 出海服务专区控制器
**/
@Slf4j
@RestController
@RequestMapping("/abroad")
@RequiredArgsConstructor
@Tag(name = "出海服务专区", description = "出海服务相关接口")
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

        try {
            CountryGuideResponse response = countryGuideService.getByCountry(country);
            return Result.success(response);
        } catch (BusinessException e) {
            log.warn("获取国家准入指南业务异常，country: {}, message: {}", country, e.getMessage());
            // 根据业务异常的状态码返回对应的响应
            if (e.getCode() == 404) {
                return Result.notFound(e.getMessage());
            } else if (e.getCode() == 400) {
                return Result.badRequest(e.getMessage());
            }
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取国家准入指南发生未知错误，country: {}", country, e);
            return Result.error("获取国家准入指南失败，请稍后重试");
        }
    }
}