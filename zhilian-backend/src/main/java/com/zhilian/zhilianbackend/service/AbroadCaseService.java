package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadServiceQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.dto.response.AbroadServiceVO;
import com.zhilian.zhilianbackend.entity.AbroadCase;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:26
 * @Param:
 * @Return:
 * @Description: 出海业务服务接口，包含案例与服务商相关业务方法
 **/
public interface AbroadCaseService extends IService<AbroadCase> {

    /**
     * 获取提供出海服务的服务商列表
     * @param request 查询条件
     * @return 服务商列表
     */
    List<AbroadServiceVO> getAbroadServices(AbroadServiceQueryRequest request);

    /**
     * 分页获取已发布成功案例
     * @param request 查询条件（含分页）
     * @return 分页结果
     */
    PageResult<AbroadCaseVO> getAbroadCases(AbroadCaseQueryRequest request);
}