package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.RegionDetailQuery;
import com.zhilian.zhilianbackend.dto.request.RegionListQuery;
import com.zhilian.zhilianbackend.dto.request.TrendQuery;
import com.zhilian.zhilianbackend.dto.response.RegionDetailVO;
import com.zhilian.zhilianbackend.dto.response.RegionListItemVO;
import com.zhilian.zhilianbackend.dto.response.TrendItemVO;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "指数管理", description = "区域指数、趋势相关接口")
@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class IndexController {

    private final RegionIndexService regionIndexService;

    @Operation(summary = "获取所有区域指标", description = "支持按季度或年月筛选，不传时间参数则返回各区域最新一期")
    @GetMapping("/region/list")
    public Result<List<RegionListItemVO>> listRegions(RegionListQuery query) {
        List<RegionListItemVO> list = regionIndexService.getRegionList(query);
        return Result.success(list);
    }

    @Operation(summary = "获取特定区域指数", description = "可指定年份季度或年月，不传时间参数则返回该区域最新一期")
    @GetMapping("/region/{region}")
    public Result<RegionDetailVO> getRegionDetail(
            @Parameter(description = "区域名称", required = true, example = "深圳")
            @PathVariable String region,
            RegionDetailQuery query) {
        RegionDetailVO vo = regionIndexService.getRegionDetail(region, query);
        if (vo == null) {
            return Result.notFound("该区域指数不存在");
        }
        return Result.success(vo);
    }

    @Operation(summary = "获取趋势数据", description = "返回指定区域在时间范围内的趋势数据（按周期升序）")
    @GetMapping("/trend")
    public Result<List<TrendItemVO>> getTrend(TrendQuery query) {
        if (query.getRegion() == null || query.getRegion().isEmpty()) {
            return Result.badRequest("区域不能为空");
        }
        List<TrendItemVO> list = regionIndexService.getTrend(query);
        return Result.success(list);
    }
}