package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.dto.response.HeatmapDataResponse;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:51
 * @Param: 
 * @Return: 
 * @Description: 合作记录表Mapper接口，提供合作记录相关的数据库操作
**/
@Mapper
public interface CooperationMapper extends BaseMapper<Cooperation> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: startDate 开始日期  endDate 结束日期
     * @Return: List<HeatmapDataResponse> 热力图数据列表
     * @Description: 统计各区域的合作次数（热力图数据）
    **/
    @Select("<script>" +
            "SELECT m.region AS region, COUNT(c.id) AS value " +
            "FROM cooperation c " +
            "INNER JOIN manufacture m ON c.manu_id = m.id " +
            "WHERE m.region IS NOT NULL AND m.region != '' " +
            "AND c.deleted = '1970-01-01 00:00:00' " +
            "<if test='startDate != null'>" +
            "AND c.create_time >= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "AND c.create_time &lt;= #{endDate} " +
            "</if>" +
            "GROUP BY m.region " +
            "ORDER BY value DESC" +
            "</script>")
    List<HeatmapDataResponse> getHeatmapData(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);
}
