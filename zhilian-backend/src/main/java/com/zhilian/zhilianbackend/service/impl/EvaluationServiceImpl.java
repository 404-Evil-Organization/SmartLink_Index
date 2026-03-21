package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Evaluation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.EvaluationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.EvaluationService;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:45
 * @Param: 
 * @Return: 
 * @Description: 评价表业务逻辑实现类，实现评价相关的业务方法
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final CooperationMapper cooperationMapper;
    private final ManufactureMapper manufactureMapper;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:45
     * @Param: serviceId 服务商ID  page 页码  size 每页条数
     * @Return: Page<EvaluationVO> 评价列表分页结果
     * @Description: 分页查询服务商的评价列表，包含评价内容和评价企业名称（支持匿名）
    **/
    @Override
    public Page<EvaluationVO> getEvaluationPage(Long serviceId, Integer page, Integer size) {
        // 1. 直接在评价查询中通过子查询按服务商 ID 过滤，避免先拉取所有合作记录 ID
        Page<Evaluation> evaluationPage = new Page<>(page, size);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(Evaluation::getCoopId,
                        "SELECT id FROM cooperation WHERE service_id = " + serviceId)
                .orderByDesc(Evaluation::getCreateTime);

        Page<Evaluation> pageResult = this.page(evaluationPage, wrapper);

        // 2. 转换为VO
        List<EvaluationVO> voList = pageResult.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        Page<EvaluationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:46
     * @Param: eval 评价实体对象
     * @Return: EvaluationVO 评价VO对象
     * @Description: 将Evaluation实体转换为EvaluationVO，处理匿名评价显示
    **/
    private EvaluationVO convertToVO(Evaluation eval) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(eval.getId());
        vo.setCoopId(eval.getCoopId());
        vo.setScore(eval.getScore() != null ? eval.getScore().intValue() : null);
        vo.setContent(eval.getContent());
        vo.setCreateTime(eval.getCreateTime());

        if (eval.getIsAnonymous() != null && eval.getIsAnonymous() == 1) {
            vo.setManufactureName("匿名用户");
        } else {
            LambdaQueryWrapper<Cooperation> cWrapper = new LambdaQueryWrapper<>();
            cWrapper.eq(Cooperation::getId, eval.getCoopId())
                    .select(Cooperation::getManuId);
            Cooperation coop = cooperationMapper.selectOne(cWrapper);
            if (coop != null) {
                Manufacture manu = manufactureMapper.selectById(coop.getManuId());
                vo.setManufactureName(manu != null ? manu.getCompanyName() : "未知企业");
            } else {
                vo.setManufactureName("未知企业");
            }
        }
        return vo;
    }
}