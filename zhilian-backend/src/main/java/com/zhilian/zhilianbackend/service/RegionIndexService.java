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
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: query 查询参数
     * @Return: 区域指标列表
     * @Description: 获取所有区域指标（列表）
     **/
    List<RegionListItemVO> getRegionList(RegionListQuery query);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: region 区域名称
     * @Param: query 查询参数（时间过滤）
     * @Return: 区域详情
     * @Description: 获取特定区域指数
     **/
    RegionDetailVO getRegionDetail(String region, RegionDetailQuery query);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/9 21:32
     * @Param: query 查询参数（区域、起止时间）
     * @Return: 趋势数据列表
     * @Description: 获取趋势数据
     **/
    List<TrendItemVO> getTrend(TrendQuery query);
}