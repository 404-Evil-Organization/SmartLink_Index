package com.zhilian.zhilianbackend.service.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.entity.Certification;
import com.zhilian.zhilianbackend.entity.AbroadCase;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.CertificationMapper;
import com.zhilian.zhilianbackend.mapper.AbroadCaseMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:42
 * @Param:
 * @Return:
 * @Description: 信用分计算算法类，基于证书、案例、评价计算服务商信用分
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditScoreAlgorithm {

    private final CertificationMapper certificationMapper;
    private final AbroadCaseMapper abroadCaseMapper;
    private final EvaluationMapper evaluationMapper;
    private final ServiceProviderMapper serviceProviderMapper;

    // 权重配置
    private static final double QUAL_WEIGHT = 0.3;  // 资质分权重
    private static final double CASE_WEIGHT = 0.3;  // 案例分权重
    private static final double EVAL_WEIGHT = 0.4;  // 评价分权重

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:42
     * @Param: serviceId 服务商ID
     * @Return: CreditScoreResult 信用分计算结果
     * @Description: 计算服务商信用分，包括资质分、案例分、评价分和综合分
     **/
    public CreditScoreResult calculate(Long serviceId) {
        // 入参校验：serviceId 不能为空且必须为正数，防止后续 Mapper 调用触发难以定位的异常
        if (serviceId == null || serviceId <= 0) {
            log.warn("计算服务商信用分入参非法，serviceId: {}", serviceId);
            throw new IllegalArgumentException("serviceId 不能为空且必须为正数");
        }
        log.debug("开始计算服务商信用分，serviceId: {}", serviceId);

        // 1. 计算资质分
        Byte qualScore = calculateQualScore(serviceId);

        // 2. 计算案例分
        Byte caseScore = calculateCaseScore(serviceId);

        // 3. 计算评价分
        Byte evalScore = calculateEvalScore(serviceId);

        // 4. 计算综合信用分
        Byte totalScore = (byte) Math.round(
                qualScore * QUAL_WEIGHT +
                        caseScore * CASE_WEIGHT +
                        evalScore * EVAL_WEIGHT
        );

        log.debug("信用分计算完成，serviceId: {}, 综合分: {}, 资质分: {}, 案例分: {}, 评价分: {}",
                serviceId, totalScore, qualScore, caseScore, evalScore);

        return new CreditScoreResult(totalScore, qualScore, caseScore, evalScore);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:42
     * @Param: serviceId 服务商ID
     * @Return: Byte 资质分（0-100）
     * @Description: 计算资质分，基于有效证书的数量和级别
     **/
    private Byte calculateQualScore(Long serviceId) {
        LambdaQueryWrapper<Certification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certification::getServiceId, serviceId)
                .eq(Certification::getStatus, 1)  // 有效状态
                // 使用当前"日期"而非当前"时间"，确保到期日当天仍视为未过期
                .ge(Certification::getExpireDate, java.sql.Date.valueOf(LocalDate.now())); // 未过期

        List<Certification> certifications = certificationMapper.selectList(wrapper);

        if (certifications.isEmpty()) {
            return (byte) 0;
        }

        // 基础分：每个证书20分，最高100分
        int baseScore = Math.min(certifications.size() * 20, 100);

        // 额外加分：国家级认证（CNAS、CMA、ISO）每个加10分
        int extraScore = 0;
        for (Certification cert : certifications) {
            String certName = cert.getCertName();
            if (certName != null && (certName.contains("CNAS") || certName.contains("CMA") || certName.contains("ISO"))) {
                extraScore += 10;
            }
        }

        return (byte) Math.min(baseScore + extraScore, 100);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:42
     * @Param: serviceId 服务商ID
     * @Return: Byte 案例分（0-100）
     * @Description: 计算案例分，基于出海案例的数量和时效性
     * 修复：先通过 serviceProviderMapper.selectById(serviceId) 获取服务商信息，判空后取 companyName
     **/
    private Byte calculateCaseScore(Long serviceId) {
        // 1. 先查询服务商信息
        ServiceProvider serviceProvider = serviceProviderMapper.selectById(serviceId);
        if (serviceProvider == null) {
            log.warn("服务商不存在，serviceId: {}", serviceId);
            return (byte) 0;
        }

        // 2. 获取公司名称
        String companyName = serviceProvider.getCompanyName();
        if (companyName == null || companyName.isEmpty()) {
            log.warn("服务商公司名称为空，serviceId: {}", serviceId);
            return (byte) 0;
        }

        // 3. 查询该服务商的所有案例，通过 companyName + companyType 精确关联
        LambdaQueryWrapper<AbroadCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AbroadCase::getCompanyName, companyName)
                .eq(AbroadCase::getCompanyType, "service")
                .eq(AbroadCase::getStatus, 1); // 已发布

        List<AbroadCase> cases = abroadCaseMapper.selectList(wrapper);

        if (cases.isEmpty()) {
            return (byte) 0;
        }

        // 4. 基础分：每个案例15分，最高60分
        int baseScore = Math.min(cases.size() * 15, 60);

        // 5. 时效加分：近1年发布的案例每个加5分，最高40分
        Date oneYearAgo = new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000);
        int recentCount = 0;
        for (AbroadCase ac : cases) {
            if (ac.getPublishTime() != null && ac.getPublishTime().after(oneYearAgo)) {
                recentCount++;
            }
        }

        int recentScore = Math.min(recentCount * 5, 40);

        return (byte) Math.min(baseScore + recentScore, 100);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:43
     * @Param: serviceId 服务商ID
     * @Return: Byte 评价分（0-100）
     * @Description: 计算评价分，基于平均评分和评价数量
     * 修复：使用 DateConstants.getNotDeletedTime() 获取未删除标志（Date 类型），避免隐式类型转换
     **/
    private Byte calculateEvalScore(Long serviceId) {
        Date notDeletedTime = DateConstants.getNotDeletedTime();

        Double avgScore = evaluationMapper.getAvgScoreByServiceId(serviceId, notDeletedTime);
        Integer count = evaluationMapper.getCountByServiceId(serviceId, notDeletedTime);

        // 没有评价时给基础分60
        if (avgScore == null || avgScore == 0) {
            return (byte) 60;
        }

        // 平均分转换为百分制 (1-5星 -> 0-100)
        int scoreBase = (int) Math.round(avgScore * 20);

        // 数量加成：评价数量超过5个每多一个加1分，最高20分
        int countBonus = 0;
        if (count != null && count > 5) {
            countBonus = Math.min(count - 5, 20);
        }

        return (byte) Math.min(scoreBase + countBonus, 100);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:43
     * @Param:
     * @Return:
     * @Description: 信用分计算结果内部类
     **/
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CreditScoreResult {
        private Byte totalScore;  // 综合信用分
        private Byte qualScore;   // 资质分
        private Byte caseScore;   // 案例分
        private Byte evalScore;   // 评价分
    }
}