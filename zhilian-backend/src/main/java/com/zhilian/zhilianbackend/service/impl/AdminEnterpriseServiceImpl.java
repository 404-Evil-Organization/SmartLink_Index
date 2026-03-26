package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.dto.response.PendingEnterpriseResponse;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.AdminEnterpriseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:36
 * @Description: 管理员企业审核服务实现类
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEnterpriseServiceImpl implements AdminEnterpriseService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;

    /**
     * @Author: 6017
     * @Date: 2026/3/25 00:16
     * @Param: page 页码  size 每页条数  IPage<PendingEnterpriseResponse> 待审核企业分页数据
     * @Return:
     * @Description: 取待审核企业列表，合并制造企业和服务商数据，按创建时间倒序排序并手动分页
     **/
    @Override
    public IPage<PendingEnterpriseResponse> getPendingEnterpriseList(Integer page, Integer size) {
        // 基本参数校验，防止非法分页参数造成不必要的压力
        if (page == null || page < 1 || size == null || size < 1) {
            throw new BusinessException("分页参数不合法，page 和 size 必须为大于等于 1 的整数");
        }

        // 创建分页对象（仅作为返回结构封装，不直接用于数据库分页）
        Page<PendingEnterpriseResponse> pageParam = new Page<>(page, size);

        // 查询待审核的制造企业，仅选择列表所需字段，并在数据库侧按创建时间倒序排序
        LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
        manuWrapper
                .select(
                        Manufacture::getId,
                        Manufacture::getCompanyName,
                        Manufacture::getRegion,
                        Manufacture::getContactPerson,
                        Manufacture::getContactPhone,
                        Manufacture::getAuditStatus,
                        Manufacture::getCreateTime,
                        Manufacture::getDeleted
                )
                .eq(Manufacture::getAuditStatus, "pending")
                .eq(Manufacture::getDeleted, DateConstants.getNotDeletedTime())
                .orderByDesc(Manufacture::getCreateTime);
        List<Manufacture> manuList = manufactureMapper.selectList(manuWrapper);

        // 查询待审核的服务商，仅选择列表所需字段，并在数据库侧按创建时间倒序排序
        LambdaQueryWrapper<ServiceProvider> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper
                .select(
                        ServiceProvider::getId,
                        ServiceProvider::getCompanyName,
                        ServiceProvider::getRegion,
                        ServiceProvider::getContactPerson,
                        ServiceProvider::getContactPhone,
                        ServiceProvider::getAuditStatus,
                        ServiceProvider::getCreateTime,
                        ServiceProvider::getDeleted
                )
                .eq(ServiceProvider::getAuditStatus, "pending")
                .orderByDesc(ServiceProvider::getCreateTime);
        List<ServiceProvider> serviceList = serviceProviderMapper.selectList(serviceWrapper);

        // 转换为VO并合并
        List<PendingEnterpriseResponse> allList = new ArrayList<>();

        for (Manufacture manu : manuList) {
            PendingEnterpriseResponse vo = new PendingEnterpriseResponse();
            vo.setId(manu.getId());
            vo.setType("manufacture");
            vo.setCompanyName(manu.getCompanyName());
            vo.setRegion(manu.getRegion());
            vo.setContactPerson(manu.getContactPerson());
            vo.setContactPhone(manu.getContactPhone());
            vo.setAuditStatus(manu.getAuditStatus());
            vo.setCreateTime(manu.getCreateTime());  // 直接使用 Date，无需转换
            allList.add(vo);
        }

        for (ServiceProvider service : serviceList) {
            PendingEnterpriseResponse vo = new PendingEnterpriseResponse();
            vo.setId(service.getId());
            vo.setType("service");
            vo.setCompanyName(service.getCompanyName());
            vo.setRegion(service.getRegion());
            vo.setContactPerson(service.getContactPerson());
            vo.setContactPhone(service.getContactPhone());
            vo.setAuditStatus(service.getAuditStatus());
            vo.setCreateTime(service.getCreateTime());  // 直接使用 Date，无需转换
            allList.add(vo);
        }

        // 按创建时间倒序排序
        allList.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 手动分页，使用 long 计算避免 int 溢出，并在 subList 前做边界保护
        long longStart = ((long) page - 1) * size;
        List<PendingEnterpriseResponse> records;
        if (longStart < 0 || longStart >= allList.size()) {
            // 起始位置越界时直接返回空记录，避免 subList 越界异常
            records = new ArrayList<>();
        } else {
            int start = (int) longStart;
            int end = (int) Math.min(longStart + size, allList.size());
            records = allList.subList(start, end);
        }

        pageParam.setRecords(records);
        pageParam.setTotal(allList.size());

        log.info("查询待审核企业列表成功，总数: {}, 请求页码: {}, 每页大小: {}, 当前页记录数: {}", allList.size(), page, size, records.size());
        return pageParam;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/24 23:37
     * @Param: enterpriseId 企业ID  type 企业类型（manufacture/service）  status 审核状态（approved/rejected）  remark 审核意见  auditUserId 审核人ID
     * @Return:
     * @Description: 审核企业，更新审核状态、审核意见、审核时间和审核人信息
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveEnterprise(Long enterpriseId, String type, String status, String remark, Long auditUserId) {
        // 业务校验：驳回时必须填写审核意见
        if ("rejected".equals(status) && (remark == null || remark.trim().isEmpty())) {
            log.warn("审核驳回失败，未填写驳回原因: enterpriseId={}, type={}", enterpriseId, type);
            throw new BusinessException(400, "审核驳回时必须填写审核意见");
        }

        Date now = new Date();

        if ("manufacture".equals(type)) {
            Manufacture manufacture = manufactureMapper.selectById(enterpriseId);
            if (manufacture == null) {
                log.warn("审核制造企业失败，企业不存在: enterpriseId={}", enterpriseId);
                throw new BusinessException(404, "制造企业不存在");
            }
            if (!"pending".equals(manufacture.getAuditStatus())) {
                log.warn("审核制造企业失败，企业已被审核: enterpriseId={}, currentStatus={}", enterpriseId, manufacture.getAuditStatus());
                throw new BusinessException(400, "该企业已被审核，请勿重复操作");
            }
            manufacture.setAuditStatus(status);
            manufacture.setAuditRemark(remark);
            manufacture.setAuditTime(now);
            manufacture.setAuditUserId(auditUserId);
            manufactureMapper.updateById(manufacture);
            log.info("审核制造企业成功: enterpriseId={}, status={}, auditUserId={}", enterpriseId, status, auditUserId);

        } else if ("service".equals(type)) {
            ServiceProvider serviceProvider = serviceProviderMapper.selectById(enterpriseId);
            if (serviceProvider == null) {
                log.warn("审核服务商失败，服务商不存在: enterpriseId={}", enterpriseId);
                throw new BusinessException(404, "服务商不存在");
            }
            if (!"pending".equals(serviceProvider.getAuditStatus())) {
                log.warn("审核服务商失败，服务商已被审核: enterpriseId={}, currentStatus={}", enterpriseId, serviceProvider.getAuditStatus());
                throw new BusinessException(400, "该企业已被审核，请勿重复操作");
            }
            serviceProvider.setAuditStatus(status);
            serviceProvider.setAuditRemark(remark);
            serviceProvider.setAuditTime(now);
            serviceProvider.setAuditUserId(auditUserId);
            serviceProviderMapper.updateById(serviceProvider);
            log.info("审核服务商成功: enterpriseId={}, status={}, auditUserId={}", enterpriseId, status, auditUserId);

        } else {
            log.warn("审核企业失败，无效的企业类型: type={}", type);
            throw new BusinessException(400, "无效的企业类型，仅支持 manufacture 或 service");
        }
    }
}