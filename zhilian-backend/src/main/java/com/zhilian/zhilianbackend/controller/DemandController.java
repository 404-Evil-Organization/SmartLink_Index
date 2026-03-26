package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.request.DemandUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.DemandMyListVO;
import com.zhilian.zhilianbackend.dto.response.DemandPublishResponse;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
@Validated
public class DemandController {

    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: request 发布需求请求参数
     * @Return: Result<DemandPublishResponse> 包含需求ID和审核状态
     * @Description: 发布需求，仅制造企业可操作，且只能为自己的企业发布
     */
    @PostMapping("/publish")
    public Result<DemandPublishResponse> publishDemand(@Valid @RequestBody DemandPublishRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        DemandPublishResponse response = demandService.publishDemand(request, userId);
        return Result.success(response);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: id 需求ID
     * @Param: request 编辑需求请求参数
     * @Return: Result<Void> 无数据返回
     * @Description: 编辑需求，仅制造企业可操作自己发布的需求，管理员可操作任意需求
     */
    @PutMapping("/{id}")
    public Result<Void> updateDemand(@PathVariable Long id,
                                     @Valid @RequestBody DemandUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.updateDemand(id, request, userId);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: id 需求ID
     * @Return: Result<Void> 无数据返回
     * @Description: 逻辑删除需求，仅制造企业可删除自己发布的需求，管理员可删除任意需求
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDemand(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.deleteDemand(id, userId);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Param: page 页码，最小1
     * @Param: size 每页条数，最小1，最大100
     * @Param: manuId 制造企业ID
     * @Param: status 需求状态筛选（可选）
     * @Return: Result<PageResult<DemandMyListVO>> 分页的需求列表
     * @Description: 获取当前用户的需求列表，制造企业只能查看自己的需求，管理员可查看任意企业需求
     */
    @GetMapping("/my-list")
    public Result<PageResult<DemandMyListVO>> getMyDemandList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer size,
            @RequestParam Long manuId,
            @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        PageResult<DemandMyListVO> pageResult = demandService.getMyDemandList(page, size, manuId, status, userId);
        return Result.success(pageResult);
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/26 18:29
     * @Param: id 需求ID
     * @Return: Result<com.zhilian.zhilianbackend.dto.response.DemandDetailVO> 需求详情
     * @Description: 获取需求详情
     */
    @GetMapping("/{id}")
    public Result<com.zhilian.zhilianbackend.dto.response.DemandDetailVO> getDemandDetail(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        return Result.success(demandService.getDemandDetail(id, userId));
    }
}