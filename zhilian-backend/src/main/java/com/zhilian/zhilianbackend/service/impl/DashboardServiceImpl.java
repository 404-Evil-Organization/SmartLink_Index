package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * top 参数的最大限制值，防止恶意请求导致数据库压力过大
     */
    private static final int MAX_TOP_LIMIT = 50;

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
                        .eq(Manufacture::getDeleted, DateConstants.getNotDeletedTimeStr())
        );

        // 统计服务商数量（审核通过的）
        Long serviceCount = serviceProviderMapper.selectCount(
                new LambdaQueryWrapper<ServiceProvider>()
                        .eq(ServiceProvider::getAuditStatus, "approved")
                        .eq(ServiceProvider::getDeleted, DateConstants.getNotDeletedTime())
        );

        // 统计需求数量（审核通过的）
        Long demandCount = demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getAuditStatus, "approved")
                        .eq(Demand::getDeleted, DateConstants.getNotDeletedTime())
        );

        // 统计合作数量
        Long cooperationCount = cooperationMapper.selectCount(
                new LambdaQueryWrapper<Cooperation>()
                        .eq(Cooperation::getDeleted, DateConstants.getNotDeletedTime())
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

        // 将 LocalDate 转换为 LocalDateTime，用于 SQL 查询
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) {
            // 开始日期取当天 00:00:00
            startDateTime = startDate.atStartOfDay();
        }

        if (endDate != null) {
            // 结束日期取当天 23:59:59
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        // 传入 notDeletedTime 参数（LocalDateTime 类型）
        return cooperationMapper.getHeatmapData(startDateTime, endDateTime, DateConstants.getNotDeletedLocalDateTime());
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
        log.debug("获取热门需求，有效 top: {}", top);

        // 传入两个参数：top 和 逻辑删除时间常量（使用 LocalDateTime 类型，与 Mapper 签名匹配）
        return demandMapper.getTopDemands(top, DateConstants.getNotDeletedLocalDateTime());
    }

    /**
     * 网络关系图最大返回边数限制，防止数据量过大导致前端渲染崩溃和网络传输压力
     */
    private static final int MAX_NETWORK_LINKS = 200;

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

        // 统一使用 LocalDateTime 类型，避免数据库隐式转换导致无法命中索引
        LocalDateTime notDeletedTime = DateConstants.getNotDeletedLocalDateTime();

        // 1. 先获取受限的连接数据（按合作次数倒序取 TopN）
        List<Map<String, Object>> linkMaps = networkMapper.getCooperationLinks(notDeletedTime, MAX_NETWORK_LINKS);
        List<NetworkDataResponse.LinkDTO> links = new ArrayList<>();
        
        // 用于收集参与了 TopN 连接的节点 ID
        java.util.Set<Long> manuIds = new java.util.HashSet<>();
        java.util.Set<Long> serviceIds = new java.util.HashSet<>();

        for (Map<String, Object> map : linkMaps) {
            String source = (String) map.get("source");
            String target = (String) map.get("target");

            // source 格式为 'm' + id，target 格式为 's' + id
            if (source != null && source.startsWith("m")) {
                manuIds.add(Long.parseLong(source.substring(1)));
            }
            if (target != null && target.startsWith("s")) {
                serviceIds.add(Long.parseLong(target.substring(1)));
            }

            Object valueObj = map.get("value");
            Long value = 0L;

            if (valueObj instanceof Number) {
                value = ((Number) valueObj).longValue();
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

        // 如果没有任何 TopN 边，则不需要去数据库查询所有节点，直接返回空网络数据，避免不必要的全量查询
        if (links.isEmpty()) {
            return NetworkDataResponse.builder()
                    .nodes(new ArrayList<>())
                    .links(links)
                    .build();
        }

        // 2. 根据收集到的实际参与连接的节点 ID 集合，向数据库精确查询对应的节点信息，避免全量查询
        List<NetworkDataResponse.NodeDTO> filteredNodes = new ArrayList<>();
        if (!manuIds.isEmpty()) {
            filteredNodes.addAll(networkMapper.getManufactureNodesByIds(notDeletedTime, manuIds));
        }
        if (!serviceIds.isEmpty()) {
            filteredNodes.addAll(networkMapper.getServiceNodesByIds(notDeletedTime, serviceIds));
        }

        return NetworkDataResponse.builder()
                .nodes(filteredNodes)
                .links(links)
                .build();
    }
}