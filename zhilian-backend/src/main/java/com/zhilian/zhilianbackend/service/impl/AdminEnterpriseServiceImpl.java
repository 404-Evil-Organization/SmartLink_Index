package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:36
 * @Param: 
 * @Return: 
 * @Description: 管理员企业审核服务实现类
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEnterpriseServiceImpl implements AdminEnterpriseService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;

    private static final Date NOT_DELETED = Date.from(LocalDateTime.of(1970, 1, 1, 0, 0, 0)
            .atZone(ZoneId.systemDefault()).toInstant());

    /**
     * @Author: 6017
     * @Date: 2026/3/25 00:16
     * @Param: page 页码  size 每页条数  IPage<PendingEnterpriseResponse> 待审核企业分页数据
     * @Return:
     * @Description: 取待审核企业列表，合并制造企业和服务商数据，按创建时间倒序排序并手动分页
    **/
    @Override
    public IPage<PendingEnterpriseResponse> getPendingEnterpriseList(Integer page, Integer size) {
        // 创建分页对象
        Page<PendingEnterpriseResponse> pageParam = new Page<>(page, size);

        // 查询待审核的制造企业
        LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
        manuWrapper.eq(Manufacture::getAuditStatus, "pending")
                .eq(Manufacture::getDeleted, NOT_DELETED);
        List<Manufacture> manuList = manufactureMapper.selectList(manuWrapper);

        // 查询待审核的服务商
        LambdaQueryWrapper<ServiceProvider> serviceWrapper = new LambdaQueryWrapper<>();
        serviceWrapper.eq(ServiceProvider::getAuditStatus, "pending")
                .eq(ServiceProvider::getDeleted, NOT_DELETED);
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

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, allList.size());
        List<PendingEnterpriseResponse> records = start < allList.size() ? allList.subList(start, end) : new ArrayList<>();

        pageParam.setRecords(records);
        pageParam.setTotal(allList.size());

        log.info("查询待审核企业列表成功，总数: {}, 当前页: {}", allList.size(), records.size());
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

    /**
     * @Author: 6017
     * @Date: 2026/3/24 23:38
     * @Param: date Date 类型的时间
     * @Return: LocalDateTime LocalDateTime 类型的时间
     * @Description: Date 转 LocalDateTime，用于日期类型转换
    **/
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}