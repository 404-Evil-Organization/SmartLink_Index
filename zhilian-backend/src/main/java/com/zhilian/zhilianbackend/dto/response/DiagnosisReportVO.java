package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/17 21:06
 * @Param:
 * @Return:
 * @Description: 诊断报告响应VO，返回给前端的诊断数据
 **/
@Data
public class DiagnosisReportVO {
    private Long diagnosisId;
    private Long manuId;
    private Integer infoScore;      // Byte 转 Integer 返回给前端
    private Integer autoScore;
    private Integer dataScore;
    private Integer serviceScore;
    private Integer totalScore;
    private String level;
    private List<String> suggestions;
    private Map<String, Integer> radarData;
    private String diagnosisDate;   // Date 转 String
}
