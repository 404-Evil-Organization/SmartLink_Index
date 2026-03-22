package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.EvaluationSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 6017 & xiaodengyou
 * @Date: 2026/3/20
 * @Description: 评价表业务逻辑实现类，实现评价相关的业务方法
 * 优化点：
 * 1. 批量查询避免 N+1 问题
 * 2. 使用参数绑定防止 SQL 注入
 * 3. 正确保留分页信息（total/current/size），即使当前页无记录
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
     * @Author: 6017
     * @Date: 2026/3/20 20:37
     * @Param: serviceId 服务商ID
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: Page<EvaluationVO> 评价列表分页结果
     * @Description: 分页查询服务商的评价列表，包含评价内容和评价企业名称（支持匿名）
     * 优化：批量查询避免 N+1 问题，使用参数绑定防止 SQL 注入
     */
    @Override
    public Page<EvaluationVO> getEvaluationPage(Long serviceId, Integer page, Integer size) {
        log.info("查询服务商评价列表，serviceId: {}, page: {}, size: {}", serviceId, page, size);

        // ==================== 第一步：查询该服务商的所有合作记录ID ====================
        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.eq(Cooperation::getServiceId, serviceId)
                .select(Cooperation::getId);
        List<Long> coopIds = cooperationMapper.selectList(coopWrapper)
                .stream()
                .map(Cooperation::getId)
                .collect(Collectors.toList());

        // ==================== 第二步：分页查询评价 ====================
        Page<Evaluation> evaluationPage = new Page<>(page, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();

        // 如果有合作记录，才添加 coopId 过滤条件
        if (!coopIds.isEmpty()) {
            wrapper.in(Evaluation::getCoopId, coopIds);
        } else {
            // 如果没有合作记录，添加一个永远不成立的条件，使查询结果为空，但仍保留分页信息
            wrapper.eq(Evaluation::getId, -1L);
        }
        wrapper.orderByDesc(Evaluation::getCreateTime);

        // 执行分页查询，pageResult 始终包含正确的分页信息（total/current/size）
        Page<Evaluation> pageResult = this.page(evaluationPage, wrapper);
        List<Evaluation> evaluations = pageResult.getRecords();

        // ==================== 始终基于 pageResult 构造返回，保留分页信息 ====================
        // 即使 evaluations 为空，也要返回正确的 total/current/size
        if (evaluations.isEmpty()) {
            log.info("服务商没有评价记录或当前页无数据，serviceId: {}, total: {}, current: {}, size: {}",
                    serviceId, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());

            Page<EvaluationVO> emptyVoPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
            emptyVoPage.setRecords(List.of());
            return emptyVoPage;
        }

        // ==================== 第三步：批量查询合作记录（获取 manuId） ====================
        List<Long> evalCoopIds = evaluations.stream()
                .map(Evaluation::getCoopId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Cooperation> coopBatchWrapper = new LambdaQueryWrapper<>();
        coopBatchWrapper.in(Cooperation::getId, evalCoopIds)
                .select(Cooperation::getId, Cooperation::getManuId);
        List<Cooperation> cooperations = cooperationMapper.selectList(coopBatchWrapper);

        Map<Long, Long> coopToManuMap = cooperations.stream()
                .collect(Collectors.toMap(Cooperation::getId, Cooperation::getManuId));

        // ==================== 第四步：批量查询制造企业名称 ====================
        List<Long> manuIds = cooperations.stream()
                .map(Cooperation::getManuId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> manuIdToNameMap;
        if (!manuIds.isEmpty()) {
            LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
            manuWrapper.in(Manufacture::getId, manuIds)
                    .select(Manufacture::getId, Manufacture::getCompanyName);
            List<Manufacture> manufactures = manufactureMapper.selectList(manuWrapper);
            manuIdToNameMap = manufactures.stream()
                    .collect(Collectors.toMap(Manufacture::getId, Manufacture::getCompanyName));
        } else {
            manuIdToNameMap = Map.of();
        }

        // ==================== 第五步：转换为VO ====================
        List<EvaluationVO> voList = evaluations.stream()
                .map(eval -> convertToVO(eval, coopToManuMap, manuIdToNameMap))
                .collect(Collectors.toList());

        // 基于 pageResult 构造返回结果，确保分页信息正确
        Page<EvaluationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);

        log.info("查询到 {} 条评价记录，总记录数: {}", voList.size(), pageResult.getTotal());
        return voPage;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/21 10:30
     * @Param: eval 评价实体对象
     * @Param: coopToManuMap 合作ID到制造企业ID的映射
     * @Param: manuIdToNameMap 制造企业ID到企业名称的映射
     * @Return: EvaluationVO 评价VO对象
     * @Description: 将Evaluation实体转换为EvaluationVO，使用传入的映射表避免N+1查询
     * 匿名处理：如果 isAnonymous == 1，则显示"匿名用户"
     */
    private EvaluationVO convertToVO(Evaluation eval,
                                     Map<Long, Long> coopToManuMap,
                                     Map<Long, String> manuIdToNameMap) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(eval.getId());
        vo.setCoopId(eval.getCoopId());
        vo.setScore(eval.getScore() != null ? eval.getScore().intValue() : null);
        vo.setContent(eval.getContent());
        vo.setCreateTime(eval.getCreateTime());

        // 匿名处理
        if (eval.getIsAnonymous() != null && eval.getIsAnonymous() == 1) {
            vo.setManufactureName("匿名用户");
        } else {
            Long manuId = coopToManuMap.get(eval.getCoopId());
            if (manuId != null) {
                String companyName = manuIdToNameMap.get(manuId);
                vo.setManufactureName(companyName != null ? companyName : "未知企业");
            } else {
                vo.setManufactureName("未知企业");
            }
        }
        return vo;
    }

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