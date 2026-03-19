package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.response.CreditScoreVO;
import com.zhilian.zhilianbackend.entity.CreditScore;
import com.zhilian.zhilianbackend.service.CreditScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 16:30
 * @Description: 信用评价模块控制器，提供获取服务商信用分接口
 */
@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
@Tag(name = "信用评价模块")
public class CreditController {

    private final CreditScoreService creditScoreService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: serviceId 服务商ID
     * @Return: 信用分信息（可能为null）
     * @Description: 根据服务商ID获取最新信用分
     */
    @GetMapping("/{serviceId}")
    @Operation(summary = "获取服务商信用分")
    public Result<CreditScoreVO> getCreditScore(@PathVariable Long serviceId) {
        CreditScore creditScore = creditScoreService.getLatestByServiceId(serviceId);
        if (creditScore == null) {
            return Result.success(null);
        }
        CreditScoreVO vo = convertToVO(creditScore);
        return Result.success(vo);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: entity 信用分实体
     * @Return: 信用分视图对象
     * @Description: 将实体转换为VO，处理字段名差异
     */
    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 16:30
     * @Param: entity 信用分实体
     * @Return: 信用分视图对象
     * @Description: 将实体转换为VO，处理字段名差异（calcTime -> calTime）
     */
    private CreditScoreVO convertToVO(CreditScore entity) {
        CreditScoreVO vo = new CreditScoreVO();
        vo.setServiceId(entity.getServiceId());
        vo.setScore(entity.getScore());           // 类型已匹配，直接赋值
        vo.setQualScore(entity.getQualScore());
        vo.setCaseScore(entity.getCaseScore());
        vo.setEvalScore(entity.getEvalScore());
        vo.setCalTime(entity.getCalcTime());       // 字段名不一致，手动设置
        return vo;
    }
}