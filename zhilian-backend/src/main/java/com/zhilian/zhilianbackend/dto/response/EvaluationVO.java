package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:44
 * @Param:
 * @Return:
 * @Description: 评价列表返回VO，用于前端展示评价信息
**/
@Data
@Accessors(chain = true)
public class EvaluationVO {
    private Long id;                // 评价ID
    private Long coopId;            // 合作ID
    private Integer score;           // 评分（1-5星）
    private String content;          // 评价内容
    private Date createTime;         // 评价时间
    private String manufactureName;  // 评价企业名称（匿名时显示“匿名用户”）
}