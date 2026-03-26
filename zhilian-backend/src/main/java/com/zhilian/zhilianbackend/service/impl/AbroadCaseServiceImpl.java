package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.entity.AbroadCase;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.AbroadCaseMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:33
 * @Param:
 * @Return:
 * @Description: 出海业务服务实现类，包含案例与服务商相关业务实现
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AbroadCaseServiceImpl extends ServiceImpl<AbroadCaseMapper, AbroadCase> implements AbroadCaseService {

    private final ServiceProviderMapper serviceProviderMapper;

    @Override
    public List<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request) {
        String serviceType = request.getServiceType();
        log.debug("查询出海服务商，服务类型过滤：{}", serviceType);

        // 构建查询条件
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getIsAbroad, 1)               // 提供出海服务
                .eq(ServiceProvider::getAuditStatus, "approved")   // 审核通过
                .apply("deleted = '1970-01-01 00:00:00'");        // 未删除

        // 服务类型筛选（使用 FIND_IN_SET 匹配逗号分隔的 service_type）
        if (StringUtils.hasText(serviceType)) {
            wrapper.apply("FIND_IN_SET({0}, service_type) > 0", serviceType);
        }

        // 按 id 降序，保证稳定排序
        wrapper.orderByDesc(ServiceProvider::getId);

        List<ServiceProvider> providers = serviceProviderMapper.selectList(wrapper);

        List<AbroadServiceVO> result = providers.stream()
                .map(this::convertToServiceVO)
                .collect(Collectors.toList());

        log.info("查询出海服务商成功，共{}条", result.size());
        return result;
    }

    @Override
    public PageResult<AbroadCaseVO> getAbroadCases(AbroadCaseQueryRequest request) {
        String country = request.getCountry();
        String serviceType = request.getServiceType();
        int pageNum = request.getPage();
        int pageSize = request.getSize();

        log.debug("查询成功案例，国家：{}，服务类型：{}，页码：{}，每页条数：{}",
                country, serviceType, pageNum, pageSize);

        // 构建分页对象
        Page<AbroadCase> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<AbroadCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AbroadCase::getStatus, 1)                       // 已发布
                .apply("deleted = '1970-01-01 00:00:00'");         // 未删除

        if (StringUtils.hasText(country)) {
            wrapper.like(AbroadCase::getCountry, country);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.like(AbroadCase::getServiceType, serviceType);
        }

        // 按发布时间倒序
        wrapper.orderByDesc(AbroadCase::getPublishTime);

        // 执行分页查询
        IPage<AbroadCase> pageResult = this.page(page, wrapper);

        // 转换为 VO
        List<AbroadCaseVO> records = pageResult.getRecords().stream()
                .map(this::convertToCaseVO)
                .collect(Collectors.toList());

        log.info("查询成功案例成功，总记录数：{}，本次返回：{}条", pageResult.getTotal(), records.size());

        // 手动构建 PageResult，因为实体类型和 VO 类型不同
        return new PageResult<>(pageResult.getTotal(), records, pageResult.getCurrent(), pageResult.getSize());
    }

    // ==================== 转换方法 ====================

    private AbroadServiceVO convertToServiceVO(ServiceProvider provider) {
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

    private AbroadCaseVO convertToCaseVO(AbroadCase abroadCase) {
        AbroadCaseVO vo = new AbroadCaseVO();
        vo.setId(abroadCase.getId());
        vo.setTitle(abroadCase.getTitle());
        vo.setCompanyName(abroadCase.getCompanyName());
        vo.setCompanyType(abroadCase.getCompanyType());
        vo.setCountry(abroadCase.getCountry());
        vo.setServiceType(abroadCase.getServiceType());
        vo.setDescription(abroadCase.getDescription());
        vo.setCoverImage(abroadCase.getCoverImage());
        vo.setPublishTime(abroadCase.getPublishTime());
        return vo;
    }
}