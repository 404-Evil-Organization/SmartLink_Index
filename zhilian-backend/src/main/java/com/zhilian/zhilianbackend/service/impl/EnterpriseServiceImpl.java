package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.EnterpriseManufactureVO;
import com.zhilian.zhilianbackend.dto.response.EnterpriseServiceVO;
import com.zhilian.zhilianbackend.dto.request.ManufactureListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: 6017
 * @Date: 2026/3/20 23:59
 * @Param: 
 * @Return: 
 * @Description: 企业管理服务实现类
**/
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final SecurityUtils securityUtils;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:59
     * @Param: userId 用户ID  pageNum 页码  pageSize 每页条数
     * @Return: 分页结果
     * @Description: 获取当前用户的制造企业列表
    **/
    @Override
    public IPage<EnterpriseManufactureVO> getMyManufactureList(ManufactureListRequestDTO requestDTO) {
        Long userId = securityUtils.getCurrentUserId();
        String role = securityUtils.getCurrentUserRole();
        
        LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
        // 如果不是管理员，则只查询自己的数据
        if (!"admin".equals(role)) {
            wrapper.eq(Manufacture::getUserId, userId);
        }
        
        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            wrapper.eq(Manufacture::getRegion, requestDTO.getRegion());
        }
        if (StringUtils.isNotBlank(requestDTO.getScale())) {
            wrapper.eq(Manufacture::getScale, requestDTO.getScale());
        }
        if (StringUtils.isNotBlank(requestDTO.getProductType())) {
            String escaped = escapeSqlLike(requestDTO.getProductType());
            wrapper.apply("product_type LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escaped);
        }

        wrapper.orderByDesc(Manufacture::getCreateTime);

        Page<Manufacture> resultPage = manufactureMapper.selectPage(new Page<>(requestDTO.getPage(), requestDTO.getSize()), wrapper);
        return resultPage.convert(this::convertToManufactureVO);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:59
     * @Param: requestDTO 查询参数
     * @Return: 分页结果
     * @Description: 获取当前用户的服务商列表
    **/
    @Override
    public IPage<EnterpriseServiceVO> getMyServiceList(ServiceProviderListRequestDTO requestDTO) {
        Long userId = securityUtils.getCurrentUserId();
        String role = securityUtils.getCurrentUserRole();
        
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        // 如果不是管理员，则只查询自己的数据
        if (!"admin".equals(role)) {
            wrapper.eq(ServiceProvider::getUserId, userId);
        }

        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            wrapper.eq(ServiceProvider::getRegion, requestDTO.getRegion());
        }
        if (StringUtils.isNotBlank(requestDTO.getServiceType())) {
            String escaped = escapeSqlLike(requestDTO.getServiceType());
            wrapper.apply("service_type LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escaped);
        }

        wrapper.orderByDesc(ServiceProvider::getCreateTime);

        Page<ServiceProvider> resultPage = serviceProviderMapper.selectPage(new Page<>(requestDTO.getPage(), requestDTO.getSize()), wrapper);
        return resultPage.convert(this::convertToServiceVO);
    }

    /**
     * 转义SQL LIKE特殊字符
     */
    private String escapeSqlLike(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return keyword;
        }
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 00:00
     * @Param: manufacture 制造企业实体  
     * @Return: EnterpriseManufactureVO
     * @Description: 转换Manufacture实体为EnterpriseManufactureVO
    **/
    private EnterpriseManufactureVO convertToManufactureVO(Manufacture manufacture) {
        EnterpriseManufactureVO vo = new EnterpriseManufactureVO();
        BeanUtils.copyProperties(manufacture, vo);
        return vo;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 00:01
     * @Param: serviceProvider 服务商实体
     * @Return: EnterpriseServiceVO
     * @Description: 转换ServiceProvider实体为EnterpriseServiceVO
    **/
    private EnterpriseServiceVO convertToServiceVO(ServiceProvider serviceProvider) {
        EnterpriseServiceVO vo = new EnterpriseServiceVO();
        BeanUtils.copyProperties(serviceProvider, vo);
        return vo;
    }
}