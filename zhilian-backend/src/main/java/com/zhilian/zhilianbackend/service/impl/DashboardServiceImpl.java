package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.dto.response.DashboardStatisticsResponse;
import com.zhilian.zhilianbackend.dto.response.HeatmapDataResponse;
import com.zhilian.zhilianbackend.dto.response.NetworkDataResponse;
import com.zhilian.zhilianbackend.dto.response.TopDemandResponse;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Demand;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.DemandMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.NetworkMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:51
 * @Param: 
 * @Return: 
 * @Description: 数据可视化看板服务实现类
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final DemandMapper demandMapper;
    private final CooperationMapper cooperationMapper;
    private final NetworkMapper networkMapper;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:51
     * @Param: 
     * @Return: DashboardStatisticsResponse 统计卡片数据
     * @Description: 获取统计卡片数据
    **/
    @Override
    public DashboardStatisticsResponse getStatistics() {
        log.debug("获取统计卡片数据");

        // 统计制造企业数量（审核通过的）
        Long manufactureCount = manufactureMapper.selectCount(
                new LambdaQueryWrapper<Manufacture>()
                        .eq(Manufacture::getAuditStatus, "approved")
        );

        // 统计服务商数量（审核通过的）
        Long serviceCount = serviceProviderMapper.selectCount(
                new LambdaQueryWrapper<ServiceProvider>()
                        .eq(ServiceProvider::getAuditStatus, "approved")
        );

        // 统计需求数量（审核通过的）
        Long demandCount = demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getAuditStatus, "approved")
        );

        // 统计合作数量（仅统计未逻辑删除的数据，逻辑删除由 @TableLogic 自动处理）
        Long cooperationCount = cooperationMapper.selectCount(
                new LambdaQueryWrapper<Cooperation>()
        );

        return DashboardStatisticsResponse.builder()
                .manufactureCount(manufactureCount)
                .serviceCount(serviceCount)
                .demandCount(demandCount)
                .cooperationCount(cooperationCount)
                .build();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:52
     * @Param: startDate 开始日期  endDate 结束日期
     * @Return: List<HeatmapDataResponse> 热力图数据列表
     * @Description: 获取热力图数据
    **/
    @Override
    public List<HeatmapDataResponse> getHeatmapData(String startDate, String endDate) {
        log.debug("获取热力图数据，startDate: {}, endDate: {}", startDate, endDate);
        return cooperationMapper.getHeatmapData(startDate, endDate);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:52
     * @Param: top 返回数量
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求
    **/
    @Override
    public List<TopDemandResponse> getTopDemands(Integer top) {
        if (top == null || top <= 0) {
            top = 5;
        }
        log.debug("获取热门需求，top: {}", top);
        return demandMapper.getTopDemands(top);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:52
     * @Param: 
     * @Return: NetworkDataResponse 网络关系数据
     * @Description: 获取网络关系数据
    **/
    @Override
    public NetworkDataResponse getNetworkData() {
        log.debug("获取网络关系数据");

        // 获取节点数据
        List<NetworkDataResponse.NodeDTO> nodes = new ArrayList<>();
        nodes.addAll(networkMapper.getManufactureNodes());
        nodes.addAll(networkMapper.getServiceNodes());

        // 获取连接数据（Mapper 返回 List<Map<String, Object>>，此处需做安全类型检查）
        List<Map<String, Object>> linkMaps = networkMapper.getCooperationLinks();
        List<NetworkDataResponse.LinkDTO> links = linkMaps.stream()
                .filter(map -> map != null)
                .map(map -> {
                    Object sourceObj = map.get("source");
                    Object targetObj = map.get("target");
                    Object valueObj = map.get("value");

                    // 防御性编程：检查键是否存在且类型是否匹配，避免 ClassCastException / NullPointerException
                    if (!(sourceObj instanceof String) || !(targetObj instanceof String) || !(valueObj instanceof Number)) {
                        log.warn("网络关系数据格式异常，忽略此记录: {}", map);
                        return null;
                    }

                    return NetworkDataResponse.LinkDTO.builder()
                            .source((String) sourceObj)
                            .target((String) targetObj)
                            .value(((Number) valueObj).longValue())
                            .build();
                })
                .filter(link -> link != null)
                .collect(Collectors.toList());

        return NetworkDataResponse.builder()
                .nodes(nodes)
                .links(links)
                .build();
    }
}