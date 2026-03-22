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

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:25
 * @Param:
 * @Return:
 * @Description: 区域指数表业务逻辑接口，定义区域指数相关的业务方法
**/
public interface RegionIndexService extends IService<RegionIndex> {


    // ============== 计算和定时任务方法 ==============

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:07
     * @Param: year 年份  quarter 季度
     * @Return:
     * @Description: 计算并保存季度区域指数
     **/
    void calculateAndSaveQuarterIndex(Short year, Byte quarter);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:08
     * @Param: year 年份  quarter 季度
     * @Return: 
     * @Description: 手动触发计算（用于测试）
     **/
    void manualCalculate(Short year, Byte quarter);
}
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
