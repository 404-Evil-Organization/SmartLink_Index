package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.dto.response.RegionDetailVO;
import com.zhilian.zhilianbackend.dto.response.RegionIndexAdminVO;
import com.zhilian.zhilianbackend.dto.response.RegionListItemVO;
import com.zhilian.zhilianbackend.dto.response.TrendItemVO;
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
import com.zhilian.zhilianbackend.utils.QuarterMonthUtils;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import com.zhilian.zhilianbackend.dto.request.AdminRegionIndexListRequest;
import com.zhilian.zhilianbackend.dto.request.RegionDetailQuery;
import com.zhilian.zhilianbackend.dto.request.RegionIndexCreateRequest;
import com.zhilian.zhilianbackend.dto.request.RegionIndexUpdateRequest;
import com.zhilian.zhilianbackend.dto.request.RegionListQuery;
import com.zhilian.zhilianbackend.dto.request.TrendQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {

    private static final String PERIOD_TYPE_QUARTER = "quarter";

    @Value("${mybatis-plus.global-config.db-config.logic-not-delete-value:1970-01-01 00:00:00}")
    private String logicNotDeletedDatetime;

    private final ManufactureMapper manufactureMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final CooperationMapper cooperationMapper;
    private final RegionIndexAlgorithm regionIndexAlgorithm;
    private final TransactionTemplate transactionTemplate;
    private final SecurityUtils securityUtils;

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

        this.calculateAndSaveQuarterIndex(year, quarter);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:12
     * @Param: year 年份  quarter 季度
     * @Return:
     * @Description: 计算并保存季度区域指数（优化版，避免重复查询合作记录，不再使用大事务）
     */
    @Override
    public void calculateAndSaveQuarterIndex(Short year, Byte quarter) {
        if (year == null) {
            throw new BusinessException("年份参数不能为空");
        }
        if (quarter == null) {
            throw new BusinessException("季度参数不能为空");
        }

        log.info("计算季度区域指数 - 年份: {}, 季度: {}", year, quarter);

        LocalDate[] dateRange = getQuarterDateRange(year, quarter);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.between(Cooperation::getCreateTime, start, end)
                .eq(Cooperation::getDeleted, DateConstants.getNotDeletedTime());
        List<Cooperation> allCooperations = cooperationMapper.selectList(coopWrapper);
        log.debug("加载本季度合作记录数量: {}", allCooperations.size());

        LambdaQueryWrapper<Manufacture> manufactureWrapper = new LambdaQueryWrapper<>();
        manufactureWrapper.select(Manufacture::getId, Manufacture::getRegion)
                .eq(Manufacture::getDeleted, DateConstants.getNotDeletedTime());
        List<Manufacture> allManufactures = manufactureMapper.selectList(manufactureWrapper);
        Map<String, List<Manufacture>> regionManufacturesMap = allManufactures.stream()
                .filter(m -> m.getRegion() != null && !m.getRegion().isEmpty())
                .collect(Collectors.groupingBy(Manufacture::getRegion));
        log.debug("区域制造企业分组完成，区域数: {}", regionManufacturesMap.size());

        LambdaQueryWrapper<ServiceProvider> spWrapper = new LambdaQueryWrapper<>();
        spWrapper.select(ServiceProvider::getId, ServiceProvider::getRegion)
                .eq(ServiceProvider::getDeleted, DateConstants.getNotDeletedTime());
        List<ServiceProvider> allServiceProviders = serviceProviderMapper.selectList(spWrapper);
        Map<Long, String> serviceProviderRegionMap = new HashMap<>();
        if (allServiceProviders != null) {
            for (ServiceProvider sp : allServiceProviders) {
                if (sp.getId() != null && sp.getRegion() != null) {
                    serviceProviderRegionMap.put(sp.getId(), sp.getRegion());
                }
            }
        }

        Map<Long, String> manufactureIdToRegion = allManufactures.stream()
                .filter(m -> m.getId() != null && m.getRegion() != null)
                .collect(Collectors.toMap(Manufacture::getId, Manufacture::getRegion, (v1, v2) -> v1));

        Map<String, List<Cooperation>> regionCooperationsMap = new HashMap<>();
        for (Cooperation coop : allCooperations) {
            Long manuId = coop.getManuId();
            if (manuId == null) continue;
            String region = manufactureIdToRegion.get(manuId);
            if (region != null) {
                regionCooperationsMap.computeIfAbsent(region, k -> new ArrayList<>()).add(coop);
            }
        }

        for (Map.Entry<String, List<Manufacture>> entry : regionManufacturesMap.entrySet()) {
            String region = entry.getKey();
            List<Manufacture> regionManufactures = entry.getValue();
            List<Cooperation> regionCooperations = regionCooperationsMap.getOrDefault(region, Collections.emptyList());

            try {
                RegionIndex index = calculateQuarterIndexForRegion(region, year, quarter,
                        regionManufactures, regionCooperations, serviceProviderRegionMap);
                if (index != null) {
                    transactionTemplate.execute(status -> {
                        try {
                            LambdaQueryWrapper<RegionIndex> removeWrapper = new LambdaQueryWrapper<>();
                            removeWrapper.eq(RegionIndex::getRegion, index.getRegion())
                                    .eq(RegionIndex::getYear, index.getYear())
                                    .eq(RegionIndex::getPeriodType, index.getPeriodType())
                                    .eq(RegionIndex::getPeriodValue, index.getPeriodValue());
                            this.remove(removeWrapper);
                            this.save(index);
                            log.info("区域 {} 季度指数计算完成: {}", region, index.getTotalIndex());
                            return true;
                        } catch (DuplicateKeyException e) {
                            log.warn("区域 {} 季度指数插入触发唯一键冲突，可能由并发任务导致，当前计算结果将被忽略", region, e);
                            status.setRollbackOnly();
                            return false;
                        } catch (Exception e) {
                            log.error("区域 {} 季度指数保存失败", region, e);
                            status.setRollbackOnly();
                            throw e;
                        }
                    });
                }
            } catch (BusinessException e) {
                log.warn("区域 {} 季度指数计算发生业务异常: {}", region, e.getMessage(), e);
            } catch (Exception e) {
                log.error("区域 {} 季度指数计算及保存过程发生未知异常", region, e);
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
    public void manualCalculate(Short year, Byte quarter) {
        if (year == null) {
            throw new BusinessException("年份参数不能为空");
        }
        if (quarter == null) {
            throw new BusinessException("季度参数不能为空");
        }
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
     * @param serviceProviderRegionMap 服务商区域映射缓存
     * @return RegionIndex 区域指数实体，如果该区域没有制造企业则返回 null
     */
    private RegionIndex calculateQuarterIndexForRegion(String region, Short year, Byte quarter,
                                                       List<Manufacture> manufactures,
                                                       List<Cooperation> regionCooperations,
                                                       Map<Long, String> serviceProviderRegionMap) {
        int manufactureCount = manufactures.size();
        if (manufactureCount == 0) {
            log.warn("区域 {} 没有制造企业，跳过计算", region);
            return null;
        }

        int totalCoopCount = regionCooperations.size();
        int crossRegionCoopCount = 0;
        Set<Long> serviceUserSet = new HashSet<>();

        for (Cooperation coop : regionCooperations) {
            if (isCrossRegionCooperation(coop, region, serviceProviderRegionMap)) {
                crossRegionCoopCount++;
            }
            serviceUserSet.add(coop.getManuId());
        }

        int serviceUserCount = serviceUserSet.size();

        BigDecimal coopDensity = regionIndexAlgorithm.calculateCoopDensity(totalCoopCount, manufactureCount);
        BigDecimal serviceRate = regionIndexAlgorithm.calculateServiceRate(serviceUserCount, manufactureCount);
        BigDecimal crossRate = regionIndexAlgorithm.calculateCrossRate(crossRegionCoopCount, totalCoopCount);
        BigDecimal totalIndex = regionIndexAlgorithm.calculateTotalIndex(coopDensity, serviceRate, crossRate);

        RegionIndex index = new RegionIndex();
        index.setRegion(region);
        index.setYear(year);
        index.setPeriodType(PERIOD_TYPE_QUARTER);
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
     * @Param: coop 合作记录  manuRegion 制造企业区域  serviceProviderRegionMap 服务商区域缓存
     * @Return: boolean 是否跨区域
     * @Description: 判断是否是跨区域合作
     */
    private boolean isCrossRegionCooperation(Cooperation coop, String manuRegion, Map<Long, String> serviceProviderRegionMap) {
        if (coop == null || coop.getServiceId() == null || manuRegion == null) {
            return false;
        }

        Long serviceIdKey = coop.getServiceId();
        String serviceRegion = serviceProviderRegionMap.get(serviceIdKey);
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

    private String cleanedNotDeletedValue;

    @PostConstruct
    public void init() {
        cleanedNotDeletedValue = cleanLogicNotDeletedDatetime(logicNotDeletedDatetime);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @param raw 原始值
     * @return 清理后的值
     * @Description: 清理逻辑未删除值，去除可能存在的首尾单引号
     */
    private String cleanLogicNotDeletedDatetime(String raw) {
        if (raw != null && raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")) {
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

    // ============== 查询方法 ==============

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: query 查询参数
     * @Return: 区域指标列表
     * @Description: 获取所有区域指标（列表），根据查询条件过滤
     **/
    @Override
    public List<RegionListItemVO> getRegionList(RegionListQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();

        String quarter = query.getQuarter();
        if (StringUtils.hasText(quarter)) {
            quarter = quarter.trim();
            QuarterMonthUtils.QuarterInfo quarterInfo = QuarterMonthUtils.parseQuarter(quarter);
            wrapper.eq("year", quarterInfo.getYear())
                    .eq("period_type", PERIOD_TYPE_QUARTER)
                    .eq("period_value", quarterInfo.getQuarter())
                    .orderByAsc("region");
        } else if (query.getYear() != null && query.getMonth() != null) {
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "month")
                    .eq("period_value", query.getMonth())
                    .orderByAsc("region");
        } else {
            wrapper.isNotNull("region")
                    .apply("id IN (SELECT t.id FROM (" +
                            "  SELECT id, region, calc_time, " +
                            "         ROW_NUMBER() OVER (PARTITION BY region ORDER BY calc_time DESC, id DESC) AS rn " +
                            "  FROM region_index " +
                            "  WHERE region IS NOT NULL AND deleted = {0}" +
                            ") t WHERE t.rn = 1)", cleanedNotDeletedValue)
                    .orderByAsc("region");
        }

        List<RegionIndex> list = list(wrapper);
        return list.stream().map(entity -> {
            RegionListItemVO vo = new RegionListItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: region 区域名称
     * @Param: query 查询参数
     * @Return: 区域详情
     * @Description: 获取特定区域指数，根据时间参数过滤
     **/
    @Override
    public RegionDetailVO getRegionDetail(String region, RegionDetailQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();
        wrapper.eq("region", region);

        if (StringUtils.hasText(query.getQuarter())) {
            String quarterStr = query.getQuarter().trim();
            QuarterMonthUtils.QuarterInfo quarterInfo = QuarterMonthUtils.parseQuarter(quarterStr);
            wrapper.eq("year", quarterInfo.getYear())
                    .eq("period_type", PERIOD_TYPE_QUARTER)
                    .eq("period_value", quarterInfo.getQuarter());
        } else if (query.getYear() != null && query.getMonth() != null) {
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "month")
                    .eq("period_value", query.getMonth());
        } else {
            wrapper.orderByDesc("calc_time")
                    .orderByDesc("id")
                    .last("LIMIT 1");
        }

        RegionIndex entity = getOne(wrapper);
        if (entity == null) {
            throw new BusinessException(404, "未找到地区【" + region + "】的指数数据");
        }

        RegionDetailVO vo = new RegionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setYear(entity.getYear() != null ? entity.getYear().intValue() : null);
        vo.setPeriodValue(entity.getPeriodValue() != null ? entity.getPeriodValue().intValue() : null);
        return vo;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: query 查询参数
     * @Return: 趋势数据列表
     * @Description: 获取趋势数据，按时间范围过滤并排序
     **/
    @Override
    public List<TrendItemVO> getTrend(TrendQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();
        wrapper.eq("region", query.getRegion())
                .orderByAsc("calc_time");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (StringUtils.hasText(query.getStart())) {
            try {
                startDate = LocalDate.parse(query.getStart(), dateFormatter);
            } catch (DateTimeParseException e) {
                throw new BusinessException(400, "start 日期格式不合法，正确格式为 yyyy-MM-dd");
            }
        }
        if (StringUtils.hasText(query.getEnd())) {
            try {
                endDate = LocalDate.parse(query.getEnd(), dateFormatter);
            } catch (DateTimeParseException e) {
                throw new BusinessException(400, "end 日期格式不合法，正确格式为 yyyy-MM-dd");
            }
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "start 日期不能晚于 end 日期");
        }
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            wrapper.ge("calc_time", startDateTime);
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            wrapper.le("calc_time", endDateTime);
        }

        List<RegionIndex> list = list(wrapper);
        return list.stream().map(entity -> {
            TrendItemVO vo = new TrendItemVO();
            String date;
            if (PERIOD_TYPE_QUARTER.equals(entity.getPeriodType())) {
                date = entity.getYear() + "Q" + entity.getPeriodValue();
            } else {
                date = String.format("%d-%02d", entity.getYear(), entity.getPeriodValue());
            }
            vo.setDate(date);
            vo.setTotalIndex(entity.getTotalIndex());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: request 管理员列表查询请求参数，包含分页及筛选条件
     * @Return: 分页结果，封装 RegionIndexAdminVO 列表
     * @Description: 管理员分页查询区域指数，支持按区域、年份筛选，默认按计算时间倒序
     */
    @Override
    public IPage<RegionIndexAdminVO> adminList(AdminRegionIndexListRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        Page<RegionIndex> page = new Page<>(request.getPage(), request.getSize());

        LambdaQueryWrapper<RegionIndex> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getRegion())) {
            wrapper.eq(RegionIndex::getRegion, request.getRegion());
        }
        if (request.getYear() != null) {
            wrapper.eq(RegionIndex::getYear, request.getYear());
        }
        wrapper.eq(RegionIndex::getPeriodType, PERIOD_TYPE_QUARTER);
        wrapper.orderByDesc(RegionIndex::getCalcTime)
                .orderByDesc(RegionIndex::getId);

        IPage<RegionIndex> entityPage = this.page(page, wrapper);
        return entityPage.convert(entity -> {
            RegionIndexAdminVO vo = new RegionIndexAdminVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setYear(entity.getYear() != null ? entity.getYear().intValue() : null);
            vo.setQuarter(entity.getPeriodValue() != null ? entity.getPeriodValue().intValue() : null);
            return vo;
        });
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: request 新增区域指数请求参数
     * @Return: 新增记录的ID
     * @Description: 管理员新增区域指数，依赖数据库唯一约束保证原子性，冲突时返回409
     */
    @Override
    public Long adminCreate(RegionIndexCreateRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        RegionIndex entity = new RegionIndex();
        entity.setRegion(request.getRegion());
        entity.setYear(request.getYear().shortValue());
        entity.setPeriodType(PERIOD_TYPE_QUARTER);
        entity.setPeriodValue(request.getQuarter().byteValue());
        entity.setCoopDensity(request.getCoopDensity());
        entity.setServiceRate(request.getServiceRate());
        entity.setCrossRate(request.getCrossRate());
        entity.setTotalIndex(request.getTotalIndex());
        entity.setCalcTime(new Date());

        try {
            boolean success = this.save(entity);
            if (!success) {
                log.error("保存区域指数失败，返回false，request: {}", request);
                throw new BusinessException(500, "保存区域指数失败");
            }
            return entity.getId();
        } catch (DuplicateKeyException e) {
            log.warn("新增区域指数时触发唯一键冲突，region={}, year={}, quarter={}",
                    request.getRegion(), request.getYear(), request.getQuarter(), e);
            throw new BusinessException(409, "该区域、年份、季度的指数已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: id 记录ID
     * @Param: request 修改区域指数请求参数（部分字段可选）
     * @Return: 无返回值
     * @Description: 管理员修改区域指数，若修改区域/年份/季度需校验新组合唯一性，记录不存在时抛出404异常，
     *              仅支持修改季度类型数据。当请求体无任何可更新字段时返回400；当字段值无实际变更时幂等成功。
     */
    @Override
    public void adminUpdate(Long id, RegionIndexUpdateRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 校验至少有一个可更新字段
        if (request.getRegion() == null && request.getYear() == null && request.getQuarter() == null
                && request.getCoopDensity() == null && request.getServiceRate() == null
                && request.getCrossRate() == null && request.getTotalIndex() == null) {
            throw new BusinessException(400, "至少提供一个可更新字段（region、year、quarter、coopDensity、serviceRate、crossRate、totalIndex）");
        }

        RegionIndex existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "记录不存在，id=" + id);
        }

        if (!PERIOD_TYPE_QUARTER.equals(existing.getPeriodType())) {
            throw new BusinessException(400, "该接口仅支持修改季度数据，当前记录类型为: " + existing.getPeriodType());
        }

        // 如果修改了区域、年份、季度，需要校验新组合是否唯一
        // 通过中间布尔变量明确表达每个维度是否发生变化，避免依赖 && / || 的运算符优先级导致歧义
        boolean regionChanged = request.getRegion() != null
                && !request.getRegion().equals(existing.getRegion());
        boolean yearChanged = request.getYear() != null
                && request.getYear().intValue() != existing.getYear().intValue();
        boolean quarterChanged = request.getQuarter() != null
                && request.getQuarter().intValue() != existing.getPeriodValue().intValue();
        if (regionChanged || yearChanged || quarterChanged) {

            String newRegion = request.getRegion() != null ? request.getRegion() : existing.getRegion();
            Short newYear = request.getYear() != null ? request.getYear().shortValue() : existing.getYear();
            Byte newQuarter = request.getQuarter() != null ? request.getQuarter().byteValue() : existing.getPeriodValue();

            LambdaQueryWrapper<RegionIndex> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(RegionIndex::getRegion, newRegion)
                    .eq(RegionIndex::getYear, newYear)
                    .eq(RegionIndex::getPeriodType, PERIOD_TYPE_QUARTER)
                    .eq(RegionIndex::getPeriodValue, newQuarter)
                    .ne(RegionIndex::getId, id);
            if (this.count(checkWrapper) > 0) {
                throw new BusinessException(409, "目标区域、年份、季度的指数已存在");
            }
        }

        // 更新字段
        if (request.getRegion() != null) {
            existing.setRegion(request.getRegion());
        }
        if (request.getYear() != null) {
            existing.setYear(request.getYear().shortValue());
        }
        if (request.getQuarter() != null) {
            existing.setPeriodValue(request.getQuarter().byteValue());
        }
        if (request.getCoopDensity() != null) {
            existing.setCoopDensity(request.getCoopDensity());
        }
        if (request.getServiceRate() != null) {
            existing.setServiceRate(request.getServiceRate());
        }
        if (request.getCrossRate() != null) {
            existing.setCrossRate(request.getCrossRate());
        }
        if (request.getTotalIndex() != null) {
            existing.setTotalIndex(request.getTotalIndex());
        }

        try {
            boolean updated = this.updateById(existing);
            if (updated) {
                log.info("区域指数修改成功，id={}", id);
            } else {
                // 未发生实际变更（字段值与原值相同），幂等成功，不抛异常
                log.info("区域指数未发生实际变更，id={}", id);
            }
        } catch (DuplicateKeyException e) {
            log.warn("更新区域指数时触发唯一键冲突，id={}, region={}, year={}, quarter={}",
                    id, existing.getRegion(), existing.getYear(), existing.getPeriodValue(), e);
            throw new BusinessException(409, "目标区域、年份、季度的指数已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/24
     * @Param: id 记录ID
     * @Return: 无返回值
     * @Description: 管理员删除区域指数（逻辑删除），仅支持删除季度数据
     */
    @Override
    public void adminDelete(Long id) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        RegionIndex existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "记录不存在，id=" + id);
        }
        if (!PERIOD_TYPE_QUARTER.equals(existing.getPeriodType())) {
            throw new BusinessException(400, "该接口仅支持删除季度数据，当前记录类型为: " + existing.getPeriodType());
        }
        boolean deleted = this.removeById(id);
        if (!deleted) {
            log.error("删除区域指数失败，id={}", id);
            throw new BusinessException(500, "删除记录失败");
        }
    }
}