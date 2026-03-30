package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.*;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.*;
import com.zhilian.zhilianbackend.service.CooperationService;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/22 22:00
 * @Description: 合作记录业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CooperationServiceImpl extends ServiceImpl<CooperationMapper, Cooperation> implements CooperationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final DemandMapper demandMapper;
    private final EvaluationMapper evaluationMapper;
    private final DemandService demandService;
    private final SecurityUtils securityUtils;

    // ==================== 非管理员方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: userId 当前用户ID
     * @Param: userRole 当前用户角色
     * @Param: enterpriseId 企业ID（可选）
     * @Param: status 状态筛选
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: 分页的合作记录列表
     * @Description: 分页查询当前用户的合作记录（非管理员）
     */
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
        IPage<CooperationRecordVO> iPage = cooperationMapper.selectMyCooperations(
                pageParam, companyId, role, userId, status, DateConstants.getNotDeletedLocalDateTime()
        );
        return PageResult.from(iPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: cooperationId 合作记录ID
     * @Param: userId 当前用户ID
     * @Param: userRole 当前用户角色
     * @Return: 合作记录详情
     * @Description: 获取合作记录详情（非管理员，需校验权限）
     */
    @Override
    public CooperationDetailVO getCooperationDetail(Long cooperationId, Long userId, String userRole) {
        // 1. 查询合作记录
        Cooperation cooperation = cooperationMapper.selectById(cooperationId);
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }

        // 2. 权限校验：当前用户必须是该合作的一方
        boolean authorized = false;
        if ("manufacture".equals(userRole)) {
            LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Manufacture::getUserId, userId);
            List<Manufacture> manufactures = manufactureMapper.selectList(wrapper);
            for (Manufacture manufacture : manufactures) {
                if (manufacture.getId().equals(cooperation.getManuId())) {
                    authorized = true;
                    break;
                }
            }
        } else if ("service".equals(userRole)) {
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getUserId, userId);
            List<ServiceProvider> serviceProviders = serviceProviderMapper.selectList(wrapper);
            for (ServiceProvider sp : serviceProviders) {
                if (sp.getId().equals(cooperation.getServiceId())) {
                    authorized = true;
                    break;
                }
            }
        }

        if (!authorized) {
            throw new BusinessException(403, "无权查看该合作记录");
        }

        return buildCooperationDetail(cooperation, userId);
    }

    // ==================== 管理员方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: userId 当前用户ID
     * @Param: enterpriseId 企业ID（可选）
     * @Param: status 状态筛选
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: 分页的合作记录列表
     * @Description: 分页查询合作记录（管理员专用，不限制企业）
     */
    @Override
    public PageResult<CooperationRecordVO> pageMyCooperationsAdmin(Long userId, Long enterpriseId, String status, Integer page, Integer size) {
        Page<CooperationRecordVO> pageParam = new Page<>(page, size);
        IPage<CooperationRecordVO> iPage = cooperationMapper.selectMyCooperationsAdmin(
                pageParam, enterpriseId, status, userId, DateConstants.getNotDeletedLocalDateTime()
        );
        return PageResult.from(iPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: cooperationId 合作记录ID
     * @Param: userId 当前用户ID
     * @Return: 合作记录详情
     * @Description: 获取合作记录详情（管理员专用，无权限校验）
     */
    @Override
    public CooperationDetailVO getCooperationDetailAdmin(Long cooperationId, Long userId) {
        Cooperation cooperation = cooperationMapper.selectById(cooperationId);
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }
        return buildCooperationDetail(cooperation, userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: userId 当前登录用户ID
     * @Param: role 用户角色（manufacture/service）
     * @Return: 企业ID（制造企业ID或服务商ID），若无则返回null
     * @Description: 根据用户ID和角色获取其关联的默认企业ID
     */
    private Long getDefaultCompanyId(Long userId, String role) {
        if ("manufacture".equals(role)) {
            LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Manufacture::getUserId, userId);
            List<Manufacture> manufactures = manufactureMapper.selectList(wrapper);
            return manufactures != null && !manufactures.isEmpty() ? manufactures.get(0).getId() : null;
        } else if ("service".equals(role)) {
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getUserId, userId);
            List<ServiceProvider> serviceProviders = serviceProviderMapper.selectList(wrapper);
            return serviceProviders != null && !serviceProviders.isEmpty() ? serviceProviders.get(0).getId() : null;
        }
        return null;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: enterpriseId 企业ID
     * @Param: userId 当前登录用户ID
     * @Return: 企业类型（"manufacture"或"service"），若不属当前用户则返回null
     * @Description: 校验指定企业是否属于当前用户，并返回该企业的角色类型
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

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: cooperation 合作实体
     * @Param: currentUserId 当前登录用户ID
     * @Return: 合作详情VO
     * @Description: 构建合作记录详情对象（包含双方名称、需求详情、是否已评价等）
     */
    private CooperationDetailVO buildCooperationDetail(Cooperation cooperation, Long currentUserId) {
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

        // 查询当前用户是否已评价该合作（逻辑删除由 @TableLogic 自动处理，无需手动添加 deleted 条件）
        LambdaQueryWrapper<Evaluation> evaluationWrapper = new LambdaQueryWrapper<>();
        evaluationWrapper.eq(Evaluation::getCoopId, cooperation.getId())
                .eq(Evaluation::getEvaluatorId, currentUserId);
        boolean hasEvaluated = evaluationMapper.selectCount(evaluationWrapper) > 0;

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

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: cooperationId 合作记录ID
     * @Param: currentUserId 当前用户ID
     * @Param: currentUserRole 当前用户角色
     * @Return: 无
     * @Description: 取消合作，仅合作双方或管理员可操作，成功后恢复关联需求状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCooperation(Long cooperationId, Long currentUserId, String currentUserRole) {
        // 1. 查询合作记录，用于权限校验和获取 demandId
        Cooperation cooperation = cooperationMapper.selectById(cooperationId);
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }
        // 状态校验（并发安全由条件更新保证，但先校验可以提前返回）
        if (!"ongoing".equals(cooperation.getStatus())) {
            throw new BusinessException(409, "当前合作状态不允许取消");
        }

        // 权限校验
        boolean authorized = false;
        if ("manufacture".equals(currentUserRole)) {
            LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Manufacture::getUserId, currentUserId);
            List<Manufacture> manufactures = manufactureMapper.selectList(wrapper);
            for (Manufacture manufacture : manufactures) {
                if (manufacture.getId().equals(cooperation.getManuId())) {
                    authorized = true;
                    break;
                }
            }
        } else if ("service".equals(currentUserRole)) {
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getUserId, currentUserId);
            List<ServiceProvider> serviceProviders = serviceProviderMapper.selectList(wrapper);
            for (ServiceProvider sp : serviceProviders) {
                if (sp.getId().equals(cooperation.getServiceId())) {
                    authorized = true;
                    break;
                }
            }
        }
        if (!authorized && !securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权取消该合作");
        }

        // 使用条件更新，避免并发重复取消，并确保仅更新未被逻辑删除的记录
        LambdaUpdateWrapper<Cooperation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cooperation::getId, cooperationId)
                .eq(Cooperation::getStatus, "ongoing")
                .eq(Cooperation::getDeleted, DateConstants.getNotDeletedTime())
                .set(Cooperation::getStatus, "cancelled")
                .set(Cooperation::getUpdateTime, new Date());
        int updateRows = cooperationMapper.update(null, updateWrapper);
        if (updateRows == 0) {
            throw new BusinessException(409, "合作状态已被变更，无法取消");
        }

        // 恢复需求状态：resetDemandStatusToPublished 内部已包含需求存在性、状态校验及更新结果校验，
        // 任何失败都会抛出 BusinessException 回滚事务，无需在此额外查询。
        if (cooperation.getDemandId() != null) {
            demandService.resetDemandStatusToPublished(cooperation.getDemandId());
        }

        log.info("合作取消成功，合作ID: {}, 用户ID: {}, 角色: {}", cooperationId, currentUserId, currentUserRole);
    }
}