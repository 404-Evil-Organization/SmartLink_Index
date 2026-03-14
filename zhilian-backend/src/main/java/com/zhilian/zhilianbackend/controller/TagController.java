package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:55
 * @Param:
 * @Return:
 * @Description: 标签管理控制器，提供标签的增删改查接口
 **/
@Tag(name = "标签管理接口")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final TagService tagService;

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:44
     * @Param: queryRequest 分页查询参数（包含页码、每页条数、名称模糊查询、类别筛选）
     * @Return: Result<IPage<TagResponse>> 分页后的标签列表
     * @Description: 获取标签列表（分页），支持按名称模糊查询和按类别筛选
    **/
    @Operation(summary = "获取标签列表（分页）")
    @GetMapping("/list")
    public Result<IPage<TagResponse>> list(@Valid TagQueryRequest queryRequest) {
        return Result.success(tagService.pageQuery(queryRequest));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:57
     * @Param: id 标签ID
     * @Return: Result<TagResponse> 标签详情
     * @Description: 根据ID获取标签详细信息
    **/
    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<TagResponse> detail(@PathVariable Long id) {
        return Result.success(tagService.getTagDetail(id));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:57
     * @Param: request 新增标签请求参数
     * @Return: Result<Long> 新增标签的ID
     * @Description: 新增标签，会校验标签名称是否已存在
    **/
    @Operation(summary = "新增标签")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Long> add(@RequestBody @Valid TagRequest request) {
        return Result.success(tagService.addTag(request));
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:58
     * @Param: id 要修改的标签ID;request 修改标签请求参数
     * @Return: Result<Void>
     * @Description: 修改标签信息，如果修改名称会检查新名称是否与其他标签冲突
    **/
    @Operation(summary = "修改标签")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid TagRequest request) {
        tagService.updateTag(id, request);
        return Result.success();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:58
     * @Param: id 要删除的标签ID
     * @Return: Result<Void>
     * @Description: 逻辑删除标签
    **/
    @Operation(summary = "删除标签")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
