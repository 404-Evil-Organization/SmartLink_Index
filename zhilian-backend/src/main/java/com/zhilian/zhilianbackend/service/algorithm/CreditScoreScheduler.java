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
     * 注意：不在方法级别加事务，避免长事务占用数据库连接
     **/
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateAllCreditScores() {
        log.info("========== 开始定时任务：计算所有服务商信用分 ==========");

        // 记录任务开始时间，用于统计总耗时
        long taskStart = System.currentTimeMillis();

        // 单批处理的服务商数量上限，避免一次性将所有服务商加载到内存
        final int pageSize = 500;
        // 使用服务商主键 id 作为游标，按 id 递增分页，避免 offset 性能问题
        Long lastId = null;

        int batchIndex = 0;
        long totalCount = 0L;
        int successCount = 0;
        int failCount = 0;

        while (true) {
            // 查询一批审核通过的服务商，逻辑删除由 @TableLogic 自动处理
            LambdaQueryWrapper<ServiceProvider> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceProvider::getAuditStatus, "approved")
                    .orderByAsc(ServiceProvider::getId);
            if (lastId != null) {
                // 只拉取 id 大于上一次批次最后一条记录的服务商，实现基于游标的分页
                wrapper.gt(ServiceProvider::getId, lastId);
            }

            long batchStart = System.currentTimeMillis();
            // 使用 LIMIT 控制单批数据量，避免一次性全表扫描加载到内存
            List<ServiceProvider> providers = serviceProviderMapper.selectList(
                    wrapper.last("LIMIT " + pageSize)
            );

            if (providers == null || providers.isEmpty()) {
                // 没有更多数据，结束循环
                break;
            }

            batchIndex++;
            int batchSuccess = 0;
            int batchFail = 0;

            for (ServiceProvider provider : providers) {
                try {
                    // 每个服务商独立事务，互不影响
                    calculateAndSaveCreditScoreWithTransaction(provider);
                    successCount++;
                    batchSuccess++;
                } catch (Exception e) {
                    failCount++;
                    batchFail++;
                    log.error("计算服务商信用分失败，serviceId: {}, companyName: {}",
                            provider.getId(), provider.getCompanyName(), e);
                }
            }

            // 更新游标为本批次最后一个服务商的 id
            lastId = providers.get(providers.size() - 1).getId();

            long batchCost = System.currentTimeMillis() - batchStart;
            totalCount += providers.size();

            log.info("信用分定时任务：批次 {} 处理完成，本批服务商数: {}，成功: {}，失败: {}，耗时: {} ms",
                    batchIndex, providers.size(), batchSuccess, batchFail, batchCost);
        }

        long totalCost = System.currentTimeMillis() - taskStart;
        log.info("========== 定时任务完成，总批次: {}，总服务商数: {}，成功: {}，失败: {}，总耗时: {} ms ==========",
                batchIndex, totalCount, successCount, failCount, totalCost);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 10:00
     * @Param: serviceProvider 服务商对象
     * @Return:
     * @Description: 带事务的计算并保存单个服务商的信用分，每个服务商独立事务
     **/
    @Transactional
    public void calculateAndSaveCreditScoreWithTransaction(ServiceProvider serviceProvider) {
        Long serviceId = serviceProvider.getId();
        // 1. 调用算法类计算信用分
        CreditScoreAlgorithm.CreditScoreResult result = creditScoreAlgorithm.calculate(serviceProvider);

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