package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.entity.CreditScore;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:25
 * @Description: 信用分记录表业务逻辑接口，定义信用分相关的业务方法
 */
public interface CreditScoreService extends IService<CreditScore> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: serviceId 服务商ID
     * @Return: 最新的信用分实体，若不存在返回null
     * @Description: 根据服务商ID获取最新的信用分记录
     */
    CreditScore getLatestByServiceId(Long serviceId);
}