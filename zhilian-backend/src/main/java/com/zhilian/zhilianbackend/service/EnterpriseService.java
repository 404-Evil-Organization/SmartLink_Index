package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * @Param: userId 用户ID  page 页码  size 每页条数
     * @Return: 分页结果
     * @Description: 获取当前用户的制造企业列表
    **/
    Page<EnterpriseManufactureVO> getMyManufactureList(Long userId, Long page, Long size);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 23:58
     * @Param: userId 用户ID  page 页码  size 每页条数
     * @Return: 分页结果
     * @Description: 获取当前用户的服务商列表
    **/
    Page<EnterpriseServiceVO> getMyServiceList(Long userId, Long page, Long size);
}