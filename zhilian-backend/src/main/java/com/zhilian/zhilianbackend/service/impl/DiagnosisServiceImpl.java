package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.dto.request.DiagnosisSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.DiagnosisReportVO;
import com.zhilian.zhilianbackend.entity.Diagnosis;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.User;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.DiagnosisMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.UserMapper;
import com.zhilian.zhilianbackend.service.DiagnosisService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.service.algorithm.DiagnosisAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import cn.hutool.json.JSONUtil;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:30
 * @Param: 
 * @Return: 
 * @Description: 诊断记录表业务逻辑实现类，实现诊断相关的业务方法
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl extends ServiceImpl<DiagnosisMapper, Diagnosis> implements DiagnosisService {

    private final ManufactureMapper manufactureMapper;
    private final UserMapper userMapper;
    private final DiagnosisAlgorithm diagnosisAlgorithm;

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:47
     * @Param: request 诊断提交请求参数（包含企业ID和各维度得分）userId 当前操作用户ID
     * @Return: DiagnosisReportVO 诊断报告数据
     * @Description: 提交诊断问卷，计算总分、等级和建议，保存诊断记录
    **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiagnosisReportVO submitDiagnosis(DiagnosisSubmitRequest request, Long userId) {
        log.info("提交诊断问卷 - 用户ID: {}, 企业ID: {}", userId, request.getManuId());

        // 1. 验证制造企业是否存在
        Manufacture manufacture = manufactureMapper.selectById(request.getManuId());
        if (manufacture == null) {
            throw new BusinessException(404, "制造企业不存在");
        }

        // 2. 检查权限：企业创建者 或 管理员 可以提交诊断
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        boolean isAdmin = "admin".equals(user.getRole());
        boolean isOwner = manufacture.getUserId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.warn("权限不足 - 用户ID: {}, 企业创建者ID: {}, 用户角色: {}",
                    userId, manufacture.getUserId(), user.getRole());
            throw new BusinessException(403, "无权为此企业提交诊断");
        }

        // 3. 调用算法计算各项指标
        int totalScore = diagnosisAlgorithm.calculateTotalScore(
                request.getInfoScore(),
                request.getAutoScore(),
                request.getDataScore(),
                request.getServiceScore()
        );

        String level = diagnosisAlgorithm.getLevel(totalScore);
        List<String> suggestions = diagnosisAlgorithm.generateSuggestions(
                request.getInfoScore(),
                request.getAutoScore(),
                request.getDataScore(),
                request.getServiceScore()
        );

        // 4. 保存诊断记录
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setManuId(request.getManuId())
                .setInfoScore(request.getInfoScore().byteValue())
                .setAutoScore(request.getAutoScore().byteValue())
                .setDataScore(request.getDataScore().byteValue())
                .setServiceScore(request.getServiceScore().byteValue())
                .setTotalScore((byte) totalScore)
                .setLevel(level)
                .setSuggestions(JSONUtil.toJsonStr(suggestions))
                .setDiagnosisDate(new Date());

        this.save(diagnosis);
        log.info("诊断记录保存成功 - 诊断ID: {}, 总分: {}, 等级: {}", diagnosis.getId(), totalScore, level);

        // 5. 构建返回结果
        return buildDiagnosisReportVO(diagnosis);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:48
     * @Param: id 诊断记录ID userId 当前操作用户ID
     * @Return: DiagnosisReportVO 诊断报告数据
     * @Description: 根据ID获取诊断报告，并验证权限
    **/
    @Override
    public DiagnosisReportVO getDiagnosisById(Long id, Long userId) {
        log.info("获取诊断报告 - 诊断ID: {}, 用户ID: {}", id, userId);

        // 1. 查询诊断记录
        Diagnosis diagnosis = this.getById(id);
        if (diagnosis == null) {
            throw new BusinessException(404, "诊断记录不存在");
        }

        // 2. 验证权限
        Manufacture manufacture = manufactureMapper.selectById(diagnosis.getManuId());
        if (manufacture == null) {
            throw new BusinessException(404, "关联的制造企业不存在");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        boolean isAdmin = "admin".equals(user.getRole());
        boolean isOwner = manufacture.getUserId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.warn("权限不足 - 用户ID: {}, 企业创建者ID: {}, 用户角色: {}",
                    userId, manufacture.getUserId(), user.getRole());
            throw new BusinessException(403, "无权查看此诊断记录");
        }

        // 3. 构建返回结果
        return buildDiagnosisReportVO(diagnosis);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:49
     * @Param: manuId 制造企业ID userId 当前操作用户ID
     * @Return: DiagnosisReportVO 最新诊断报告数据
     * @Description: 获取企业最新诊断报告，并验证权限
    **/
    @Override
    public DiagnosisReportVO getLatestDiagnosis(Long manuId, Long userId) {
        log.info("获取企业最新诊断报告 - 企业ID: {}, 用户ID: {}", manuId, userId);

        // 1. 验证制造企业是否存在
        Manufacture manufacture = manufactureMapper.selectById(manuId);
        if (manufacture == null) {
            throw new BusinessException(404, "制造企业不存在");
        }

        // 2. 检查权限
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        boolean isAdmin = "admin".equals(user.getRole());
        boolean isOwner = manufacture.getUserId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.warn("权限不足 - 用户ID: {}, 企业创建者ID: {}, 用户角色: {}",
                    userId, manufacture.getUserId(), user.getRole());
            throw new BusinessException(403, "无权查看此企业的诊断记录");
        }

        // 3. 查询最新诊断记录
        LambdaQueryWrapper<Diagnosis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diagnosis::getManuId, manuId)
                .orderByDesc(Diagnosis::getDiagnosisDate)
                .last("LIMIT 1");

        Diagnosis diagnosis = this.getOne(wrapper);
        if (diagnosis == null) {
            throw new BusinessException(404, "该企业暂无诊断记录");
        }

        // 4. 构建返回结果
        return buildDiagnosisReportVO(diagnosis);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:49
     * @Param: diagnosis 诊断记录实体
     * @Return: DiagnosisReportVO 格式化后的诊断报告
     * @Description: 构建诊断报告响应（将Byte转为Integer，Date转为String）
    **/
    private DiagnosisReportVO buildDiagnosisReportVO(Diagnosis diagnosis) {
        DiagnosisReportVO vo = new DiagnosisReportVO();
        vo.setDiagnosisId(diagnosis.getId());
        vo.setManuId(diagnosis.getManuId());
        vo.setInfoScore(diagnosis.getInfoScore() != null ? diagnosis.getInfoScore().intValue() : null);
        vo.setAutoScore(diagnosis.getAutoScore() != null ? diagnosis.getAutoScore().intValue() : null);
        vo.setDataScore(diagnosis.getDataScore() != null ? diagnosis.getDataScore().intValue() : null);
        vo.setServiceScore(diagnosis.getServiceScore() != null ? diagnosis.getServiceScore().intValue() : null);
        vo.setTotalScore(diagnosis.getTotalScore() != null ? diagnosis.getTotalScore().intValue() : null);
        vo.setLevel(diagnosis.getLevel());

        // 解析JSON格式的建议列表
        if (diagnosis.getSuggestions() != null) {
            List<String> suggestions = JSONUtil.toList(diagnosis.getSuggestions(), String.class);
            vo.setSuggestions(suggestions);
        }

        // 获取雷达图数据
        if (diagnosis.getInfoScore() != null && diagnosis.getAutoScore() != null &&
                diagnosis.getDataScore() != null && diagnosis.getServiceScore() != null) {
            Map<String, Integer> radarData = diagnosisAlgorithm.getRadarData(
                    diagnosis.getInfoScore().intValue(),
                    diagnosis.getAutoScore().intValue(),
                    diagnosis.getDataScore().intValue(),
                    diagnosis.getServiceScore().intValue()
            );
            vo.setRadarData(radarData);
        }

        // 格式化诊断日期
        if (diagnosis.getDiagnosisDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setDiagnosisDate(sdf.format(diagnosis.getDiagnosisDate()));
        }

        return vo;
    }
}
