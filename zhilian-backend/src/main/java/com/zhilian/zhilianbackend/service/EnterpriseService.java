package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.dto.request.ManufactureListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.dto.response.EnterpriseManufactureVO;
import com.zhilian.zhilianbackend.dto.response.EnterpriseServiceVO;

/**
 * @Author: 6017
 * @Date: 2026/3/20 23:57
 * @Param: 
 * @Return: 
 * @Description: 企业管理服务接口
**/
public interface EnterpriseService {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:58
     * @Param requestDTO  查询参数
     * @Return: 分页结果
     * @Description: 获取当前用户的制造企业列表
    **/
    IPage<EnterpriseManufactureVO> getMyManufactureList(ManufactureListRequestDTO requestDTO);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:58
     * @Param requestDTO  查询参数
     * @Return: 分页结果
     * @Description: 获取当前用户的服务商列表
    **/
    IPage<EnterpriseServiceVO> getMyServiceList(ServiceProviderListRequestDTO requestDTO);
}