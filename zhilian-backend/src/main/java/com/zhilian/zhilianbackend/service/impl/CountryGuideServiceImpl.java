package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.CountryGuideCreateRequest;
import com.zhilian.zhilianbackend.dto.request.CountryGuideUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CountryGuideMapper;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryGuideServiceImpl extends ServiceImpl<CountryGuideMapper, CountryGuide> implements CountryGuideService {

    private final SecurityUtils securityUtils;

    // 分页每页最大条数限制
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: country 国家名称（模糊匹配）
     * @Return: PageResult<CountryGuideResponse> 分页结果
     * @Description: 分页查询国家指南列表，仅管理员可访问
     **/
    @Override
    public PageResult<CountryGuideResponse> listByPage(Integer page, Integer size, String country) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 分页参数默认值
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        // 限制最大分页大小，避免恶意请求
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Page<CountryGuide> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(country)) {
            wrapper.like(CountryGuide::getCountry, country);
        }
        wrapper.orderByDesc(CountryGuide::getCreateTime);

        Page<CountryGuide> resultPage = this.page(mpPage, wrapper);

        List<CountryGuideResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), records, resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: request 新增国家指南请求参数
     * @Return: Long 新增记录的ID
     * @Description: 新增国家指南，校验唯一性，仅管理员可操作
     **/
    @Override
    @Transactional
    public Long create(CountryGuideCreateRequest request) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 基础参数校验：请求体与国家名称不能为空，且长度限制为 50 个字符以内
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        String country = request.getCountry();
        if (!StringUtils.hasText(country)) {
            throw new BusinessException(400, "国家名称不能为空");
        }
        if (country.length() > 50) {
            throw new BusinessException(400, "国家名称长度不能超过50个字符");
        }
        // 检查国家名称是否已存在（未删除）
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CountryGuide::getCountry, country);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(409, "国家名称已存在");
        }

        CountryGuide entity = new CountryGuide();
        BeanUtils.copyProperties(request, entity);
        // 设置逻辑删除字段为未删除状态（使用常量）
        entity.setDeleted(DateConstants.getNotDeletedTime());

        this.save(entity);
        return entity.getId();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Param: request 修改国家指南请求参数
     * @Return: void
     * @Description: 修改国家指南，支持部分更新，仅管理员可操作
     **/
    @Override
    @Transactional
    public void update(Long id, CountryGuideUpdateRequest request) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 查询原记录（未删除）
        CountryGuide existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "国家指南不存在");
        }

        // 如果修改了国家名称，检查唯一性
        if (StringUtils.hasText(request.getCountry()) && !request.getCountry().equals(existing.getCountry())) {
            LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CountryGuide::getCountry, request.getCountry());
            long count = this.count(wrapper);
            if (count > 0) {
                throw new BusinessException(409, "国家名称已存在");
            }
            existing.setCountry(request.getCountry());
        }

        // 更新其他字段
        if (request.getRequirements() != null) {
            existing.setRequirements(request.getRequirements());
        }
        if (request.getProcess() != null) {
            existing.setProcess(request.getProcess());
        }
        if (request.getDocuments() != null) {
            existing.setDocuments(request.getDocuments());
        }

        try {
            this.updateById(existing);
        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突（并发场景下修改为国家名称已存在）
            throw new BusinessException(409, "国家名称已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Return: void
     * @Description: 逻辑删除国家指南，仅管理员可操作
     **/
    @Override
    @Transactional
    public void delete(Long id) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 逻辑删除（MyBatis Plus会自动设置deleted为当前时间）
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(404, "国家指南不存在或已删除");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: entity 国家指南实体
     * @Return: CountryGuideResponse 响应对象
     * @Description: 将实体对象转换为响应对象
     **/
    private CountryGuideResponse convertToResponse(CountryGuide entity) {
        CountryGuideResponse response = new CountryGuideResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}