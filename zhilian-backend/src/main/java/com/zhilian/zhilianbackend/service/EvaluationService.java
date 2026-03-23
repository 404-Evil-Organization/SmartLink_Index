package com.zhilian.zhilianbackend.service;

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