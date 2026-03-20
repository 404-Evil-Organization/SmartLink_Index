package com.zhilian.zhilianbackend.service.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.entity.CreditScore;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.CreditScoreMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:43
 * @Param: 
 * @Return: 
 * @Description: 信用分计算定时任务，每天凌晨2点执行，计算所有审核通过服务商的信用分
**/
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CreditScoreScheduler {

    private final ServiceProviderMapper serviceProviderMapper;
    private final CreditScoreMapper creditScoreMapper;
    private final CreditScoreAlgorithm creditScoreAlgorithm;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:44
     * @Param: 
     * @Return: 
     * @Description: 每天凌晨2点执行信用分计算，遍历所有审核通过的服务商并计算信用分
    **/
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void calculateAllCreditScores() {
        log.info("========== 开始定时任务：计算所有服务商信用分 ==========");

        // 查询所有审核通过的服务商
        LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceProvider::getAuditStatus, "approved")
                .eq(ServiceProvider::getDeleted, "1970-01-01 00:00:00");

        List<ServiceProvider> providers = serviceProviderMapper.selectList(wrapper);
        log.info("找到 {} 个服务商需要计算信用分", providers.size());

        int successCount = 0;
        int failCount = 0;

        for (ServiceProvider provider : providers) {
            try {
                calculateAndSaveCreditScore(provider.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("计算服务商信用分失败，serviceId: {}, companyName: {}",
                        provider.getId(), provider.getCompanyName(), e);
            }
        }

        log.info("========== 定时任务完成，成功: {}，失败: {} ==========", successCount, failCount);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:44
     * @Param: serviceId 服务商ID
     * @Return: 
     * @Description: 计算并保存单个服务商的信用分
    **/
    private void calculateAndSaveCreditScore(Long serviceId) {
        // 1. 调用算法类计算信用分
        CreditScoreAlgorithm.CreditScoreResult result = creditScoreAlgorithm.calculate(serviceId);

        // 2. 创建信用分记录
        CreditScore creditScore = new CreditScore();
        creditScore.setServiceId(serviceId)
                .setScore(result.getTotalScore())
                .setQualScore(result.getQualScore())
                .setCaseScore(result.getCaseScore())
                .setEvalScore(result.getEvalScore())
                .setCalcTime(new Date());

        // 3. 保存到数据库
        creditScoreMapper.insert(creditScore);

        log.debug("信用分保存完成，serviceId: {}, 综合分: {}, 资质分: {}, 案例分: {}, 评价分: {}",
                serviceId, result.getTotalScore(), result.getQualScore(),
                result.getCaseScore(), result.getEvalScore());
    }
}