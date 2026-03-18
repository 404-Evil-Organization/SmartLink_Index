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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {

    @Override
    public List<RegionListItemVO> getRegionList(RegionListQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();
        // 是否需要在内存中按区域取最新一条记录（未传任何时间参数的场景）
        boolean latestPerRegion = false;

        // 处理时间过滤
        if (query.getQuarter() != null && !query.getQuarter().isEmpty()) {
            // 解析 quarter 字符串，如 "2025Q1"
            try {
                QuarterMonthUtils.QuarterInfo quarterInfo = QuarterMonthUtils.parseQuarter(query.getQuarter());
                wrapper.eq("year", quarterInfo.getYear())
                        .eq("period_type", "quarter")
                        .eq("period_value", quarterInfo.getQuarter());
            } catch (IllegalArgumentException e) {
                // 将非法季度格式转换为业务异常，返回 400，而不是 500
                throw new BusinessException(400, "季度参数格式不正确，应为例如 2025Q1");
            }
        } else if (query.getYear() != null && query.getMonth() != null) {
            // 按年月查询
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "month")
                    .eq("period_value", query.getMonth());
        } else {
            // 未传任何时间参数：需要获取每个区域最新一期记录
            // 为避免 N+1 查询，这里通过单次查询按 region 升序、calc_time 降序排序，
            // 然后在内存中为每个 region 只保留第一条记录（即最新记录）。
            latestPerRegion = true;
            wrapper.isNotNull("region")
                    .orderByAsc("region")
                    .orderByDesc("calc_time");
        }

        // 查询所有符合条件的记录
        List<RegionIndex> list = list(wrapper);

        // 未传时间参数：按区域在内存中取最新一条记录
        if (latestPerRegion) {
            // 使用 LinkedHashMap 保证遍历顺序稳定：先遇到的即该区域 calc_time 最大的记录
            java.util.Map<String, RegionIndex> latestMap = new java.util.LinkedHashMap<>();
            for (RegionIndex entity : list) {
                String region = entity.getRegion();
                if (!StringUtils.hasText(region)) {
                    continue;
                }
                // 列表已按 region 升序、calc_time 降序排序，第一次出现即为该区域最新记录
                latestMap.putIfAbsent(region, entity);
            }
            list = latestMap.values().stream().collect(Collectors.toList());
        }
        return list.stream().map(entity -> {
            RegionListItemVO vo = new RegionListItemVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

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
            // 未传时间参数：取最新一期
            wrapper.orderByDesc("calc_time").last("LIMIT 1");
        }

        RegionIndex entity = getOne(wrapper);
        if (entity == null) {
            return null;  // 由 Controller 处理 404
        }

        RegionDetailVO vo = new RegionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        // 字段类型转换：Byte/Short 转 Integer
        vo.setYear(entity.getYear() != null ? entity.getYear().intValue() : null);
        vo.setPeriodValue(entity.getPeriodValue() != null ? entity.getPeriodValue().intValue() : null);
        return vo;
    }

    @Override
    public List<TrendItemVO> getTrend(TrendQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();
        wrapper.eq("region", query.getRegion())
                // 使用 calc_time 作为统一时间轴排序，避免 month/quarter 按字符串字典序导致乱序
                .orderByAsc("calc_time");

        // 使用 LocalDate / LocalDateTime 进行时间边界过滤，避免字符串拼接导致的隐式类型转换问题
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        if (StringUtils.hasText(query.getStart())) {
            try {
                LocalDate startDate = LocalDate.parse(query.getStart(), dateFormatter);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                wrapper.ge("calc_time", startDateTime);
            } catch (DateTimeParseException e) {
                // 非法 start 日期时忽略该条件，避免向数据库下发不可控的时间字符串
            }
        }
        if (StringUtils.hasText(query.getEnd())) {
            try {
                LocalDate endDate = LocalDate.parse(query.getEnd(), dateFormatter);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                wrapper.le("calc_time", endDateTime);
            } catch (DateTimeParseException e) {
                // 非法 end 日期时同样忽略该条件
            }
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