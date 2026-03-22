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
                        .eq(ServiceProvider::getDeleted, DateConstants.getNotDeletedTimeStr())
        );

        // 统计需求数量（审核通过的）
        Long demandCount = demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>()
                        .eq(Demand::getAuditStatus, "approved")
                        .eq(Demand::getDeleted, DateConstants.getNotDeletedTimeStr())
        );

        // 统计合作数量
        Long cooperationCount = cooperationMapper.selectCount(
                new LambdaQueryWrapper<Cooperation>()
                        .eq(Cooperation::getDeleted, DateConstants.getNotDeletedTimeStr())
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
     * @Param: top 返回数量（会被限制在 1-50 之间）
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求
     */
    @Override
    public List<TopDemandResponse> getTopDemands(Integer top) {
        // 对 top 参数做合理区间约束，防止恶意传入超大值导致数据库压力过大
        int validTop;
        if (top == null || top < 1) {
            validTop = 5;  // 默认值
            log.debug("top 参数无效（null 或 <1），使用默认值: {}", validTop);
        } else if (top > MAX_TOP_LIMIT) {
            validTop = MAX_TOP_LIMIT;
            log.warn("top 参数 {} 超过最大限制 {}，已截断为 {}", top, MAX_TOP_LIMIT, validTop);
        } else {
            validTop = top;
        }

        log.debug("获取热门需求，有效 top: {}", validTop);

        // 传入两个参数：top 和 逻辑删除时间常量（使用 LocalDateTime 类型，与 Mapper 签名匹配）
        return demandMapper.getTopDemands(validTop, DateConstants.getNotDeletedLocalDateTime());
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

        String notDeletedTime = DateConstants.getNotDeletedTimeStr();

        // 获取节点数据（只返回有有效合作关系的节点）
        List<NetworkDataResponse.NodeDTO> nodes = new ArrayList<>();
        nodes.addAll(networkMapper.getManufactureNodes(notDeletedTime));
        nodes.addAll(networkMapper.getServiceNodes(notDeletedTime));

        // 获取连接数据（只返回双方都有效的合作记录）
        List<Map<String, Object>> linkMaps = networkMapper.getCooperationLinks(notDeletedTime);
        List<NetworkDataResponse.LinkDTO> links = new ArrayList<>();

        for (Map<String, Object> map : linkMaps) {
            String source = (String) map.get("source");
            String target = (String) map.get("target");

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

        return NetworkDataResponse.builder()
                .nodes(nodes)
                .links(links)
                .build();
    }
}