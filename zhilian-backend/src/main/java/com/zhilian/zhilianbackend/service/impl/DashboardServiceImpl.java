package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.dto.response.*;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Demand;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.*;
import com.zhilian.zhilianbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:41
 * @Description: 数据可视化看板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final DemandMapper demandMapper;
    private final CooperationMapper cooperationMapper;
    private final NetworkMapper networkMapper;

    // 逻辑删除的默认时间常量
    private static final String NOT_DELETED_TIME = "1970-01-01 00:00:00";

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param:
     * @Return: DashboardStatisticsResponse 统计卡片数据
     * @Description: 获取统计卡片数据
     */
    @Override
    public DashboardStatisticsResponse getStatistics() {
        log.debug("获取统计卡片数据");

        // 统计制造企业数量（审核通过的）
        Long manufactureCount = manufactureMapper.selectCount(
                new LambdaQueryWrapper<Manufacture>()
                        .eq(Manufacture::getAuditStatus, "approved")
                        .eq(Manufacture::getDeleted, NOT_DELETED_TIME)
        );

        // 统计服务商数量（审核通过的）
        Long serviceCount = serviceProviderMapper.selectCount(
                new LambdaQueryWrapper<ServiceProvider>()
                        .eq(ServiceProvider::getAuditStatus, "approved")
                        .eq(ServiceProvider::getDeleted, NOT_DELETED_TIME)
        );

        // 统计需求数量（审核通过的）
        Long demandCount = demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getAuditStatus, "approved")
                        .eq(Demand::getDeleted, NOT_DELETED_TIME)
        );

        // 统计合作数量
        Long cooperationCount = cooperationMapper.selectCount(
                new LambdaQueryWrapper<Cooperation>()
                        .eq(Cooperation::getDeleted, NOT_DELETED_TIME)
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
     * @Date: 2026/3/20 21:41
     * @Param: startDate 开始日期
     * @Param: endDate 结束日期
     * @Return: List<HeatmapDataResponse> 热力图数据列表
     * @Description: 获取热力图数据
     */
    @Override
    public List<HeatmapDataResponse> getHeatmapData(LocalDate startDate, LocalDate endDate) {
        log.debug("获取热力图数据，startDate: {}, endDate: {}", startDate, endDate);

        // 将 LocalDate 转换为字符串格式，用于 SQL 查询
        String startDateTime = null;
        String endDateTime = null;

        if (startDate != null) {
            // 开始日期取当天 00:00:00
            startDateTime = startDate.atStartOfDay()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (endDate != null) {
            // 结束日期取当天 23:59:59
            endDateTime = endDate.atTime(LocalTime.MAX)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        return cooperationMapper.getHeatmapData(startDateTime, endDateTime);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: top 返回数量
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求
     */
    @Override
    public List<TopDemandResponse> getTopDemands(Integer top) {
        if (top == null || top <= 0) {
            top = 5;
        }
        log.debug("获取热门需求，top: {}", top);
        return demandMapper.getTopDemands(top, NOT_DELETED_TIME);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param:
     * @Return: NetworkDataResponse 网络关系数据
     * @Description: 获取网络关系数据
     */
    @Override
    public NetworkDataResponse getNetworkData() {
        log.debug("获取网络关系数据");

        // 获取节点数据
        List<NetworkDataResponse.NodeDTO> nodes = new ArrayList<>();
        nodes.addAll(networkMapper.getManufactureNodes());
        nodes.addAll(networkMapper.getServiceNodes());

        // 获取连接数据 - 使用传统 for 循环
        List<Map<String, Object>> linkMaps = networkMapper.getCooperationLinks();
        List<NetworkDataResponse.LinkDTO> links = new ArrayList<>();

        for (Map<String, Object> map : linkMaps) {
            String source = (String) map.get("source");
            String target = (String) map.get("target");

            Object valueObj = map.get("value");
            Long value = 0L;  // 改为 Long 类型，匹配 LinkDTO.value 的类型

            if (valueObj instanceof Number) {
                value = ((Number) valueObj).longValue();  // 使用 longValue() 获取 Long
            } else if (valueObj instanceof String) {
                try {
                    value = Long.parseLong((String) valueObj);
                } catch (NumberFormatException e) {
                    log.warn("无法将 value 转换为整数: {}", valueObj);
                }
            }

            NetworkDataResponse.LinkDTO link = NetworkDataResponse.LinkDTO.builder()
                    .source(source)
                    .target(target)
                    .value(value)
                    .build();
            links.add(link);
        }

        return NetworkDataResponse.builder()
                .nodes(nodes)
                .links(links)
                .build();
    }
}