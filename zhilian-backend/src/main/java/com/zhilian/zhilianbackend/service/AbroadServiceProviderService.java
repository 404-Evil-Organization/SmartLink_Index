package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/26
 * @Description: 出海服务商业务接口
 */
public interface AbroadServiceProviderService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 服务商查询请求参数（含服务类型筛选）
     * @Return: List<AbroadServiceVO> 服务商视图对象列表
     * @Description: 获取提供出海服务的服务商列表，支持按服务类型筛选
     */
    List<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request);
}