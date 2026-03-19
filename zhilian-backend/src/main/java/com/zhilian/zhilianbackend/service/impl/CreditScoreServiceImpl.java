package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.entity.CreditScore;
import com.zhilian.zhilianbackend.mapper.CreditScoreMapper;
import com.zhilian.zhilianbackend.service.CreditScoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:33
 * @Description: 信用分记录表业务逻辑实现类，实现信用分相关的业务方法
 */
@Service
public class CreditScoreServiceImpl extends ServiceImpl<CreditScoreMapper, CreditScore> implements CreditScoreService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: serviceId 服务商ID
     * @Return: 最新的信用分实体，若不存在返回null
     * @Description: 根据服务商ID获取最新的信用分记录
     */
    @Override
    public CreditScore getLatestByServiceId(Long serviceId) {
        LambdaQueryWrapper<CreditScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditScore::getServiceId, serviceId)
                .orderByDesc(CreditScore::getCalcTime)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }
}