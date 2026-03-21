package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.dto.request.DiagnosisSubmitRequest;
import com.zhilian.zhilianbackend.dto.response.DiagnosisReportVO;
import com.zhilian.zhilianbackend.entity.Diagnosis;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:25
 * @Param: 
 * @Return: 
 * @Description: 诊断记录表业务逻辑接口，定义诊断相关的业务方法
**/
public interface DiagnosisService extends IService<Diagnosis> {

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:55
     * @Param: request 诊断问卷提交请求参数
     * @Return: DiagnosisReportVO 诊断报告数据
     * @Description: 提交诊断问卷，计算总分、等级和建议，保存诊断记录
    **/
    DiagnosisReportVO submitDiagnosis(DiagnosisSubmitRequest request);

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:55
     * @Param: id 诊断记录ID
     * @Return: DiagnosisReportVO 诊断报告数据
     * @Description: 根据ID获取诊断报告
    **/
    DiagnosisReportVO getDiagnosisById(Long id);

    /**
     * @Author: 6017
     * @Date: 2026/3/17 22:56
     * @Param: manuId 制造企业ID
     * @Return: DiagnosisReportVO 最新诊断报告数据
     * @Description: 获取企业最新诊断报告
    **/
    DiagnosisReportVO getLatestDiagnosis(Long manuId);

}
