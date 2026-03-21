package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:37
 * @Param:
 * @Return:
 * @Description: 评价表业务逻辑实现类，实现评价相关的业务方法
 * 优化点：
 * 1. 批量查询避免 N+1 问题
 * 2. 使用参数绑定防止 SQL 注入
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper;

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
        // 使用 LambdaQueryWrapper 参数绑定，防止 SQL 注入
        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.eq(Cooperation::getServiceId, serviceId)
                .select(Cooperation::getId);
        List<Long> coopIds = cooperationMapper.selectList(coopWrapper)
                .stream()
                .map(Cooperation::getId)
                .collect(Collectors.toList());

        // 如果没有合作记录，直接返回空页
        if (coopIds.isEmpty()) {
            log.info("服务商没有合作记录，serviceId: {}", serviceId);
            return new Page<>(page, size);
        }

        // ==================== 第二步：分页查询评价 ====================
        Page<Evaluation> evaluationPage = new Page<>(page, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Evaluation::getCoopId, coopIds)
                .orderByDesc(Evaluation::getCreateTime);

        Page<Evaluation> pageResult = this.page(evaluationPage, wrapper);
        List<Evaluation> evaluations = pageResult.getRecords();

        if (evaluations.isEmpty()) {
            return new Page<>(page, size);
        }

        // ==================== 第三步：批量查询合作记录（获取 manuId） ====================
        // 提取本页所有评价的 coopId
        List<Long> evalCoopIds = evaluations.stream()
                .map(Evaluation::getCoopId)
                .collect(Collectors.toList());

        // 批量查询合作记录，只查询需要的字段
        LambdaQueryWrapper<Cooperation> coopBatchWrapper = new LambdaQueryWrapper<>();
        coopBatchWrapper.in(Cooperation::getId, evalCoopIds)
                .select(Cooperation::getId, Cooperation::getManuId);
        List<Cooperation> cooperations = cooperationMapper.selectList(coopBatchWrapper);

        // 构建 coopId -> manuId 映射
        Map<Long, Long> coopToManuMap = cooperations.stream()
                .collect(Collectors.toMap(Cooperation::getId, Cooperation::getManuId));

        // ==================== 第四步：批量查询制造企业名称 ====================
        // 提取所有不重复的 manuId
        List<Long> manuIds = cooperations.stream()
                .map(Cooperation::getManuId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询制造企业名称
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

        // ==================== 第五步：转换为VO（内存组装，避免 N+1） ====================
        List<EvaluationVO> voList = evaluations.stream()
                .map(eval -> convertToVO(eval, coopToManuMap, manuIdToNameMap))
                .collect(Collectors.toList());

        // 封装分页结果
        Page<EvaluationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);

        log.info("查询到 {} 条评价记录", voList.size());
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
            // 从映射表中获取企业名称
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
}