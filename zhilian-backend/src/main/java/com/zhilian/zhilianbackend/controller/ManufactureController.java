package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.ManufactureAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ManufactureAddVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureListVO;
import com.zhilian.zhilianbackend.annotation.LogOperation;
import com.zhilian.zhilianbackend.service.ManufactureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:32
 * @Param:
 * @Return:
 * @Description: 制造企业管理 Controller
 **/
@Slf4j
@RestController
@RequestMapping("/manufacture")
@RequiredArgsConstructor
@Tag(name = "制造企业管理", description = "制造企业相关接口")
@Validated
public class ManufactureController {

    private final ManufactureService manufactureService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: requestDTO 查询请求参数
     * @Return: Result<PageResult<ManufactureListVO>> 分页列表结果
     * @Description: 获取制造企业列表
     **/
    @LogOperation("获取制造企业列表")
    @GetMapping("/list")
    @Operation(summary = "获取制造企业列表", description = "分页查询制造企业列表，支持区域、规模、产品类型筛选")
    public Result<PageResult<ManufactureListVO>> getManufactureList(@Valid ManufactureListRequestDTO requestDTO) {
        log.info("获取制造企业列表，请求参数：{}", requestDTO);
        PageResult<ManufactureListVO> pageResult = PageResult.from(manufactureService.getManufactureList(requestDTO));
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID
     * @Return: Result<ManufactureDetailVO> 企业详情
     * @Description: 获取制造企业详情
     **/
    @LogOperation("获取制造企业详情")
    @GetMapping("/{id}")
    @Operation(summary = "获取制造企业详情", description = "根据ID获取制造企业详细信息")
    public Result<ManufactureDetailVO> getManufactureDetail(
            @Parameter(description = "企业ID", required = true, example = "1001")
            @PathVariable("id") Long id) {
        log.info("获取制造企业详情，企业ID：{}", id);
        ManufactureDetailVO detail = manufactureService.getManufactureDetail(id);
        return Result.success(detail);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: requestDTO 新增企业请求参数
     * @Return: Result<ManufactureAddVO> 新增结果（返回新ID）
     * @Description: 新增制造企业
     **/
    @LogOperation("新增制造企业")
    @PostMapping
    @Operation(summary = "新增制造企业", description = "创建新的制造企业信息")
    public Result<ManufactureAddVO> addManufacture(@Valid @RequestBody ManufactureAddRequestDTO requestDTO) {
        log.info("新增制造企业接口被调用");
        ManufactureAddVO result = manufactureService.addManufacture(requestDTO);
        return Result.success("新增成功", result);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID
     * @Param: requestDTO 修改企业请求参数
     * @Return: Result<Void> 修改结果
     * @Description: 修改制造企业信息
     **/
    @LogOperation("修改制造企业信息")
    @PutMapping("/{id}")
    @Operation(summary = "修改制造企业", description = "根据ID修改制造企业信息，只传需要修改的字段")
    public Result<Void> updateManufacture(
            @Parameter(description = "企业ID", required = true, example = "83")
            @PathVariable("id") Long id,
            @Valid @RequestBody ManufactureUpdateRequestDTO requestDTO) {
        log.info("修改制造企业接口被调用，企业ID：{}", id);
        manufactureService.updateManufacture(id, requestDTO);
        return Result.success("修改成功");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID
     * @Return: Result<Void> 删除结果
     * @Description: 删除制造企业（逻辑删除）
     **/
    @LogOperation("删除制造企业")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除制造企业", description = "根据ID删除制造企业（逻辑删除）")
    public Result<Void> deleteManufacture(
            @Parameter(description = "企业ID", required = true, example = "83")
            @PathVariable("id") Long id) {
        log.info("删除制造企业，企业ID：{}", id);
        manufactureService.deleteManufacture(id);
        return Result.success("删除成功");
    }
}