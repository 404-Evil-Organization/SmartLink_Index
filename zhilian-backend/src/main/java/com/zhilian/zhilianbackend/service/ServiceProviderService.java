package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderAddVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderDetailVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderListVO;
import com.zhilian.zhilianbackend.entity.ServiceProvider;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 01:00
 * @Param:
 * @Return:
 * @Description: 服务商 Service 接口
 **/
public interface ServiceProviderService extends IService<ServiceProvider> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: requestDTO 查询请求参数
     * @Return: IPage<ServiceProviderListVO> 分页结果
     * @Description: 分页查询服务商列表
     **/
    IPage<ServiceProviderListVO> getServiceProviderList(ServiceProviderListRequestDTO requestDTO);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID
     * @Return: ServiceProviderDetailVO 服务商详情
     * @Description: 根据ID获取服务商详情
     **/
    ServiceProviderDetailVO getServiceProviderDetail(Long id);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: requestDTO 新增服务商请求参数
     * @Return: ServiceProviderAddVO 新增结果（返回新ID）
     * @Description: 新增服务商
     **/
    ServiceProviderAddVO addServiceProvider(ServiceProviderAddRequestDTO requestDTO);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID
     * @Param: requestDTO 修改服务商请求参数
     * @Param: currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 修改服务商信息，需要校验操作权限
     **/
    void updateServiceProvider(Long id, ServiceProviderUpdateRequestDTO requestDTO, Long currentUserId);

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID
     * @Param: currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 删除服务商（逻辑删除），需要校验操作权限
     **/
    void deleteServiceProvider(Long id, Long currentUserId);
}