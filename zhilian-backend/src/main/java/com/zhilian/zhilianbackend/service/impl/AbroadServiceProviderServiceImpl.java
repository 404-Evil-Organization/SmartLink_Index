package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.AbroadServiceProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/26 15:51
 * @Description: 出海服务商业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbroadServiceProviderServiceImpl implements AbroadServiceProviderService {

    private final ServiceProviderMapper serviceProviderMapper;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 服务商查询请求参数（含服务类型、区域筛选及分页）
     * @Return: PageResult<AbroadServiceVO> 分页封装的服务商视图对象
     * @Description: 分页获取提供出海服务且审核通过的服务商列表，支持按服务类型、区域筛选
     */
    @Override
    public PageResult<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request) {
        String serviceType = request.getServiceType();
        String region = request.getRegion();
        int pageNum = request.getPage();
        int pageSize = request.getSize();

        log.debug("查询出海服务商，服务类型过滤：{}，区域：{}，页码：{}，每页条数：{}",
                serviceType, region, pageNum, pageSize);

        // 构建分页对象
        Page<ServiceProvider> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getIsAbroad, 1)
                .eq(ServiceProvider::getAuditStatus, "approved")
                .apply("deleted = {0}", DateConstants.getNotDeletedTimeStr());

        if (StringUtils.hasText(serviceType)) {
            wrapper.apply("FIND_IN_SET({0}, service_type) > 0", serviceType);
        }
        if (StringUtils.hasText(region)) {
            wrapper.eq(ServiceProvider::getRegion, region);
        }

        wrapper.orderByDesc(ServiceProvider::getId);

        // 执行分页查询
        IPage<ServiceProvider> pageResult = serviceProviderMapper.selectPage(page, wrapper);

        // 转换为 VO 并返回分页结果
        IPage<AbroadServiceVO> voPage = pageResult.convert(this::convertToVO);

        log.info("查询出海服务商成功，总记录数：{}，本次返回：{}条", voPage.getTotal(), voPage.getRecords().size());

        return PageResult.from(voPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: provider 服务商实体
     * @Return: AbroadServiceVO 视图对象
     * @Description: 将 ServiceProvider 实体转换为 AbroadServiceVO 视图对象
     */
    private AbroadServiceVO convertToVO(ServiceProvider provider) {
        AbroadServiceVO vo = new AbroadServiceVO();
        vo.setId(provider.getId());
        vo.setCompanyName(provider.getCompanyName());
        vo.setRegion(provider.getRegion());
        vo.setServiceType(provider.getServiceType());
        vo.setDescription(provider.getDescription());
        vo.setLogo(provider.getLogo());
        vo.setWebsite(provider.getWebsite());
        vo.setEstablishedDate(provider.getEstablishedDate());
        vo.setEmployeeCount(provider.getEmployeeCount());
        vo.setCountryCoverage(provider.getCountryCoverage());
        vo.setQualification(provider.getQualification());
        return vo;
    }
}