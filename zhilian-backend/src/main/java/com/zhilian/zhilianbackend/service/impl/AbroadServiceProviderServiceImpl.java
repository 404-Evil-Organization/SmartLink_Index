package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.AbroadServiceProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

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
     * @Param: request 服务商查询请求参数（含服务类型筛选）
     * @Return: List<AbroadServiceVO> 服务商视图对象列表
     * @Description: 获取提供出海服务且审核通过的服务商列表，支持按服务类型筛选
     */
    @Override
    public List<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request) {
        String serviceType = request.getServiceType();
        log.debug("查询出海服务商，服务类型过滤：{}", serviceType);

        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getIsAbroad, 1)
                .eq(ServiceProvider::getAuditStatus, "approved")
                .apply("deleted = {0}", DateConstants.getNotDeletedTimeStr());

        if (StringUtils.hasText(serviceType)) {
            wrapper.apply("FIND_IN_SET({0}, service_type) > 0", serviceType);
        }

        wrapper.orderByDesc(ServiceProvider::getId);

        List<ServiceProvider> providers = serviceProviderMapper.selectList(wrapper);

        List<AbroadServiceVO> result = providers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("查询出海服务商成功，共{}条", result.size());
        return result;
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