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
import org.springframework.dao.DataAccessException;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Collections;

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
     * 校验单个诊断维度得分是否合法（1-5 分），并安全转换为 byte。
     * 说明：即使 Controller 已做校验，这里仍在 Service 层进行兜底校验，
     * 防止其他调用方绕过 Controller 直接调用 Service 时写入脏数据。
     *
     * @param score   维度得分（来自请求）
     * @param field   维度字段名（用于日志打印）
     * @param manuId  企业ID（用于日志打印）
     * @param userId  当前用户ID（用于日志打印）
     * @return        合法的 byte 值（1-5）
     */
    private byte validateDimensionScore(Integer score, String field, Long manuId, Long userId) {
        if (score == null) {
            log.error("诊断维度得分为空 - 字段: {}, 用户ID: {}, 企业ID: {}", field, userId, manuId);
            throw new BusinessException(400, "诊断问卷各维度得分不能为空，请填写完整后重试");
        }
        if (score < 1 || score > 5) {
            log.error("诊断维度得分超出合法范围[1,5] - 字段: {}, 得分: {}, 用户ID: {}, 企业ID: {}",
                    field, score, userId, manuId);
            throw new BusinessException(400, "诊断问卷各维度得分必须在 1-5 分之间，请检查后重试");
        }
        return score.byteValue();
    }

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
        // 未登录时直接返回 401，避免后续 selectById(null) 导致错误的 404/403 或底层异常
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

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

        // 3. 调用算法计算各项指标前的兜底校验，避免 Integer 自动拆箱导致 NPE
        if (request == null) {
            log.error("提交诊断请求对象为空 - 用户ID: {}", userId);
            throw new BusinessException(400, "诊断提交参数不能为空");
        }

        // 先对四个维度得分做非空及 1-5 范围校验，再将校验通过的结果传给算法和持久化层
        byte infoScore = validateDimensionScore(request.getInfoScore(), "infoScore",
                request.getManuId(), userId);
        byte autoScore = validateDimensionScore(request.getAutoScore(), "autoScore",
                request.getManuId(), userId);
        byte dataScore = validateDimensionScore(request.getDataScore(), "dataScore",
                request.getManuId(), userId);
        byte serviceScore = validateDimensionScore(request.getServiceScore(), "serviceScore",
                request.getManuId(), userId);

        int totalScore = diagnosisAlgorithm.calculateTotalScore(
                infoScore,
                autoScore,
                dataScore,
                serviceScore
        );

        String level = diagnosisAlgorithm.getLevel(totalScore);
        List<String> suggestions = diagnosisAlgorithm.generateSuggestions(
                infoScore,
                autoScore,
                dataScore,
                serviceScore
        );

        // 4. 校验总分范围，避免违反数据库 total_score 0-100 CHECK 约束
        // 说明：如果算法或输入异常导致总分超出 [0,100]，这里抛出明确的业务异常，
        // 而不是让底层数据库约束异常冒泡为通用 500，便于前端提示和问题排查。
        if (totalScore < 0 || totalScore > 100) {
            log.error("诊断总分超出合法范围[0,100] - 计算结果: {}, 用户ID: {}, 企业ID: {}",
                    totalScore, userId, request.getManuId());
            throw new BusinessException(400, "诊断总分计算异常，请检查各维度评分是否在合法范围内（1-5 分）");
        }

        // 5. 保存诊断记录
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setManuId(request.getManuId())
                // 在 Service 层对各维度得分做 1-5 范围校验后再转换为 byte，避免 Integer 溢出为 Byte 及数据库 CHECK 异常
                .setInfoScore(infoScore)
                .setAutoScore(autoScore)
                .setDataScore(dataScore)
                .setServiceScore(serviceScore)
                .setTotalScore((byte) totalScore)
                .setLevel(level)
                .setSuggestions(JSONUtil.toJsonStr(suggestions))
                .setDiagnosisDate(new Date());

        try {
            boolean saved = this.save(diagnosis);
            if (!saved) {
                // MyBatis Plus save 返回 false 说明未成功插入任何记录，此时不应继续后续逻辑
                log.error("诊断记录保存失败（save 返回 false）- 企业ID: {}, 总分: {}, 等级: {}",
                        request.getManuId(), totalScore, level);
                throw new BusinessException(500, "诊断记录保存失败，请稍后重试");
            }
        } catch (DataAccessException e) {
            // 捕获底层数据库访问异常（如 CHECK 约束、外键约束、连接异常等），统一转换为业务异常
            log.error("诊断记录持久化异常 - 企业ID: {}, 总分: {}, 等级: {}",
                    request.getManuId(), totalScore, level, e);
            throw new BusinessException(500, "诊断记录保存异常，请稍后重试");
        }

        log.info("诊断记录保存成功 - 诊断ID: {}, 企业ID: {}, 总分: {}, 等级: {}",
                diagnosis.getId(), request.getManuId(), totalScore, level);

        // 6. 构建返回结果
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
        // 未登录用户不允许访问诊断报告，避免 userId 为 null 导致底层异常或错误状态码
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

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

        // 登录校验：userId 为空视为未登录
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

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

        // 解析JSON格式的建议列表，防御历史脏数据/非法JSON，避免因单条坏数据导致接口整体500
        if (diagnosis.getSuggestions() != null) {
            try {
                List<String> suggestions = JSONUtil.toList(diagnosis.getSuggestions(), String.class);
                vo.setSuggestions(suggestions);
            } catch (Exception e) {
                // 不中断整体诊断报告查询，仅记录错误并降级为空列表，后续可根据日志排查并修复脏数据
                log.error("解析诊断建议JSON失败，diagnosisId={}, suggestions={}", diagnosis.getId(), diagnosis.getSuggestions(), e);
                vo.setSuggestions(Collections.emptyList());
            }
        } else {
            // 当历史数据中建议字段为 null 时，同样返回空列表，保证响应结构稳定
            vo.setSuggestions(Collections.emptyList());
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
