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
import com.zhilian.zhilianbackend.mapper.RegionIndexMapper;
import com.zhilian.zhilianbackend.service.RegionIndexService;
import com.zhilian.zhilianbackend.utils.QuarterMonthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionIndexServiceImpl extends ServiceImpl<RegionIndexMapper, RegionIndex> implements RegionIndexService {

    @Override
    public List<RegionListItemVO> getRegionList(RegionListQuery query) {
        QueryWrapper<RegionIndex> wrapper = new QueryWrapper<>();

        // 处理时间过滤
        if (query.getQuarter() != null && !query.getQuarter().isEmpty()) {
            // 解析 quarter 字符串，如 "2025Q1"
            QuarterMonthUtils.QuarterInfo quarterInfo = QuarterMonthUtils.parseQuarter(query.getQuarter());
            wrapper.eq("year", quarterInfo.getYear())
                    .eq("period_type", "quarter")
                    .eq("period_value", quarterInfo.getQuarter());
        } else if (query.getYear() != null && query.getMonth() != null) {
            // 按年月查询
            wrapper.eq("year", query.getYear())
                    .eq("period_type", "month")
                    .eq("period_value", query.getMonth());
        } else {
            // 未传任何时间参数：获取每个区域最新一期（按 calc_time 倒序，取一条）
            // 这里使用子查询或 group by 取最新，简单起见可以用两条 SQL，或者使用 MyBatis Plus 的 last 方法。
            // 为了效率，我们使用自定义 SQL 或者循环查询。这里采用分组取最大 calc_time 的方式。
            // 由于 MyBatis Plus 不支持直接 group by 后取最新，我们改为：先查出所有区域名称，再分别查最新记录（循环）。数据量不大时可以。
            // 另一种方式：使用 wrapper 按 region 分组并取最大 calc_time，需要写子查询。我们采用简单循环（假设区域不多）。
            List<String> regions = listObjs(new QueryWrapper<RegionIndex>()
                    .select("DISTINCT region")
                    .isNotNull("region"), obj -> (String) obj);

            return regions.stream().map(region -> {
                RegionIndex latest = lambdaQuery()
                        .eq(RegionIndex::getRegion, region)
                        .orderByDesc(RegionIndex::getCalcTime)
                        .last("LIMIT 1")
                        .one();
                if (latest == null) return null;
                RegionListItemVO vo = new RegionListItemVO();
                BeanUtils.copyProperties(latest, vo);
                return vo;
            }).filter(vo -> vo != null).collect(Collectors.toList());
        }

        // 如果有时间条件，直接查询所有符合条件的记录（可能多个区域）
        List<RegionIndex> list = list(wrapper);
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
                .orderByAsc("year", "period_type", "period_value");  // 按时间升序

        if (StringUtils.hasText(query.getStart())) {
            wrapper.ge("calc_time", query.getStart() + " 00:00:00");
        }
        if (StringUtils.hasText(query.getEnd())) {
            wrapper.le("calc_time", query.getEnd() + " 23:59:59");
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