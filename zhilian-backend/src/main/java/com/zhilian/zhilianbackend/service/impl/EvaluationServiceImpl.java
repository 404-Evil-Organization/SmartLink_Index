package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.EvaluationService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 21:33
 * @Description: 评价表业务逻辑实现类，实现评价相关的业务方法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: request 评价请求参数
     * @Param: evaluatorId 当前登录用户ID
     * @Param: evaluatorRole 评价人角色（manufacture/service）
     * @Return: 生成的评价ID
     * @Description: 提交评价，包含合作存在性校验、权限校验（管理员禁止评价、非管理员必须为合作的制造企业方）、重复评价校验，捕获唯一约束异常并转换为业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitEvaluation(EvaluationSubmitRequest request, Long evaluatorId, String evaluatorRole) {
        // 1. 校验合作记录是否存在
        Cooperation cooperation = cooperationMapper.selectById(request.getCoopId());
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }

        // 2. 权限校验：禁止管理员评价，且必须是合作制造企业方
        if (securityUtils.isAdmin()) {
            // 管理员不允许以普通评价身份写入，避免占用真实企业评价名额
            throw new BusinessException(403, "管理员不允许提交评价");
        }
        // 非管理员：必须校验当前用户是合作的制造企业方
        LambdaQueryWrapper<Manufacture> manuQuery = new LambdaQueryWrapper<>();
        manuQuery.eq(Manufacture::getUserId, evaluatorId);
        Manufacture manufacture = manufactureMapper.selectOne(manuQuery);
        if (manufacture == null) {
            throw new BusinessException(403, "您不是制造企业，无法评价");
        }
        if (!manufacture.getId().equals(cooperation.getManuId())) {
            throw new BusinessException(403, "无权评价该合作");
        }

        // 3. 检查是否已评价（同一合作、同一角色只能评价一次）
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getCoopId, request.getCoopId())
                .eq(Evaluation::getEvaluatorRole, evaluatorRole);
        if (this.count(wrapper) > 0) {
            throw new BusinessException(409, "您已经评价过该合作");
        }

        // 4. 创建评价实体
        Evaluation evaluation = new Evaluation();
        evaluation.setCoopId(request.getCoopId());
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatorRole(evaluatorRole);
        evaluation.setScore(request.getScore().byteValue());
        evaluation.setContent(request.getContent());
        evaluation.setIsAnonymous(Boolean.TRUE.equals(request.getIsAnonymous()) ? (byte) 1 : (byte) 0);

        // 5. 保存，捕获唯一约束异常
        try {
            this.save(evaluation);
        } catch (DataIntegrityViolationException e) {
            log.warn("并发提交评价，检测到重复评价：coopId={}, evaluatorRole={}, evaluatorId={}",
                    request.getCoopId(), evaluatorRole, evaluatorId);
            throw new BusinessException(409, "您已经评价过该合作");
        }

        return evaluation.getId();
    }
}