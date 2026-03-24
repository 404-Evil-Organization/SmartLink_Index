package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.dto.request.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SecurityUtils securityUtils;   // 新增注入

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

        // 调用当前代理对象的方法，确保 @Transactional 能够生效（虽然此处已不再需要大事务，但通过代理调用是好习惯）
        // 更优做法是将调度入口剥离到单独组件，或者依赖注入自身（如 @Autowired @Lazy RegionIndexService self）
        // 由于这里我们取消了 calculateAndSaveQuarterIndex 的大事务，直接调用即可
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
        // 参数校验
        if (year == null) {
            throw new BusinessException("年份参数不能为空");
        }
        if (quarter == null) {
            throw new BusinessException("季度参数不能为空");
        }

        // 这里不再重置实例级服务商区域缓存，避免并发计算时出现跨线程共享状态被清空的竞态问题

        log.info("计算季度区域指数 - 年份: {}, 季度: {}", year, quarter);

        // 1. 获取季度起止日期
        LocalDate[] dateRange = getQuarterDateRange(year, quarter);
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 2. 一次性加载该季度所有合作记录
        LambdaQueryWrapper<Cooperation> coopWrapper = new LambdaQueryWrapper<>();
        coopWrapper.between(Cooperation::getCreateTime, start, end)
                .eq(Cooperation::getDeleted, DateConstants.getNotDeletedTime());
        List<Cooperation> allCooperations = cooperationMapper.selectList(coopWrapper);
        log.debug("加载本季度合作记录数量: {}", allCooperations.size());

        // 3. 一次性加载所有制造企业，只查询必要字段（id, region）
        LambdaQueryWrapper<Manufacture> manufactureWrapper = new LambdaQueryWrapper<>();
        manufactureWrapper.select(Manufacture::getId, Manufacture::getRegion)
                .eq(Manufacture::getDeleted, DateConstants.getNotDeletedTime());
        List<Manufacture> allManufactures = manufactureMapper.selectList(manufactureWrapper);
        Map<String, List<Manufacture>> regionManufacturesMap = allManufactures.stream()
                .filter(m -> m.getRegion() != null && !m.getRegion().isEmpty())
                .collect(Collectors.groupingBy(Manufacture::getRegion));
        log.debug("区域制造企业分组完成，区域数: {}", regionManufacturesMap.size());

        // 3.5 每次计算时，一次性加载所有服务商区域信息到局部变量中，作为缓存传递，保证数据新鲜度
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
                        regionManufactures, regionCooperations, serviceProviderRegionMap);
                if (index != null) {
                    // 使用 TransactionTemplate 将每个区域的删除和插入操作包裹在独立的小事务中
                    transactionTemplate.execute(status -> {
                        try {
                            // 保证幂等：先逻辑删除旧记录，再插入新记录
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
                // 业务异常记录日志，不中断其他区域计算
                log.warn("区域 {} 季度指数计算发生业务异常: {}", region, e.getMessage(), e);
            } catch (Exception e) {
                // 系统异常：记录日志，不抛出，避免单区域失败导致整个大批次全盘崩溃
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
        // 参数校验
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

        // 统计合作相关数据
        int totalCoopCount = regionCooperations.size();
        int crossRegionCoopCount = 0;
        Set<Long> serviceUserSet = new HashSet<>();

        for (Cooperation coop : regionCooperations) {
            // totalCoopCount 已通过 size() 统计总合作次数，这里仅统计跨区域合作和参与服务的制造企业数
            if (isCrossRegionCooperation(coop, region, serviceProviderRegionMap)) {
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
     * 清理逻辑未删除值，去除可能存在的首尾单引号
     * @param raw 原始值
     * @return 清理后的值
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

        // 处理时间过滤
        String quarter = query.getQuarter();
        if (StringUtils.hasText(quarter)) {
            // 解析 quarter 字符串，如 "2025Q1"；先去除首尾空白，避免空白字符导致解析异常
            quarter = quarter.trim();
            QuarterMonthUtils.QuarterInfo quarterInfo = QuarterMonthUtils.parseQuarter(quarter);
            wrapper.eq("year", quarterInfo.getYear())
                    .eq("period_type", PERIOD_TYPE_QUARTER)
                    .eq("period_value", quarterInfo.getQuarter())
                    // 显式按 region 升序排序，避免依赖数据库默认顺序导致列表顺序不稳定
                    .orderByAsc("region");
        } else if (query.getYear() != null && query.getMonth() != null) {
            // 按年月查询，显式按 region 升序排序，保证返回顺序稳定
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "month")
                    .eq("period_value", query.getMonth())
                    .orderByAsc("region");
        } else {
            // 为避免全表排序+内存去重，改为在数据库侧通过窗口函数一次性取出每个 region 的最新记录
            // 使用 apply 方法，{0} 占位符会被替换为清理后的参数值，并由 JDBC 自动处理类型
            wrapper.isNotNull("region")
                    .apply("id IN (SELECT t.id FROM (" +
                            "  SELECT id, region, calc_time, " +
                            "         ROW_NUMBER() OVER (PARTITION BY region ORDER BY calc_time DESC, id DESC) AS rn " +
                            "  FROM region_index " +
                            "  WHERE region IS NOT NULL AND deleted = {0}" +
                            ") t WHERE t.rn = 1)", cleanedNotDeletedValue)
                    .orderByAsc("region");
        }

        // 查询所有符合条件的记录（已在数据库层完成“每个 region 取最新一条”的过滤）
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

        // 处理时间参数
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
            // 未传时间参数：取最新一期；当 calc_time 相同时按 id 倒序保证结果稳定
            wrapper.orderByDesc("calc_time")
                    .orderByDesc("id")
                    .last("LIMIT 1");
        }

        RegionIndex entity = getOne(wrapper);
        if (entity == null) {
            // 查无数据时抛出业务异常，由全局异常处理器统一转换为 code=404 的统一响应，避免调用方出现 NPE
            throw new BusinessException(404, "未找到地区【" + region + "】的指数数据");
        }

        RegionDetailVO vo = new RegionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        // 字段类型转换：Byte/Short 转 Integer
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
                // 使用 calc_time 作为统一时间轴排序，避免 month/quarter 按字符串字典序导致乱序
                .orderByAsc("calc_time");

        // 使用 LocalDate / LocalDateTime 进行时间边界过滤，并对日期格式与区间合法性做显式校验
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (StringUtils.hasText(query.getStart())) {
            try {
                startDate = LocalDate.parse(query.getStart(), dateFormatter);
            } catch (DateTimeParseException e) {
                // start 日期格式不合法时直接抛出业务异常，避免静默忽略导致误解为过滤生效
                throw new BusinessException(400, "start 日期格式不合法，正确格式为 yyyy-MM-dd");
            }
        }
        if (StringUtils.hasText(query.getEnd())) {
            try {
                endDate = LocalDate.parse(query.getEnd(), dateFormatter);
            } catch (DateTimeParseException e) {
                // end 日期格式不合法时直接抛出业务异常
                throw new BusinessException(400, "end 日期格式不合法，正确格式为 yyyy-MM-dd");
            }
        }
        // 当同时传入 start 和 end 时，校验区间合法性（start <= end）
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
            // 组装 date 字段：季度格式 "2025Q1"，月份格式 "2025-03"
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
        // 防御式授权：仅管理员可调用
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
        // 始终限定为季度数据，避免混入 month 记录导致 VO 中 quarter 语义错误
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
        // 防御式授权：仅管理员可调用
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
                // save 返回 false 通常表示未发生数据库异常但插入失败（极少见），稳妥起见转为业务异常
                log.error("保存区域指数失败，返回false，request: {}", request);
                throw new BusinessException(500, "保存区域指数失败");
            }
            return entity.getId();
        } catch (DuplicateKeyException e) {
            // 数据库唯一约束 uk_region_year_period_deleted 冲突
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
     *              仅支持修改季度类型数据
     */
    @Override
    public void adminUpdate(Long id, RegionIndexUpdateRequest request) {
        // 防御式授权：仅管理员可调用
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        RegionIndex existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "记录不存在，id=" + id);
        }

        // 校验 periodType 必须为季度，避免误改月度数据
        if (!PERIOD_TYPE_QUARTER.equals(existing.getPeriodType())) {
            throw new BusinessException(400, "该接口仅支持修改季度数据，当前记录类型为: " + existing.getPeriodType());
        }

        // 如果修改了区域、年份、季度，需要校验新组合是否唯一
        if (request.getRegion() != null && !request.getRegion().equals(existing.getRegion())
                || request.getYear() != null && request.getYear().intValue() != existing.getYear().intValue()
                || request.getQuarter() != null && request.getQuarter().intValue() != existing.getPeriodValue().intValue()) {

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

        boolean updated;
        try {
            updated = this.updateById(existing);
        } catch (DuplicateKeyException e) {
            log.warn("更新区域指数时触发唯一键冲突，id={}, region={}, year={}, quarter={}",
                    id, existing.getRegion(), existing.getYear(), existing.getPeriodValue(), e);
            throw new BusinessException(409, "目标区域、年份、季度的指数已存在");
        }
        if (!updated) {
            throw new BusinessException(404, "更新失败，记录不存在或已被删除，id=" + id);
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
        // 防御式授权：仅管理员可调用
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        RegionIndex existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "记录不存在，id=" + id);
        }
        // 校验是否为季度数据，与 adminList/adminCreate/adminUpdate 保持一致
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