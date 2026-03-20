package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:24
 * @Param: 
 * @Return: 
 * @Description: 评价表业务逻辑接口，定义评价相关的业务方法
**/
public interface EvaluationService extends IService<Evaluation> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:44
     * @Param: serviceId 服务商ID  page 页码  size 每页条数
     * @Return: Page<EvaluationVO> 评价列表分页结果
     * @Description: 分页查询服务商的评价列表
    **/
    Page<EvaluationVO> getEvaluationPage(Long serviceId, Integer page, Integer size);
}
