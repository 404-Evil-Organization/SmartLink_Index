package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.dto.response.TopDemandResponse;
import com.zhilian.zhilianbackend.entity.Demand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:48
 * @Param: 
 * @Return: 
 * @Description: 需求 Mapper 接口
**/
@Mapper
public interface DemandMapper extends BaseMapper<Demand> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:48
     * @Param: top 返回数量
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求统计（按标签统计）
    **/
    @Select("SELECT t.name AS serviceType, COUNT(dt.demand_id) AS count " +
            "FROM demand_tag dt " +
            "INNER JOIN tag t ON dt.tag_id = t.id " +
            "INNER JOIN demand d ON dt.demand_id = d.id " +
            "WHERE d.deleted = #{notDeletedTime} " +
            "AND d.audit_status = 'approved' " +
            "AND dt.deleted = #{notDeletedTime} " +
            "AND t.deleted = #{notDeletedTime} " +
            "GROUP BY t.id, t.name " +
            "ORDER BY count DESC " +
            "LIMIT #{top}")
    List<TopDemandResponse> getTopDemands(@Param("top") Integer top,
                                          @Param("notDeletedTime") String notDeletedTime);
}