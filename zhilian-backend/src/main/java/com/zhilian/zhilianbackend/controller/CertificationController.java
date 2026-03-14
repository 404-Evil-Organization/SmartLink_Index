package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.CertificationQueryRequest;
import com.zhilian.zhilianbackend.dto.request.CertificationUpdateRequest;
import com.zhilian.zhilianbackend.dto.request.CertificationUploadRequest;
import com.zhilian.zhilianbackend.dto.response.CertificationVO;
import com.zhilian.zhilianbackend.entity.Certification;
import com.zhilian.zhilianbackend.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 资质证书管理控制器，提供证书的增删改查接口
 **/
@Slf4j
@RestController
@RequestMapping("/certification")
@RequiredArgsConstructor
@Tag(name = "资质证书管理", description = "证书上传、列表、修改、删除等接口")
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: request 证书查询请求（含serviceId筛选）
     * @Return: Result<Map<String, Object>> 证书列表（带分页信息）
     * @Description: 获取资质证书列表（可按serviceId筛选）
     **/
    @GetMapping("/list")
    @Operation(summary = "获取证书列表", description = "可按serviceId筛选证书列表")
    public Result<Map<String, Object>> list(CertificationQueryRequest request) {
        log.info("查询证书列表, 请求参数: serviceId={}, page={}, size={}",
                request.getServiceId(), request.getPage(), request.getSize());

        // 构建查询条件
        LambdaQueryWrapper<Certification> wrapper = new LambdaQueryWrapper<>();
        if (request.getServiceId() != null) {
            wrapper.eq(Certification::getServiceId, request.getServiceId());
        }
        wrapper.orderByDesc(Certification::getCreateTime);

        // 分页查询
        Page<Certification> page = new Page<>(request.getPage(), request.getSize());
        Page<Certification> pageResult = certificationService.page(page, wrapper);

        // 转换为VO
        List<CertificationVO> records = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 封装分页数据到Map中
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("records", records);
        data.put("page", request.getPage());
        data.put("size", request.getSize());

        log.info("查询证书列表成功, 总记录数: {}, 当前页记录数: {}", pageResult.getTotal(), records.size());
        return Result.success(data);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: request 证书上传请求
     * @Return: Result<Long> 新创建的证书ID
     * @Description: 上传资质证书（先只保存到数据库，不包含文件上传）
     **/
    @PostMapping("/upload")
    @Operation(summary = "上传证书", description = "创建证书记录（暂不含文件上传）")
    public Result<Long> upload(@Valid @RequestBody CertificationUploadRequest request) {
        log.info("上传证书, 请求参数: {}", request);

        // 创建证书实体
        Certification certification = new Certification();
        BeanUtils.copyProperties(request, certification);

        // 设置默认值
        certification.setStatus((byte) 1); // 默认有效
        // createTime 和 updateTime 由自动填充处理

        // 保存到数据库
        certificationService.save(certification);

        log.info("证书上传成功, 证书ID: {}", certification.getId());
        return Result.success(certification.getId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: id 证书ID
     * @Param: request 证书更新请求
     * @Return: Result<Void>
     * @Description: 更新证书信息
     **/
    @PutMapping("/{id}")
    @Operation(summary = "更新证书", description = "修改证书信息")
    public Result<Void> update(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CertificationUpdateRequest request) {
        log.info("更新证书, 证书ID: {}, 请求参数: {}", id, request);

        // 检查证书是否存在
        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        // 更新字段
        Certification certification = new Certification();
        BeanUtils.copyProperties(request, certification);
        certification.setId(id);
        // updateTime 由自动填充处理

        // 更新到数据库
        certificationService.updateById(certification);

        log.info("证书更新成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: id 证书ID
     * @Return: Result<Void>
     * @Description: 删除证书（逻辑删除）
     **/
    @DeleteMapping("/{id}")
    @Operation(summary = "删除证书", description = "逻辑删除证书记录")
    public Result<Void> delete(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        log.info("删除证书, 证书ID: {}", id);

        // 检查证书是否存在
        Certification existing = certificationService.getById(id);
        if (existing == null) {
            log.warn("证书不存在, 证书ID: {}", id);
            return Result.notFound("证书不存在");
        }

        // 逻辑删除（deleted 字段会自动填充为当前时间）
        certificationService.removeById(id);

        log.info("证书删除成功, 证书ID: {}", id);
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/14 15:30
     * @Param: certification 证书实体
     * @Return: CertificationVO 证书VO对象
     * @Description: 将证书实体转换为VO对象
     **/
    private CertificationVO convertToVO(Certification certification) {
        if (certification == null) {
            return null;
        }
        CertificationVO vo = new CertificationVO();
        BeanUtils.copyProperties(certification, vo);
        return vo;
    }
}