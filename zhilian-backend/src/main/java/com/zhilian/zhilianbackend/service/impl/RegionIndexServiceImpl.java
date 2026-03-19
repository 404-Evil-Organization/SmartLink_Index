package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.mapper.CooperationMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.mapper.RegionIndexMapper;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.service.algorithm.RegionIndexAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * @Author: 6017
 * @Date: 2026/3/18 21:52
 * @Param:
 * @Return:
 * @Description: 区域指数服务实现类，实现区域指数相关的业务方法
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {

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
     * @Description: 定时任务，每天凌晨2点计算上一季度的区域指数
    **/
    @Scheduled(cron = "0 0 2 * * ?")
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
     * @Description: 计算并保存季度区域指数
    **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateAndSaveQuarterIndex(Short year, Byte quarter) {
        log.info("计算季度区域指数 - 年份: {}, 季度: {}", year, quarter);

        // 1. 获取所有区域列表
        List<String> regions = getAllRegions();

        // 2. 为每个区域计算指数
        for (String region : regions) {
            try {
                RegionIndex index = calculateQuarterIndex(region, year, quarter);
                if (index != null) {
                    // 为保证定时任务幂等性，先删除同一 region/year/periodType/periodValue 的旧记录
                    // 使用 MyBatis Plus 的逻辑删除能力，避免物理删除历史数据
                    LambdaQueryWrapper<RegionIndex> removeWrapper = new LambdaQueryWrapper<>();
                    removeWrapper.eq(RegionIndex::getRegion, index.getRegion())
                            .eq(RegionIndex::getYear, index.getYear())
                            .eq(RegionIndex::getPeriodType, index.getPeriodType())
                            .eq(RegionIndex::getPeriodValue, index.getPeriodValue());
                    this.remove(removeWrapper);

                    // 插入最新计算的季度指数记录
                    this.save(index);
                    log.info("区域 {} 季度指数计算完成: {}", region, index.getTotalIndex());
                }
            } catch (Exception e) {
                log.error("区域 {} 季度指数计算失败", region, e);
            }
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:14
     * @Param: year 年份  quarter 季度
     * @Return: 
     * @Description: 手动触发计算（用于测试）
    **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualCalculate(Short year, Byte quarter) {
        log.info("手动触发季度计算 - 年份: {}, 季度: {}", year, quarter);
        calculateAndSaveQuarterIndex(year, quarter);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:14
     * @Param: region 区域  year 年份  quarter 季度
     * @Return: RegionIndex 区域指数实体
     * @Description: 计算单个区域的季度指数
    **/
    private RegionIndex calculateQuarterIndex(String region, Short year, Byte quarter) {
        // 获取季度起止日期
        LocalDate[] dateRange = getQuarterDateRange(year, quarter);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 1. 获取区域内的制造企业
        LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
        manuWrapper.eq(Manufacture::getRegion, region);
        List<Manufacture> manufactures = manufactureMapper.selectList(manuWrapper);
        int manufactureCount = manufactures.size();

        if (manufactureCount == 0) {
            log.warn("区域 {} 没有制造企业，跳过计算", region);
            return null;
        }

        // 2. 获取该时间段内的合作记录
        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.between(Cooperation::getCreateTime, start, end);
        List<Cooperation> cooperations = cooperationMapper.selectList(coopWrapper);

        // 3. 统计合作相关数据
        int totalCoopCount = 0;
        int crossRegionCoopCount = 0;
        Set<Long> serviceUserSet = new HashSet<>();
        Map<Long, String> manufactureRegionMap = new HashMap<>();

        for (Manufacture manu : manufactures) {
            manufactureRegionMap.put(manu.getId(), manu.getRegion());
        }

        for (Cooperation coop : cooperations) {
            String manuRegion = manufactureRegionMap.get(coop.getManuId());

            if (region.equals(manuRegion)) {
                totalCoopCount++;

                if (isCrossRegionCooperation(coop, region)) {
                    crossRegionCoopCount++;
                }

                serviceUserSet.add(coop.getManuId());
            }
        }

        int serviceUserCount = serviceUserSet.size();

        // 4. 调用算法计算各项指标
        BigDecimal coopDensity = regionIndexAlgorithm.calculateCoopDensity(totalCoopCount, manufactureCount);
        BigDecimal serviceRate = regionIndexAlgorithm.calculateServiceRate(serviceUserCount, manufactureCount);
        BigDecimal crossRate = regionIndexAlgorithm.calculateCrossRate(crossRegionCoopCount, totalCoopCount);
        BigDecimal totalIndex = regionIndexAlgorithm.calculateTotalIndex(coopDensity, serviceRate, crossRate);

        // 5. 构建实体
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
    **/
    private boolean isCrossRegionCooperation(Cooperation coop, String manuRegion) {
        ServiceProvider serviceProvider = serviceProviderMapper.selectById(coop.getServiceId());
        if (serviceProvider != null) {
            return !manuRegion.equals(serviceProvider.getRegion());
        }
        return false;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:15
     * @Param:
     * @Return: List<String> 区域列表
     * @Description: 获取所有区域列表（从数据库中动态查询 DISTINCT region，避免硬编码）
    **/
    private List<String> getAllRegions() {
        // 从制造企业表查询区域
        LambdaQueryWrapper<Manufacture> manuWrapper = new LambdaQueryWrapper<>();
        manuWrapper.isNotNull(Manufacture::getRegion);
        List<Manufacture> manuList = manufactureMapper.selectList(manuWrapper);

        // 从服务商表查询区域
        LambdaQueryWrapper<ServiceProvider> spWrapper = new LambdaQueryWrapper<>();
        spWrapper.isNotNull(ServiceProvider::getRegion);
        List<ServiceProvider> spList = serviceProviderMapper.selectList(spWrapper);

        // 使用 Set 去重，避免重复区域；使用 LinkedHashSet 保持插入顺序
        Set<String> regionSet = new LinkedHashSet<>();

        if (manuList != null) {
            regionSet.addAll(
                    manuList.stream()
                            .map(Manufacture::getRegion)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
            );
        }

        if (spList != null) {
            regionSet.addAll(
                    spList.stream()
                            .map(ServiceProvider::getRegion)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
            );
        }

        return new ArrayList<>(regionSet);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:15
     * @Param: year 年份  quarter 季度
     * @Return: LocalDate[] 起止日期数组
     * @Description: 获取季度的起止日期
    **/
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
                // 季度参数非法时抛出业务异常，方便全局异常处理返回 400 而非 500
                throw new BusinessException("季度参数不合法，必须为 1-4，实际值为: " + quarter);
        }

        return new LocalDate[]{startDate, endDate};
    }
    
}