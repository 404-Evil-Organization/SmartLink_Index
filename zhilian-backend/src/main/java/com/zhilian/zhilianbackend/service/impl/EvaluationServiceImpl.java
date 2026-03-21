package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
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
    private final ServiceProviderMapper serviceProviderMapper;
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: request 评价请求参数
     * @Param: evaluatorId 当前登录用户ID
     * @Param: evaluatorRole 评价人角色（manufacture/service）
     * @Return: 生成的评价ID
     * @Description: 提交评价，包含合作存在性校验、权限校验（管理员禁止评价、非管理员必须为合作对应方）、重复评价校验，捕获唯一约束异常并转换为业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitEvaluation(EvaluationSubmitRequest request, Long evaluatorId, String evaluatorRole) {
        // 0. 归一化评价角色，确保写入数据库的是合法枚举值（manufacture 或 service）
        String normalizedRole = normalizeEvaluatorRole(evaluatorRole);

        // 1. 校验合作记录是否存在
        Cooperation cooperation = cooperationMapper.selectById(request.getCoopId());
        if (cooperation == null) {
            throw new BusinessException(404, "合作记录不存在");
        }

        // 2. 权限校验：禁止管理员评价
        if (securityUtils.isAdmin()) {
            throw new BusinessException(403, "管理员不允许提交评价");
        }

        // 3. 按归一化后的角色校验当前用户是否属于该合作对应的企业
        if ("manufacture".equals(normalizedRole)) {
            LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
            manuWrapper.eq(Manufacture::getUserId, evaluatorId);
            Manufacture manufacture = manufactureMapper.selectOne(manuWrapper);
            if (manufacture == null) {
                throw new BusinessException(403, "当前用户未绑定制造企业，无法评价");
            }
            if (cooperation.getManuId() == null || !cooperation.getManuId().equals(manufacture.getId())) {
                throw new BusinessException(403, "无权评价该合作记录");
            }
        } else if ("service".equals(normalizedRole)) {
            LambdaQueryWrapper<ServiceProvider> spWrapper = new LambdaQueryWrapper<>();
            spWrapper.eq(ServiceProvider::getUserId, evaluatorId);
            ServiceProvider serviceProvider = serviceProviderMapper.selectOne(spWrapper);
            if (serviceProvider == null) {
                throw new BusinessException(403, "当前用户未绑定服务商企业，无法评价");
            }
            if (cooperation.getServiceId() == null || !cooperation.getServiceId().equals(serviceProvider.getId())) {
                throw new BusinessException(403, "无权评价该合作记录");
            }
        } else {
            // 理论上归一化后只会是 manufacture 或 service，这里兜底
            throw new BusinessException(403, "当前角色无权提交该合作评价");
        }

        // 4. 检查是否已评价（同一合作、同一角色只能评价一次）
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getCoopId, request.getCoopId())
                .eq(Evaluation::getEvaluatorRole, normalizedRole);
        if (this.count(wrapper) > 0) {
            throw new BusinessException(409, "您已经评价过该合作");
        }

        // 5. 创建评价实体
        Evaluation evaluation = new Evaluation();
        evaluation.setCoopId(request.getCoopId());
        evaluation.setEvaluatorId(evaluatorId);
        evaluation.setEvaluatorRole(normalizedRole);
        evaluation.setScore(request.getScore().byteValue());
        evaluation.setContent(request.getContent());
        evaluation.setIsAnonymous(Boolean.TRUE.equals(request.getIsAnonymous()) ? (byte) 1 : (byte) 0);

        // 6. 保存，捕获唯一约束异常
        try {
            this.save(evaluation);
        } catch (DataIntegrityViolationException e) {
            log.warn("并发提交评价，检测到重复评价：coopId={}, evaluatorRole={}, evaluatorId={}",
                    request.getCoopId(), normalizedRole, evaluatorId);
            throw new BusinessException(409, "您已经评价过该合作");
        }

        return evaluation.getId();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: evaluatorRole 外部传入的评价角色标识
     * @Return: 符合数据库枚举定义的角色字符串（manufacture 或 service）
     * @Description: 将外部传入的评价角色统一转换为数据库合法枚举值。
     *              当前 evaluation.evaluator_role 字段定义为 ENUM('manufacture','service')，
     *              因此仅接受 "manufacture" 或 "service"（不区分大小写），否则抛出业务异常。
     */
    private String normalizeEvaluatorRole(String evaluatorRole) {
        if (evaluatorRole == null || evaluatorRole.trim().isEmpty()) {
            throw new BusinessException(400, "评价角色不能为空");
        }
        String role = evaluatorRole.trim().toLowerCase();
        if ("manufacture".equals(role) || "service".equals(role)) {
            return role;
        }
        throw new BusinessException(400, "评价角色不合法，仅允许 manufacture 或 service");
    }
}