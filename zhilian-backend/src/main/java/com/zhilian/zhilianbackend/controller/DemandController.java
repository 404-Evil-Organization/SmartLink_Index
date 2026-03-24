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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    /**
     * 发布需求
     */
    @PostMapping("/publish")
    public Result<DemandPublishResponse> publishDemand(@Valid @RequestBody DemandPublishRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        DemandPublishResponse response = demandService.publishDemand(request, userId);
        return Result.success(response);
    }

    /**
     * 编辑需求
     */
    @PutMapping("/{id}")
    public Result<Void> updateDemand(@PathVariable Long id,
                                     @Valid @RequestBody DemandUpdateRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.updateDemand(id, request, userId);
        return Result.success();
    }

    /**
     * 删除需求（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDemand(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        demandService.deleteDemand(id, userId);
        return Result.success();
    }

    /**
     * 获取我的需求列表
     */
    @GetMapping("/my-list")
    public Result<PageResult<DemandMyListVO>> getMyDemandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long manuId,
            @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        PageResult<DemandMyListVO> pageResult = demandService.getMyDemandList(page, size, manuId, status, userId);
        return Result.success(pageResult);
    }
}