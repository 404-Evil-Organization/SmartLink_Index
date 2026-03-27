package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/26
 * @Description: 出海服务商业务接口
 */
public interface AbroadServiceProviderService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 服务商查询请求参数（含服务类型、区域筛选及分页）
     * @Return: PageResult<AbroadServiceVO> 分页封装的服务商视图对象
     * @Description: 分页获取提供出海服务且审核通过的服务商列表，支持按服务类型、区域筛选
     */
    PageResult<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request);
}