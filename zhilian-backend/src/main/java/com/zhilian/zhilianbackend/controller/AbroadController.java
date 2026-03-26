package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/abroad")
@RequiredArgsConstructor
@Tag(name = "出海业务模块", description = "出海服务商、成功案例")
public class AbroadController {

    private final AbroadCaseService abroadCaseService;

    @GetMapping("/services")
    @Operation(summary = "获取提供出海服务的服务商列表", description = "支持按服务类型筛选，公开接口")
    public Result<List<AbroadServiceVO>> getAbroadServices(@Valid AbroadServiceQueryRequest request) {
        log.debug("接收到获取出海服务商请求，参数：{}", request);
        List<AbroadServiceVO> list = abroadCaseService.getAbroadServices(request);
        return Result.success(list);
    }

    @GetMapping("/cases")
    @Operation(summary = "获取成功案例列表", description = "支持按国家、服务类型筛选，支持分页，公开接口")
    public Result<PageResult<AbroadCaseVO>> getAbroadCases(@Validated AbroadCaseQueryRequest request) {
        log.debug("接收到获取成功案例请求，参数：{}", request);
        PageResult<AbroadCaseVO> pageResult = abroadCaseService.getAbroadCases(request);
        return Result.success(pageResult);
    }
}