package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.dto.response.*;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:50
 * @Param: 
 * @Return: 
 * @Description: 数据可视化看板服务接口
**/
public interface DashboardService {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:50
     * @Param: 
     * @Return: DashboardStatisticsResponse 统计卡片数据
     * @Description: 获取统计卡片数据
    **/
    DashboardStatisticsResponse getStatistics();

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:50
     * @Param: startDate 开始日期（可选）  endDate 结束日期（可选）
     * @Return: List<HeatmapDataResponse> 热力图数据列表
     * @Description: 获取热力图数据
    **/
    List<HeatmapDataResponse> getHeatmapData(String startDate, String endDate);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:51
     * @Param: top 返回数量，默认5
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求
    **/
    List<TopDemandResponse> getTopDemands(Integer top);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:51
     * @Param: 
     * @Return: NetworkDataResponse 网络关系数据
     * @Description: 获取网络关系数据
    **/
    NetworkDataResponse getNetworkData();
}