package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderAddVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderDetailVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderListVO;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 01:02
 * @Param:
 * @Return:
 * @Description: 服务商管理 Controller
 **/
@Slf4j
@RestController
@RequestMapping("/service-provider")
@RequiredArgsConstructor
@Tag(name = "服务商管理", description = "服务商相关接口")
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: requestDTO 查询请求参数
     * @Return: Result<IPage<ServiceProviderListVO>> 分页列表结果
     * @Description: 获取服务商列表
     **/
    @GetMapping("/list")
    @Operation(summary = "获取服务商列表", description = "分页查询服务商列表，支持区域、服务大类筛选")
    public Result<IPage<ServiceProviderListVO>> getServiceProviderList(ServiceProviderListRequestDTO requestDTO) {
        log.info("获取服务商列表，请求参数：{}", requestDTO);
        IPage<ServiceProviderListVO> pageResult = serviceProviderService.getServiceProviderList(requestDTO);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Return: Result<ServiceProviderDetailVO> 服务商详情
     * @Description: 获取服务商详情
     **/
    @GetMapping("/{id}")
    @Operation(summary = "获取服务商详情", description = "根据ID获取服务商详细信息")
    public Result<ServiceProviderDetailVO> getServiceProviderDetail(
            @Parameter(description = "服务商ID", required = true, example = "2001")
            @PathVariable("id") Long id) {
        log.info("获取服务商详情，服务商ID：{}", id);
        ServiceProviderDetailVO detail = serviceProviderService.getServiceProviderDetail(id);
        return Result.success(detail);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: requestDTO 新增服务商请求参数
     * @Return: Result<ServiceProviderAddVO> 新增结果（返回新ID）
     * @Description: 新增服务商
     **/
    @PostMapping
    @Operation(summary = "新增服务商", description = "创建新的服务商信息")
    public Result<ServiceProviderAddVO> addServiceProvider(@Valid @RequestBody ServiceProviderAddRequestDTO requestDTO) {
        log.info("新增服务商，请求参数：{}", requestDTO);
        ServiceProviderAddVO result = serviceProviderService.addServiceProvider(requestDTO);
        return Result.success("新增成功", result);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Param: requestDTO 修改服务商请求参数
     * @Return: Result<Void> 修改结果
     * @Description: 修改服务商信息
     **/
    @PutMapping("/{id}")
    @Operation(summary = "修改服务商", description = "根据ID修改服务商信息，只传需要修改的字段")
    public Result<Void> updateServiceProvider(
            @Parameter(description = "服务商ID", required = true, example = "2010")
            @PathVariable("id") Long id,
            @Valid @RequestBody ServiceProviderUpdateRequestDTO requestDTO) {
        log.info("修改服务商，服务商ID：{}，请求参数：{}", id, requestDTO);
        serviceProviderService.updateServiceProvider(id, requestDTO);
        return Result.success("修改成功");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:02
     * @Param: id 服务商ID
     * @Return: Result<Void> 删除结果
     * @Description: 删除服务商（逻辑删除）
     **/
    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务商", description = "根据ID删除服务商（逻辑删除）")
    public Result<Void> deleteServiceProvider(
            @Parameter(description = "服务商ID", required = true, example = "2010")
            @PathVariable("id") Long id) {
        log.info("删除服务商，服务商ID：{}", id);
        serviceProviderService.deleteServiceProvider(id);
        return Result.success("删除成功");
    }
}