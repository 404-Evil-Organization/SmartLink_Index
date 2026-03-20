package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.EnterpriseManufactureVO;
import com.zhilian.zhilianbackend.dto.response.EnterpriseServiceVO;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:59
     * @Param: userId 用户ID  pageNum 页码  pageSize 每页条数
     * @Return: 分页结果
     * @Description: 获取当前用户的制造企业列表
    **/
    @Override
    public Page<EnterpriseManufactureVO> getMyManufactureList(Long userId, Long pageNum, Long pageSize) {
        // 构建查询条件：根据user_id查询，且未删除
        LambdaQueryWrapper<Manufacture> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Manufacture::getUserId, userId)
                .eq(Manufacture::getDeleted, "1970-01-01 00:00:00")
                .orderByDesc(Manufacture::getCreateTime);

        // 分页查询
        Page<Manufacture> manufacturePage = new Page<>(pageNum, pageSize);
        Page<Manufacture> resultPage = manufactureMapper.selectPage(manufacturePage, wrapper);

        // 转换为VO并创建新的Page对象
        List<EnterpriseManufactureVO> records = resultPage.getRecords().stream()
                .map(this::convertToManufactureVO)
                .collect(Collectors.toList());

        // 创建返回的Page对象，使用泛型构造器
        Page<EnterpriseManufactureVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(records);

        return voPage;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 00:00
     * @Param: userId 用户ID  pageNum 页码  pageSize 每页条数
     * @Return: 分页结果
     * @Description: 获取当前用户的服务商列表
    **/
    @Override
    public Page<EnterpriseServiceVO> getMyServiceList(Long userId, Long pageNum, Long pageSize) {
        // 构建查询条件：根据user_id查询，且未删除
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getUserId, userId)
                .eq(ServiceProvider::getDeleted, "1970-01-01 00:00:00")
                .orderByDesc(ServiceProvider::getCreateTime);

        // 分页查询
        Page<ServiceProvider> servicePage = new Page<>(pageNum, pageSize);
        Page<ServiceProvider> resultPage = serviceProviderMapper.selectPage(servicePage, wrapper);

        // 转换为VO并创建新的Page对象
        List<EnterpriseServiceVO> records = resultPage.getRecords().stream()
                .map(this::convertToServiceVO)
                .collect(Collectors.toList());

        // 创建返回的Page对象，使用泛型构造器
        Page<EnterpriseServiceVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(records);

        return voPage;
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