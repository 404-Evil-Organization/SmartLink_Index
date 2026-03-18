package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.RegionDetailQuery;
import com.zhilian.zhilianbackend.dto.request.RegionListQuery;
import com.zhilian.zhilianbackend.dto.request.TrendQuery;
import com.zhilian.zhilianbackend.dto.response.RegionDetailVO;
import com.zhilian.zhilianbackend.dto.response.RegionListItemVO;
import com.zhilian.zhilianbackend.dto.response.TrendItemVO;
import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.RegionIndexMapper;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.utils.QuarterMonthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {
    @Value("${mybatis-plus.global-config.db-config.logic-not-delete-value:1970-01-01 00:00:00}")
    private String logicNotDeletedDatetime;

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
                    .eq("period_type", "quarter")
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
            String cleanValue = cleanLogicNotDeletedDatetime(logicNotDeletedDatetime);
            wrapper.isNotNull("region")
                    .apply("id IN (SELECT t.id FROM (" +
                            "  SELECT id, region, calc_time, " +
                            "         ROW_NUMBER() OVER (PARTITION BY region ORDER BY calc_time DESC, id DESC) AS rn " +
                            "  FROM region_index " +
                            "  WHERE region IS NOT NULL AND deleted = {0}" +
                            ") t WHERE t.rn = 1)", cleanValue)
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
        if (query.getYear() != null && query.getQuarter() != null) {
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "quarter")
                    .eq("period_value", query.getQuarter());
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
            // 查无数据时抛出业务异常，由全局异常处理器统一转换为 404 响应，避免调用方出现 NPE
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
            if ("quarter".equals(entity.getPeriodType())) {
                date = entity.getYear() + "Q" + entity.getPeriodValue();
            } else {
                date = String.format("%d-%02d", entity.getYear(), entity.getPeriodValue());
            }
            vo.setDate(date);
            vo.setTotalIndex(entity.getTotalIndex());
            return vo;
        }).collect(Collectors.toList());
    }
}