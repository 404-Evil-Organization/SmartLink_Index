package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.*;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.*;
import com.zhilian.zhilianbackend.service.CooperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperationServiceImpl extends ServiceImpl<CooperationMapper, Cooperation> implements CooperationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final DemandMapper demandMapper;
    private final EvaluationMapper evaluationMapper;

    /**
     * 根据用户ID和角色获取默认企业ID
     */
    private Long getDefaultCompanyId(Long userId, String role) {
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

    /**
     * 校验指定企业是否属于当前用户，并返回该企业的角色类型
     */
    private String validateAndGetRoleByEnterpriseId(Long enterpriseId, Long userId) {
        // 检查制造企业
        LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
        manuWrapper.eq(Manufacture::getId, enterpriseId).eq(Manufacture::getUserId, userId);
        if (manufactureMapper.selectCount(manuWrapper) > 0) {
            return "manufacture";
        }
        // 检查服务商
        LambdaQueryWrapper<ServiceProvider> spWrapper = new LambdaQueryWrapper<>();
        spWrapper.eq(ServiceProvider::getId, enterpriseId).eq(ServiceProvider::getUserId, userId);
        if (serviceProviderMapper.selectCount(spWrapper) > 0) {
            return "service";
        }
        return null;
    }

    @Override
    public PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String userRole, Long enterpriseId, String status, Integer page, Integer size) {
        Long companyId;
        String role;

        if (enterpriseId != null) {
            // 指定了企业ID，校验归属
            String enterpriseRole = validateAndGetRoleByEnterpriseId(enterpriseId, userId);
            if (enterpriseRole == null) {
                throw new BusinessException(403, "无权访问该企业");
            }
            companyId = enterpriseId;
            role = enterpriseRole;
        } else {
            // 未指定企业ID，使用默认企业（根据当前用户角色）
            companyId = getDefaultCompanyId(userId, userRole);
            role = userRole;
            if (companyId == null) {
                // 用户没有关联企业，返回空列表
                return PageResult.from(new Page<>(page, size));
            }
        }

        Page<CooperationRecordVO> pageParam = new Page<>(page, size);
        IPage<CooperationRecordVO> iPage = cooperationMapper.selectMyCooperations(pageParam, companyId, role, userId, status);
        return PageResult.from(iPage);
    }

    @Override
    public CooperationDetailVO getCooperationDetail(Long cooperationId, Long userId, String userRole) {
        // 1. 查询合作记录
        Cooperation cooperation = cooperationMapper.selectById(cooperationId);
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }

        // 2. 权限校验：当前用户必须是该合作的一方（制造企业或服务商）
        boolean authorized = false;
        Long userCompanyId = null;
        if ("manufacture".equals(userRole)) {
            LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Manufacture::getUserId, userId);
            Manufacture manufacture = manufactureMapper.selectOne(wrapper);
            if (manufacture != null && manufacture.getId().equals(cooperation.getManuId())) {
                authorized = true;
                userCompanyId = manufacture.getId();
            }
        } else if ("service".equals(userRole)) {
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getUserId, userId);
            ServiceProvider sp = serviceProviderMapper.selectOne(wrapper);
            if (sp != null && sp.getId().equals(cooperation.getServiceId())) {
                authorized = true;
                userCompanyId = sp.getId();
            }
        }

        if (!authorized) {
            throw new BusinessException(403, "无权查看该合作记录");
        }

        // 3. 查询关联信息
        String manuName = null;
        if (cooperation.getManuId() != null) {
            Manufacture manufacture = manufactureMapper.selectById(cooperation.getManuId());
            manuName = manufacture != null ? manufacture.getCompanyName() : null;
        }

        String serviceName = null;
        if (cooperation.getServiceId() != null) {
            ServiceProvider sp = serviceProviderMapper.selectById(cooperation.getServiceId());
            serviceName = sp != null ? sp.getCompanyName() : null;
        }

        String demandTitle = null;
        String demandDescription = null;
        if (cooperation.getDemandId() != null) {
            Demand demand = demandMapper.selectById(cooperation.getDemandId());
            if (demand != null) {
                demandTitle = demand.getTitle();
                demandDescription = demand.getDescription();
            }
        }

        // 4. 查询是否已评价（基于当前用户）
        LambdaQueryWrapper<Evaluation> evaluationWrapper = new LambdaQueryWrapper<>();
        evaluationWrapper.eq(Evaluation::getCoopId, cooperationId)
                .eq(Evaluation::getEvaluatorId, userId)
                .eq(Evaluation::getDeleted, "1970-01-01 00:00:00");
        boolean hasEvaluated = evaluationMapper.selectCount(evaluationWrapper) > 0;

        // 5. 构建返回对象
        return CooperationDetailVO.builder()
                .id(cooperation.getId())
                .manuId(cooperation.getManuId())
                .manuName(manuName)
                .serviceId(cooperation.getServiceId())
                .serviceName(serviceName)
                .demandId(cooperation.getDemandId())
                .demandTitle(demandTitle)
                .demandDescription(demandDescription)
                .amount(cooperation.getAmount())
                .startDate(cooperation.getStartDate())
                .endDate(cooperation.getEndDate())
                .description(cooperation.getDescription())
                .status(cooperation.getStatus())
                .createTime(cooperation.getCreateTime())
                .hasEvaluated(hasEvaluated)
                .build();
    }
}