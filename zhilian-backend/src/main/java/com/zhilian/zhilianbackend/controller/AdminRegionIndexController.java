package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.annotation.LogOperation;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.AdminRegionIndexListRequest;
import com.zhilian.zhilianbackend.dto.request.RegionIndexCreateRequest;
import com.zhilian.zhilianbackend.dto.request.RegionIndexUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.RegionIndexAdminVO;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员 - 区域指数管理", description = "管理员对区域指数数据进行增删改查")
@RestController
@RequestMapping("/admin/region-index")
@RequiredArgsConstructor
public class AdminRegionIndexController {

    private final RegionIndexService regionIndexService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: request 分页及筛选参数
     * @Return: 分页结果，包含区域指数列表
     * @Description: 管理员分页获取区域指数列表，支持按区域、年份筛选
     */
    @LogOperation("获取区域指数列表（管理员）")
    @Operation(summary = "获取区域指数列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<RegionIndexAdminVO>> list(@Valid AdminRegionIndexListRequest request) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        IPage<RegionIndexAdminVO> page = regionIndexService.adminList(request);
        return Result.success(PageResult.from(page));
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: request 新增区域指数请求参数
     * @Return: 新增记录的ID
     * @Description: 管理员新增区域指数，需保证区域+年份+季度组合唯一
     */
    @LogOperation("新增区域指数")
    @Operation(summary = "新增区域指数")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid RegionIndexCreateRequest request) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        Long id = regionIndexService.adminCreate(request);
        return Result.success("新增成功", id);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: id 记录ID
     * @Param: request 修改区域指数请求参数（部分字段可选）
     * @Return: 无返回数据
     * @Description: 管理员修改区域指数，若修改区域/年份/季度需校验唯一性
     */
    @LogOperation("修改区域指数")
    @Operation(summary = "修改区域指数")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid RegionIndexUpdateRequest request) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        regionIndexService.adminUpdate(id, request);
        return Result.success("修改成功");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: id 记录ID
     * @Return: 无返回数据
     * @Description: 管理员删除区域指数（逻辑删除）
     */
    @LogOperation("删除区域指数")
    @Operation(summary = "删除区域指数")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Parameter(description = "记录ID") Long id) {
        if (!securityUtils.isAdmin()) {
            return Result.forbidden("无权限访问");
        }
        regionIndexService.adminDelete(id);
        return Result.success("删除成功");
    }
}