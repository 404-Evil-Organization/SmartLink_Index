package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.response.DashboardStatisticsResponse;
import com.zhilian.zhilianbackend.dto.response.HeatmapDataResponse;
import com.zhilian.zhilianbackend.dto.response.NetworkDataResponse;
import com.zhilian.zhilianbackend.dto.response.TopDemandResponse;
import com.zhilian.zhilianbackend.service.DashboardService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:53
 * @Param:
 * @Return:
 * @Description: 数据可视化看板控制器
 **/
@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "数据可视化看板模块", description = "提供统计数据、热力图、热门需求、网络关系等接口")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:53
     * @Param:
     * @Return: Result<DashboardStatisticsResponse> 统计卡片数据
     * @Description: 获取统计卡片数据（需要登录）
     **/
    @GetMapping("/statistics")
    @Operation(summary = "获取统计卡片数据", description = "返回制造企业数、服务商数、需求数、合作数")
    public Result<DashboardStatisticsResponse> getStatistics() {
        // 手动校验登录态
        try {
            securityUtils.getCurrentUserId();
            log.info("获取统计卡片数据，用户已登录");
        } catch (Exception e) {
            log.warn("获取统计卡片数据失败，用户未登录");
            return Result.unauthorized("请先登录");
        }

        DashboardStatisticsResponse statistics = dashboardService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:53
     * @Param: startDate 开始日期（可选）  endDate 结束日期（可选）
     * @Return: Result<List<HeatmapDataResponse>> 热力图数据列表
     * @Description: 获取热力图数据（需要登录）
     **/
    @GetMapping("/heatmap")
    @Operation(summary = "获取热力图数据", description = "按区域统计合作次数，支持日期范围筛选")
    public Result<List<HeatmapDataResponse>> getHeatmapData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 手动校验登录态
        try {
            securityUtils.getCurrentUserId();
            log.info("获取热力图数据，用户已登录");
        } catch (Exception e) {
            log.warn("获取热力图数据失败，用户未登录");
            return Result.unauthorized("请先登录");
        }

        // 参数校验：如果同时传了 startDate 和 endDate，确保 startDate <= endDate
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            log.warn("开始日期 {} 大于结束日期 {}", startDate, endDate);
            return Result.badRequest("开始日期不能大于结束日期");
        }

        log.info("获取热力图数据，startDate: {}, endDate: {}", startDate, endDate);
        List<HeatmapDataResponse> heatmapData = dashboardService.getHeatmapData(startDate, endDate);
        return Result.success(heatmapData);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:53
     * @Param: top 返回数量，默认5
     * @Return: Result<List<TopDemandResponse>> 热门需求列表
     * @Description: 获取热门需求（需要登录）
     **/
    @GetMapping("/topDemands")
    @Operation(summary = "获取热门需求", description = "按服务类型统计需求数量，返回Top N")
    public Result<List<TopDemandResponse>> getTopDemands(
            @RequestParam(required = false, defaultValue = "5") Integer top) {

        // 手动校验登录态
        try {
            securityUtils.getCurrentUserId();
            log.info("获取热门需求，用户已登录");
        } catch (Exception e) {
            log.warn("获取热门需求失败，用户未登录");
            return Result.unauthorized("请先登录");
        }

        // 对 top 参数做合理区间约束，防止恶意传入超大值导致数据库压力过大
        if (top == null || top < 1) {
            top = 1;
        } else if (top > 50) {
            top = 50;
        }
        log.info("获取热门需求，归一化后 top: {}", top);
        List<TopDemandResponse> topDemands = dashboardService.getTopDemands(top);
        return Result.success(topDemands);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:54
     * @Param:
     * @Return: Result<NetworkDataResponse> 网络关系数据
     * @Description: 获取网络关系数据（需要登录）
     **/
    @GetMapping("/network")
    @Operation(summary = "获取网络关系数据", description = "返回制造企业和服务商之间的合作关系图数据")
    public Result<NetworkDataResponse> getNetworkData() {
        // 手动校验登录态
        try {
            securityUtils.getCurrentUserId();
            log.info("获取网络关系数据，用户已登录");
        } catch (Exception e) {
            log.warn("获取网络关系数据失败，用户未登录");
            return Result.unauthorized("请先登录");
        }

        log.info("获取网络关系数据");
        NetworkDataResponse networkData = dashboardService.getNetworkData();
        return Result.success(networkData);
    }
}