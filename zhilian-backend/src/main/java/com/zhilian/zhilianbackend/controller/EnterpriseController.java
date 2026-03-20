package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.PageRequest;
import com.zhilian.zhilianbackend.dto.response.EnterpriseManufactureVO;
import com.zhilian.zhilianbackend.dto.response.EnterpriseServiceVO;
import com.zhilian.zhilianbackend.service.EnterpriseService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 6017
 * @Date: 2026/3/20 23:56
 * @Param: 
 * @Return: 
 * @Description: 个人企业管理控制器，提供个人制造企业列表和个人服务商列表接口
**/
@RestController
@RequestMapping("/enterprise")
@RequiredArgsConstructor
@Tag(name = "个人企业管理", description = "个人企业相关接口")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: 6017
     * @Date: 2026/3/21 00:01
     * @Param: pageRequest 分页请求参数
     * @Return: 个人制造企业分页列表
     * @Description: 获取个人制造企业列表
    **/
    @GetMapping("/manufacture/list")
    @Operation(summary = "获取个人制造企业列表", description = "返回当前登录用户创建的制造企业列表，包含所有审核状态")
    public Result<PageResult<EnterpriseManufactureVO>> getMyManufactureList(@Valid PageRequest pageRequest) {
        // 获取当前登录用户ID
        Long userId = securityUtils.getCurrentUserId();

        // 分页查询
        Page<EnterpriseManufactureVO> page = enterpriseService.getMyManufactureList(
                userId,
                pageRequest.getPage().longValue(),
                pageRequest.getSize().longValue()
        );

        // 封装分页结果 - 使用page的records
        Page<EnterpriseManufactureVO> mybatisPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mybatisPage.setRecords(page.getRecords());
        PageResult<EnterpriseManufactureVO> pageResult = PageResult.from(mybatisPage);

        return Result.success(pageResult);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 00:01
     * @Param: pageRequest 分页请求参数
     * @Return: 个人服务商分页列表
     * @Description: 获取个人服务商列表
    **/
    @GetMapping("/service/list")
    @Operation(summary = "获取个人服务商列表", description = "返回当前登录用户创建的服务商列表，包含所有审核状态")
    public Result<PageResult<EnterpriseServiceVO>> getMyServiceList(@Valid PageRequest pageRequest) {
        // 获取当前登录用户ID
        Long userId = securityUtils.getCurrentUserId();

        // 分页查询
        Page<EnterpriseServiceVO> page = enterpriseService.getMyServiceList(
                userId,
                pageRequest.getPage().longValue(),
                pageRequest.getSize().longValue()
        );

        // 封装分页结果 - 使用page的records
        Page<EnterpriseServiceVO> mybatisPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mybatisPage.setRecords(page.getRecords());
        PageResult<EnterpriseServiceVO> pageResult = PageResult.from(mybatisPage);

        return Result.success(pageResult);
    }
}