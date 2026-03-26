package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.entity.AbroadCase;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:26
 * @Description: 出海成功案例业务接口
 */
public interface AbroadCaseService extends IService<AbroadCase> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 成功案例查询请求参数（含分页、筛选条件）
     * @Return: PageResult<AbroadCaseVO> 分页封装的成功案例视图对象
     * @Description: 分页获取已发布的成功案例，支持按国家、服务类型筛选
     */
    PageResult<AbroadCaseVO> getAbroadCases(AbroadCaseQueryRequest request);
}