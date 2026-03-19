package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.CooperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CooperationServiceImpl extends ServiceImpl<CooperationMapper, Cooperation> implements CooperationService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final CooperationMapper cooperationMapper;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 17:30
     * @Description: 根据用户ID和角色获取对应的企业ID
     */
    private Long getCompanyIdByUserIdAndRole(Long userId, String role) {
        if ("manufacture".equals(role)) {
            LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Manufacture::getUserId, userId);
            Manufacture manufacture = manufactureMapper.selectOne(wrapper);
            return manufacture != null ? manufacture.getId() : null;
        } else if ("service".equals(role)) {
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getUserId, userId);
            ServiceProvider sp = serviceProviderMapper.selectOne(wrapper);
            return sp != null ? sp.getId() : null;
        }
        return null;
    }

    @Override
    public PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String role, String status, Integer page, Integer size) {
        // 1. 获取当前用户对应的企业ID
        Long companyId = getCompanyIdByUserIdAndRole(userId, role);
        if (companyId == null) {
            // 企业信息不存在，返回空列表（手动构造空 PageResult）
            return new PageResult<>(0L, Collections.emptyList(), page.longValue(), size.longValue());
        }

        // 2. 创建分页对象
        Page<CooperationRecordVO> pageParam = new Page<>(page, size);

        // 3. 执行自定义分页查询
        IPage<CooperationRecordVO> iPage = cooperationMapper.selectMyCooperations(pageParam, companyId, role, userId, status);

        // 4. 转换为 PageResult（使用已有的 from 方法）
        return PageResult.from(iPage);
    }
}