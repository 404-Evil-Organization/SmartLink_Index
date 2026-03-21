package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.entity.Evaluation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:51
 * @Param: 
 * @Return: 
 * @Description: 评价表Mapper接口，提供评价相关的数据库操作
**/
public interface EvaluationMapper extends BaseMapper<Evaluation> {
    /**
     * 获取服务商的平均评分
     * 仅统计：未被逻辑删除的合作记录 + 未被逻辑删除的评价记录
     *
     * @param serviceId 服务商ID
     * @return 平均评分
     */
    @Select("SELECT AVG(score) FROM evaluation " +
            "WHERE coop_id IN (" +
            "  SELECT id FROM cooperation " +
            "  WHERE service_id = #{serviceId} " +
            "    AND cooperation.deleted = '1970-01-01 00:00:00'" +
            ") " +
            "AND evaluation.deleted = '1970-01-01 00:00:00'")
    Double getAvgScoreByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 获取服务商的评价数量
     * 仅统计：未被逻辑删除的合作记录 + 未被逻辑删除的评价记录
     *
     * @param serviceId 服务商ID
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM evaluation " +
            "WHERE coop_id IN (" +
            "  SELECT id FROM cooperation " +
            "  WHERE service_id = #{serviceId} " +
            "    AND cooperation.deleted = '1970-01-01 00:00:00'" +
            ") " +
            "AND evaluation.deleted = '1970-01-01 00:00:00'")
    Integer getCountByServiceId(@Param("serviceId") Long serviceId);
}
