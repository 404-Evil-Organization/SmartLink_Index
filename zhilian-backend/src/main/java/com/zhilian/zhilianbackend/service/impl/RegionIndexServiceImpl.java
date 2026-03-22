package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.RegionIndexMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.service.algorithm.RegionIndexAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/18 21:52
 * @Param:
 * @Return:
 * @Description: 区域指数服务实现类，实现区域指数相关的业务方法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {

    /**
     * 本地缓存 ServiceProvider 的区域映射，key 为服务商主键 ID，value 为其所属区域 region。
     * 说明：
     * - 季度定时任务在单线程环境执行，一次性加载所有服务商区域信息到内存，可以有效避免
     *   isCrossRegionCooperation 在循环中对每条合作记录都执行一次 selectById 造成的 N+1 查询问题。
     * - 若未来有服务商区域数据变动场景，可在相关更新逻辑中主动清空该缓存或重建。
     */
    private final Map<Long, String> serviceProviderRegionCache = new HashMap<>();
    private volatile boolean cacheLoaded = false;

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final CooperationMapper cooperationMapper;
    private final RegionIndexAlgorithm regionIndexAlgorithm;

    // ============== 计算和定时任务方法 ==============

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:12
     * @Param:
     * @Return:
     * @Description: 定时任务，每季度首月1日凌晨2点计算上一季度的区域指数
     *
     * 说明：
     * - 使用 cron "0 0 2 1 1,4,7,10 ?" 表示每年 1、4、7、10 月 1 日 02:00 执行一次，
     *   配合方法内部“推算上一季度”的逻辑，确保每个自然季度只计算一次，避免整个季度期间重复重算。
     */
    @Scheduled(cron = "0 0 2 1 1,4,7,10 ?")
    @Transactional(rollbackFor = Exception.class)
    public void scheduledCalculateQuarter() {
        log.info("开始定时计算季度区域指数");

        // 获取上一季度
        LocalDate now = LocalDate.now();
        Short year = (short) now.getYear();
        Byte quarter;

        int month = now.getMonthValue();
        if (month <= 3) {
            year = (short) (year - 1);
            quarter = 4;
        } else if (month <= 6) {
            quarter = 1;
        } else if (month <= 9) {
            quarter = 2;
        } else {
            quarter = 3;
        }

        calculateAndSaveQuarterIndex(year, quarter);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:12
     * @Param: year 年份  quarter 季度
     * @Return:
     * @Description: 计算并保存季度区域指数（优化版，避免重复查询合作记录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateAndSaveQuarterIndex(Short year, Byte quarter) {
        // 重置服务商区域缓存，确保本次计算使用最新的服务商数据
        synchronized (serviceProviderRegionCache) {
            serviceProviderRegionCache.clear();
            cacheLoaded = false;
        }

        log.info("计算季度区域指数 - 年份: {}, 季度: {}", year, quarter);

        // 1. 获取季度起止日期
        LocalDate[] dateRange = getQuarterDateRange(year, quarter);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 2. 一次性加载该季度所有合作记录
        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.between(Cooperation::getCreateTime, start, end);
        List<Cooperation> allCooperations = cooperationMapper.selectList(coopWrapper);
        log.debug("加载本季度合作记录数量: {}", allCooperations.size());

        // 3. 一次性加载所有制造企业，并按区域分组
        List<Manufacture> allManufactures = manufactureMapper.selectList(null);
        Map<String, List<Manufacture>> regionManufacturesMap = allManufactures.stream()
                .filter(m -> m.getRegion() != null && !m.getRegion().isEmpty())
                .collect(Collectors.groupingBy(Manufacture::getRegion));
        log.debug("区域制造企业分组完成，区域数: {}", regionManufacturesMap.size());

        // 4. 构建制造企业 id -> region 映射（用于合作记录分组）
        Map<Long, String> manufactureIdToRegion = allManufactures.stream()
                .filter(m -> m.getId() != null && m.getRegion() != null)
                .collect(Collectors.toMap(Manufacture::getId, Manufacture::getRegion, (v1, v2) -> v1));

        // 5. 将合作记录按制造企业所属区域分组
        Map<String, List<Cooperation>> regionCooperationsMap = new HashMap<>();
        for (Cooperation coop : allCooperations) {
            Long manuId = coop.getManuId();
            if (manuId == null) continue;
            String region = manufactureIdToRegion.get(manuId);
            if (region != null) {
                regionCooperationsMap.computeIfAbsent(region, k -> new ArrayList<>()).add(coop);
            }
        }

        // 6. 遍历每个有制造企业的区域，计算指数
        for (Map.Entry<String, List<Manufacture>> entry : regionManufacturesMap.entrySet()) {
            String region = entry.getKey();
            List<Manufacture> regionManufactures = entry.getValue();
            List<Cooperation> regionCooperations = regionCooperationsMap.getOrDefault(region, Collections.emptyList());

            try {
                RegionIndex index = calculateQuarterIndexForRegion(region, year, quarter,
                        regionManufactures, regionCooperations);
                if (index != null) {
                    // 保证幂等：先逻辑删除旧记录，再插入新记录
                    LambdaQueryWrapper<RegionIndex> removeWrapper = new LambdaQueryWrapper<>();
                    removeWrapper.eq(RegionIndex::getRegion, index.getRegion())
                            .eq(RegionIndex::getYear, index.getYear())
                            .eq(RegionIndex::getPeriodType, index.getPeriodType())
                            .eq(RegionIndex::getPeriodValue, index.getPeriodValue());
                    this.remove(removeWrapper);

                    try {
                        this.save(index);
                        log.info("区域 {} 季度指数计算完成: {}", region, index.getTotalIndex());
                    } catch (DuplicateKeyException e) {
                        log.warn("区域 {} 季度指数插入触发唯一键冲突，可能由并发任务导致，当前计算结果将被忽略", region, e);
                    }
                }
            } catch (BusinessException e) {
                // 业务异常直接透传
                log.warn("区域 {} 季度指数计算发生业务异常: {}", region, e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                // 系统异常：记录并抛出，触发事务回滚
                log.error("区域 {} 季度指数计算失败", region, e);
                throw new RuntimeException(String.format("区域 %s 季度指数计算失败，事务已回滚", region), e);
            }
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:14
     * @Param: year 年份  quarter 季度
     * @Return:
     * @Description: 手动触发计算（用于测试）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualCalculate(Short year, Byte quarter) {
        log.info("手动触发季度计算 - 年份: {}, 季度: {}", year, quarter);
        calculateAndSaveQuarterIndex(year, quarter);
    }

    /**
     * 计算单个区域的季度指数（使用预加载的数据，避免重复查询）
     *
     * @param region               区域
     * @param year                 年份
     * @param quarter              季度
     * @param manufactures         该区域所有制造企业列表
     * @param regionCooperations   该区域所有合作记录列表
     * @return RegionIndex 区域指数实体，如果该区域没有制造企业则返回 null
     */
    private RegionIndex calculateQuarterIndexForRegion(String region, Short year, Byte quarter,
                                                       List<Manufacture> manufactures,
                                                       List<Cooperation> regionCooperations) {
        int manufactureCount = manufactures.size();
        if (manufactureCount == 0) {
            log.warn("区域 {} 没有制造企业，跳过计算", region);
            return null;
        }

        // 统计合作相关数据
        int totalCoopCount = regionCooperations.size();
        int crossRegionCoopCount = 0;
        Set<Long> serviceUserSet = new HashSet<>();

        for (Cooperation coop : regionCooperations) {
            // totalCoopCount 已通过 size() 统计总合作次数，这里仅统计跨区域合作和参与服务的制造企业数
            if (isCrossRegionCooperation(coop, region)) {
                crossRegionCoopCount++;
            }
            serviceUserSet.add(coop.getManuId());
        }

        int serviceUserCount = serviceUserSet.size();

        // 调用算法计算各项指标
        BigDecimal coopDensity = regionIndexAlgorithm.calculateCoopDensity(totalCoopCount, manufactureCount);
        BigDecimal serviceRate = regionIndexAlgorithm.calculateServiceRate(serviceUserCount, manufactureCount);
        BigDecimal crossRate = regionIndexAlgorithm.calculateCrossRate(crossRegionCoopCount, totalCoopCount);
        BigDecimal totalIndex = regionIndexAlgorithm.calculateTotalIndex(coopDensity, serviceRate, crossRate);

        // 构建实体
        RegionIndex index = new RegionIndex();
        index.setRegion(region);
        index.setYear(year);
        index.setPeriodType("quarter");
        index.setPeriodValue(quarter);
        index.setCoopDensity(coopDensity);
        index.setServiceRate(serviceRate);
        index.setCrossRate(crossRate);
        index.setTotalIndex(totalIndex);
        index.setCalcTime(new Date());

        return index;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:14
     * @Param: coop 合作记录  manuRegion 制造企业区域
     * @Return: boolean 是否跨区域
     * @Description: 判断是否是跨区域合作
     */
    private boolean isCrossRegionCooperation(Cooperation coop, String manuRegion) {
        if (coop == null || coop.getServiceId() == null || manuRegion == null) {
            return false;
        }

        Long serviceIdKey = coop.getServiceId();

        // 双重检查锁，保证加载标记与缓存数据一致
        if (!cacheLoaded) {
            synchronized (serviceProviderRegionCache) {
                if (!cacheLoaded) {
                    List<ServiceProvider> allServiceProviders = serviceProviderMapper.selectList(null);
                    serviceProviderRegionCache.clear();
                    if (allServiceProviders != null && !allServiceProviders.isEmpty()) {
                        for (ServiceProvider sp : allServiceProviders) {
                            if (sp != null && sp.getId() != null && sp.getRegion() != null) {
                                serviceProviderRegionCache.put(sp.getId(), sp.getRegion());
                            }
                        }
                    }
                    cacheLoaded = true;
                }
            }
        }

        String serviceRegion = serviceProviderRegionCache.get(serviceIdKey);
        if (serviceRegion == null) {
            return false;
        }
        return !manuRegion.equals(serviceRegion);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:15
     * @Param: year 年份  quarter 季度
     * @Return: LocalDate[] 起止日期数组
     * @Description: 获取季度的起止日期
     */
    private LocalDate[] getQuarterDateRange(Short year, Byte quarter) {
        LocalDate startDate;
        LocalDate endDate;
        switch (quarter) {
            case 1:
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 3, 31);
                break;
            case 2:
                startDate = LocalDate.of(year, 4, 1);
                endDate = LocalDate.of(year, 6, 30);
                break;
            case 3:
                startDate = LocalDate.of(year, 7, 1);
                endDate = LocalDate.of(year, 9, 30);
                break;
            case 4:
                startDate = LocalDate.of(year, 10, 1);
                endDate = LocalDate.of(year, 12, 31);
                break;
            default:
                throw new BusinessException("季度参数不合法，必须为 1-4，实际值为: " + quarter);
        }
        return new LocalDate[]{startDate, endDate};
    }
}