package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.RegionDetailQuery;
import com.zhilian.zhilianbackend.dto.request.RegionListQuery;
import com.zhilian.zhilianbackend.dto.request.TrendQuery;
import com.zhilian.zhilianbackend.dto.response.RegionDetailVO;
import com.zhilian.zhilianbackend.dto.response.RegionListItemVO;
import com.zhilian.zhilianbackend.dto.response.TrendItemVO;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.utils.QuarterMonthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "指数管理", description = "区域指数、趋势相关接口")
@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class IndexController {

    private final RegionIndexService regionIndexService;

    /**
     * 校验整数型时间参数（用于 RegionDetailQuery）
     */
    private String validateIntegerTimeParams(Integer year, Integer month, Integer quarter) {
        // 当指定了 month 或 quarter 时，必须同时指定 year，避免 Service 默认取最新一期导致语义偏差
        if ((month != null || quarter != null) && year == null) {
            return "时间参数不合法，当指定 month 或 quarter 时，year 不能为空";
        }
        // 仅传 year 而未指定 month 或 quarter 也视为非法，避免 year 被 Service 层忽略导致语义与结果不一致
        if (year != null && month == null && quarter == null) {
            return "时间参数不合法，不能仅指定 year，必须配合 month 或 quarter，或完全不传时间参数";
        }
        if (month != null && (month < 1 || month > 12)) {
            return "月份参数不合法，month 必须在 1-12 之间";
        }
        if (quarter != null && (quarter < 1 || quarter > 4)) {
            return "季度参数不合法，quarter 必须在 1-4 之间";
        }
        if (month != null && quarter != null) {
            return "时间参数不合法，month 与 quarter 不能同时指定";
        }
        return null;
    }

    /**
     * 校验 RegionListQuery 的字符串季度参数
     */
    private String validateRegionListQuery(RegionListQuery query) {
        // 先对 quarter 做 trim 并回写，保证与 Service 层处理逻辑一致
        String rawQuarter = query.getQuarter();
        if (rawQuarter != null) {
            String trimmedQuarter = rawQuarter.trim();
            query.setQuarter(trimmedQuarter);
        }

        // quarter 与 year/month 互斥
        boolean hasQuarter = StringUtils.hasText(query.getQuarter());
        boolean hasYearMonth = query.getYear() != null && query.getMonth() != null;

        if (hasQuarter && hasYearMonth) {
            return "不能同时使用 quarter 和 year/month 组合";
        }
        if (hasQuarter) {
            // 校验 quarter 格式
            try {
                QuarterMonthUtils.parseQuarter(query.getQuarter());
                // 可选：校验年份范围（例如不能为负数）
            } catch (BusinessException e) {
                return "季度格式错误，应为 '2025Q1' 格式";
            }
        } else if (query.getYear() != null || query.getMonth() != null) {
            // 如果提供了 year 或 month，必须同时提供两者
            if (query.getYear() == null || query.getMonth() == null) {
                return "使用年月查询时，必须同时提供 year 和 month";
            }
            // 校验 month 范围
            if (query.getMonth() < 1 || query.getMonth() > 12) {
                return "月份必须在 1-12 之间";
            }
        }
        // 若无任何时间参数，视为查询最新，不需要校验
        return null;
    }

    @Operation(summary = "获取所有区域指标", description = "支持按季度或年月筛选，不传时间参数则返回各区域最新一期")
    @GetMapping("/region/list")
    public Result<List<RegionListItemVO>> listRegions(RegionListQuery query) {
        String error = validateRegionListQuery(query);
        if (error != null) {
            return Result.badRequest(error);
        }
        List<RegionListItemVO> list = regionIndexService.getRegionList(query);
        return Result.success(list);
    }

    @Operation(summary = "获取特定区域指数", description = "可指定年份季度或年月，不传时间参数则返回该区域最新一期")
    @GetMapping("/region/{region}")
    public Result<RegionDetailVO> getRegionDetail(
            @Parameter(description = "区域名称", required = true, example = "深圳")
            @PathVariable String region,
            RegionDetailQuery query) {
        String error = validateIntegerTimeParams(query.getYear(), query.getMonth(), query.getQuarter());
        if (error != null) {
            return Result.badRequest(error);
        }
        RegionDetailVO vo = regionIndexService.getRegionDetail(region, query);
        if (vo == null) {
            return Result.notFound("该区域指数不存在");
        }
        return Result.success(vo);
    }

    @Operation(summary = "获取趋势数据", description = "返回指定区域在时间范围内的趋势数据（按周期升序）")
    @GetMapping("/trend")
    public Result<List<TrendItemVO>> getTrend(TrendQuery query) {
        if (!StringUtils.hasText(query.getRegion())) {
            return Result.badRequest("区域不能为空");
        }
        // 趋势接口不需要校验 year/month/quarter
        List<TrendItemVO> list = regionIndexService.getTrend(query);
        return Result.success(list);
    }
}