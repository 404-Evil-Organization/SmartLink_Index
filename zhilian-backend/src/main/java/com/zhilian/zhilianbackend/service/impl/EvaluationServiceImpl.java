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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 评价表业务逻辑实现类，实现评价相关的业务方法
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper; // 注入制造企业Mapper

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitEvaluation(EvaluationSubmitRequest request, Long evaluatorId, String evaluatorRole) {
        // 1. 校验合作记录是否存在
        Cooperation cooperation = cooperationMapper.selectById(request.getCoopId());
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }

        // 2. 权限校验：当前登录用户必须是该合作的制造企业方
        // 根据当前用户ID查询其所属制造企业ID
        LambdaQueryWrapper<Manufacture> manuQuery = new LambdaQueryWrapper<>();
        manuQuery.eq(Manufacture::getUserId, evaluatorId);
        Manufacture manufacture = manufactureMapper.selectOne(manuQuery);
        if (manufacture == null) {
            throw new BusinessException(403, "您不是制造企业，无法评价");
        }
        // 比较合作中的 manu_id 与当前企业ID是否一致
        if (!manufacture.getId().equals(cooperation.getManuId())) {
            throw new BusinessException(403, "无权评价该合作");
        }

        // 3. 检查是否已评价（非并发情况下的快速失败）
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
        evaluation.setIsAnonymous(request.getIsAnonymous() ? (byte)1 : (byte)0);

        // 5. 保存，捕获唯一约束异常（并发场景下两个请求同时通过第2步的检查）
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