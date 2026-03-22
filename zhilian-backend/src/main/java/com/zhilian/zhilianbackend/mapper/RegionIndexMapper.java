package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.text.SimpleDateFormat;
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
     * 未删除标记的字符串表示（与 DateConstants.getNotDeletedTime() 保持一致）
     * 使用 SimpleDateFormat 格式化，确保格式为 'yyyy-MM-dd HH:mm:ss'
     */
    String UNDELETED_DATE_STR = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(DateConstants.getNotDeletedTime());

    /**
     * 查询指定年份和季度的所有区域指数
     */
    @Select("SELECT * FROM region_index WHERE year = #{year} " +
            "AND period_type = 'quarter' AND period_value = #{quarter} " +
            "AND deleted = '${UNDELETED_DATE_STR}'")
    List<RegionIndex> selectByYearAndQuarter(@Param("year") Short year,
                                             @Param("quarter") Byte quarter);

    /**
     * 查询指定年份的所有区域指数
     */
    @Select("SELECT * FROM region_index WHERE year = #{year} " +
            "AND period_type = 'quarter' AND deleted = '${UNDELETED_DATE_STR}'")
    List<RegionIndex> selectByYear(@Param("year") Short year);

    /**
     * 查询指定区域的最新指数
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND period_type = 'quarter' AND deleted = '${UNDELETED_DATE_STR}' " +
            "ORDER BY year DESC, period_value DESC LIMIT 1")
    RegionIndex selectLatestByRegion(@Param("region") String region);

    /**
     * 查询指定区域的指定年份指数
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND year = #{year} AND period_type = 'quarter' " +
            "AND deleted = '${UNDELETED_DATE_STR}'")
    List<RegionIndex> selectByRegionAndYear(@Param("region") String region,
                                            @Param("year") Short year);

    /**
     * 查询指定区域的时间范围趋势
     * 使用“起点不早于 start（年+季度）、终点不晚于 end（年+季度）”的组合条件，
     * 兼容同一年与跨年场景，避免 startYear == endYear 时误查所有季度。
     */
    @Select("SELECT * FROM region_index WHERE region = #{region} " +
            "AND period_type = 'quarter' AND deleted = '${UNDELETED_DATE_STR}' " +
            "AND ((year > #{startYear} OR (year = #{startYear} AND period_value >= #{startQuarter})) " +
            "AND (year < #{endYear} OR (year = #{endYear} AND period_value <= #{endQuarter}))) " +
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
            "WHERE period_type = 'quarter' AND deleted = '${UNDELETED_DATE_STR}' " +
            "ORDER BY year DESC")
    List<Short> selectAvailableYears();

    /**
     * 删除指定年份和季度的数据（用于重新计算）
     * 仅对当前未删除记录（deleted = UNDELETED_DATE_STR）进行逻辑删除，
     * 避免覆盖历史删除时间，确保 deleted 字段可作为审计时间使用。
     */
    @Update("UPDATE region_index SET deleted = NOW() WHERE year = #{year} " +
            "AND period_type = 'quarter' AND period_value = #{quarter} " +
            "AND deleted = '${UNDELETED_DATE_STR}'")
    void deleteByYearAndQuarter(@Param("year") Short year, @Param("quarter") Byte quarter);
}