package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseCreateRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseDetailResponse;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseListResponse;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/25 21:13
 * @Param:
 * @Return:
 * @Description: 管理员出海案例管理接口
 **/
@Slf4j
@RestController
@RequestMapping("/admin/abroad-case")
@RequiredArgsConstructor
@Tag(name = "管理员出海案例管理", description = "出海案例CRUD接口")
public class AdminAbroadCaseController {

    private final AbroadCaseService abroadCaseService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:14
     * @Param: queryRequest 查询请求参数
     * @Return: Result<Page<AbroadCaseListResponse>> 分页案例列表
     * @Description: 获取出海案例列表（管理员）
     **/
    @GetMapping("/list")
    @Operation(summary = "获取出海案例列表")
    public Result<Page<AbroadCaseListResponse>> list(@Valid AbroadCaseQueryRequest queryRequest) {
        // 校验管理员权限
        checkAdminPermission();

        log.info("管理员获取出海案例列表, queryRequest: {}", queryRequest);
        return Result.success(abroadCaseService.listByPage(queryRequest));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:14
     * @Param: id 案例ID
     * @Return: Result<AbroadCaseDetailResponse> 案例详情
     * @Description: 获取出海案例详情
     **/
    @GetMapping("/{id}")
    @Operation(summary = "获取出海案例详情")
    public Result<AbroadCaseDetailResponse> detail(@PathVariable Long id) {
        // 校验管理员权限
        checkAdminPermission();

        log.info("管理员获取出海案例详情, id: {}", id);
        return Result.success(abroadCaseService.getDetail(id));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:14
     * @Param: request 创建请求参数
     * @Return: Result<Long> 新创建的案例ID
     * @Description: 新增出海案例（含封面上传）
     **/
    @PostMapping
    @Operation(summary = "新增出海案例")
    public Result<Long> create(@Valid @ModelAttribute AbroadCaseCreateRequest request) {
        // 校验管理员权限
        checkAdminPermission();

        log.info("管理员新增出海案例, request: {}", request);

        // 获取当前管理员 ID（用于审计记录）
        Long adminId = securityUtils.getCurrentUserId();

        // 仅传递 adminId，adminName 暂传 null，避免使用不准确的固定名称；
        // 如需展示/记录管理员名称，请在服务层通过 adminId 查询真实用户名。
        Long caseId = abroadCaseService.create(request, adminId, null);
        return Result.success(caseId);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:14
     * @Param: id 案例ID  request 更新请求参数
     * @Return:
     * @Description: 修改出海案例（含封面上传，支持部分字段更新）
     **/
    @PutMapping("/{id}")
    @Operation(summary = "修改出海案例")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @ModelAttribute AbroadCaseUpdateRequest request) {
        // 校验管理员权限
        checkAdminPermission();

        log.info("管理员修改出海案例, id: {}, request: {}", id, request);
        abroadCaseService.update(id, request);
        return Result.success();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:15
     * @Param: id 案例ID
     * @Return:
     * @Description: 逻辑删除出海案例
     **/
    @DeleteMapping("/{id}")
    @Operation(summary = "删除出海案例")
    public Result<Void> delete(@PathVariable Long id) {
        // 校验管理员权限
        checkAdminPermission();

        log.info("管理员删除出海案例, id: {}", id);
        abroadCaseService.delete(id);
        return Result.success();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:30
     * @Param:
     * @Return: void
     * @Description: 校验当前用户是否为管理员，不是则抛出403异常
     **/
    private void checkAdminPermission() {
        if (!securityUtils.isAdmin()) {
            log.warn("非管理员用户尝试访问管理员接口");
            throw new BusinessException(403, "无权限访问，需要管理员角色");
        }
    }
}