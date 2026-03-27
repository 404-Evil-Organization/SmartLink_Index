package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CountryGuideCreateRequest;
import com.zhilian.zhilianbackend.dto.request.CountryGuideUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/country-guide")
@RequiredArgsConstructor
@Tag(name = "国家指南管理", description = "管理员对国家准入指南的CRUD操作")
public class CountryGuideController {

    private final CountryGuideService countryGuideService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: page 页码，默认1
     * @Param: size 每页条数，默认10
     * @Param: country 国家名称（模糊匹配）
     * @Return: Result<PageResult<CountryGuideResponse>> 分页结果
     * @Description: 获取国家指南列表（分页）
     **/
    @GetMapping("/list")
    @Operation(summary = "获取国家指南列表（分页）")
    public Result<PageResult<CountryGuideResponse>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String country) {
        PageResult<CountryGuideResponse> pageResult = countryGuideService.listByPage(page, size, country);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: request 新增国家指南请求参数
     * @Return: Result<Map<String, Long>> 包含新增记录ID的响应
     * @Description: 新增国家指南
     **/
    @PostMapping
    @Operation(summary = "新增国家指南")
    public Result<Map<String, Long>> create(@Valid @RequestBody CountryGuideCreateRequest request) {
        Long id = countryGuideService.create(request);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Param: request 修改国家指南请求参数
     * @Return: Result<Void> 无数据响应
     * @Description: 修改国家指南
     **/
    @PutMapping("/{id}")
    @Operation(summary = "修改国家指南")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CountryGuideUpdateRequest request) {
        countryGuideService.update(id, request);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Return: Result<Void> 无数据响应
     * @Description: 删除国家指南（逻辑删除）
     **/
    @DeleteMapping("/{id}")
    @Operation(summary = "删除国家指南（逻辑删除）")
    public Result<Void> delete(@PathVariable Long id) {
        countryGuideService.delete(id);
        return Result.success();
    }
}