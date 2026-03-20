package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.service.EvaluationService;
import com.zhilian.zhilianbackend.dto.response.EvaluationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 6017
 * @Date: 2026/3/20 20:41
 * @Param: 
 * @Return: 
 * @Description: 评价控制器，提供评价列表查询接口
**/
@Slf4j
@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 20:46
     * @Param: serviceId 服务商ID  page 页码，默认1  size 每页条数，默认10
     * @Return: Result<Page<EvaluationVO>> 评价列表分页结果
     * @Description: 获取服务商评价列表
    **/
    @GetMapping("/list/{serviceId}")
    public Result<Page<EvaluationVO>> listEvaluations(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("接收到评价列表请求，serviceId: {}, page: {}, size: {}", serviceId, page, size);

        Page<EvaluationVO> result = evaluationService.getEvaluationPage(serviceId, page, size);

        return Result.success(result);
    }
}