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

        // 分页参数合法性校验与范围限制，防止 page/size 为 0、负数或过大导致分页异常或一次性返回过多数据
        if (page == null || page < 1) {
            log.warn("收到非法分页参数 page: {}，已重置为 1，serviceId: {}", page, serviceId);
            page = 1;
        }
        if (size == null || size <= 0) {
            log.warn("收到非法分页参数 size: {}，已重置为默认值 10，serviceId: {}", size, serviceId);
            size = 10;
        }
        // 可以根据项目统一规范调整最大分页大小，这里以 100 作为示例上限
        int maxPageSize = 100;
        if (size > maxPageSize) {
            log.warn("收到超大分页参数 size: {}，已限制为最大值 {}，serviceId: {}", size, maxPageSize, serviceId);
            size = maxPageSize;
        }
        log.info("接收到评价列表请求，serviceId: {}, page: {}, size: {}", serviceId, page, size);

        Page<EvaluationVO> result = evaluationService.getEvaluationPage(serviceId, page, size);

        return Result.success(result);
    }
}