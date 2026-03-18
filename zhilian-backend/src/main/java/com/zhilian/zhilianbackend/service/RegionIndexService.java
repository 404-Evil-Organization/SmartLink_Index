package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.request.RegionDetailQuery;
import com.zhilian.zhilianbackend.dto.request.RegionListQuery;
import com.zhilian.zhilianbackend.dto.request.TrendQuery;
import com.zhilian.zhilianbackend.dto.response.RegionDetailVO;
import com.zhilian.zhilianbackend.dto.response.RegionListItemVO;
import com.zhilian.zhilianbackend.dto.response.TrendItemVO;
import com.zhilian.zhilianbackend.entity.RegionIndex;

import java.util.List;

public interface RegionIndexService extends IService<RegionIndex> {

    /**
     * 获取所有区域指标（列表）
     * @param query 查询参数
     * @return 区域指标列表
     */
    List<RegionListItemVO> getRegionList(RegionListQuery query);

    /**
     * 获取特定区域指数
     * @param region 区域名称
     * @param query 查询参数（时间过滤）
     * @return 区域详情
     */
    RegionDetailVO getRegionDetail(String region, RegionDetailQuery query);

    /**
     * 获取趋势数据
     * @param query 查询参数（区域、起止时间）
     * @return 趋势数据列表
     */
    List<TrendItemVO> getTrend(TrendQuery query);
}