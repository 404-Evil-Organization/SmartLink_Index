package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:52
 * @Param: 
 * @Return: 
 * @Description: 区域指数表Mapper接口，提供区域指数相关的数据库操作
**/
public interface RegionIndexMapper extends BaseMapper<RegionIndex> {
    /**
     * 查询指定年份和季度的所有区域指数
     */
    @Select("SELECT * FROM region_index WHERE year = #{year} " +
            "AND period_type = 'quarter' AND period_value = #{quarter} " +
            "AND deleted = '1970-01-01 00:00:00'")
    List<RegionIndex> selectByYearAndQuarter(@Param("year") Short year,
                                             @Param("quarter") Byte quarter);

    /**
     * 查询指定年份的所有区域指数
     */
    @Select("SELECT * FROM region_index WHERE year = #{year} " +
            "AND period_type = 'quarter' AND deleted = '1970-01-01 00:00:00'")
    List<RegionIndex> selectByYear(@Param("year") Short year);

    /**
     * 查询指定区域的最新指数
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND period_type = 'quarter' AND deleted = '1970-01-01 00:00:00' " +
            "ORDER BY year DESC, period_value DESC LIMIT 1")
    RegionIndex selectLatestByRegion(@Param("region") String region);

    /**
     * 查询指定区域的指定年份指数
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND year = #{year} AND period_type = 'quarter' " +
            "AND deleted = '1970-01-01 00:00:00'")
    List<RegionIndex> selectByRegionAndYear(@Param("region") String region,
                                            @Param("year") Short year);

    /**
     * 查询指定区域的时间范围趋势
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND period_type = 'quarter' AND deleted = '1970-01-01 00:00:00' " +
            "AND ((year = #{startYear} AND period_value >= #{startQuarter}) " +
            "OR (year > #{startYear} AND year < #{endYear}) " +
            "OR (year = #{endYear} AND period_value <= #{endQuarter})) " +
            "ORDER BY year ASC, period_value ASC")
    List<RegionIndex> selectTrend(@Param("region") String region,
                                  @Param("startYear") Short startYear,
                                  @Param("startQuarter") Byte startQuarter,
                                  @Param("endYear") Short endYear,
                                  @Param("endQuarter") Byte endQuarter);

    /**
     * 获取所有有数据的年份列表
     */
    @Select("SELECT DISTINCT year FROM region_index " +
            "WHERE period_type = 'quarter' AND deleted = '1970-01-01 00:00:00' " +
            "ORDER BY year DESC")
    List<Short> selectAvailableYears();

    /**
     * 删除指定年份和季度的数据（用于重新计算）
     * 仅对当前未删除记录（deleted = '1970-01-01 00:00:00'）进行逻辑删除，
     * 避免覆盖历史删除时间，确保 deleted 字段可作为审计时间使用。
     */
    @Update("UPDATE region_index SET deleted = NOW() WHERE year = #{year} " +
            "AND period_type = 'quarter' AND period_value = #{quarter} " +
            "AND deleted = '1970-01-01 00:00:00'")
    void deleteByYearAndQuarter(@Param("year") Short year, @Param("quarter") Byte quarter);
}
