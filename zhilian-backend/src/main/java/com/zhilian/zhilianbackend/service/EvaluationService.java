package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 评价表业务逻辑接口，定义评价相关的业务方法
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:44
     * @Param: serviceId 服务商ID  page 页码  size 每页条数
     * @Return: Page<EvaluationVO> 评价列表分页结果
     * @Description: 分页查询服务商的评价列表
    **/
    Page<EvaluationVO> getEvaluationPage(Long serviceId, Integer page, Integer size);

    /**
    * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: request 评价请求参数
     * @Param: evaluatorId 当前登录用户ID
     * @Param: evaluatorRole 当前登录用户角色
     * @Return: 生成的评价ID
     * @Description: 提交评价
     */
    Long submitEvaluation(EvaluationSubmitRequest request, Long evaluatorId, String evaluatorRole);
}