package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    /**
     * 获取服务商的平均评分
     * @param serviceId 服务商ID
     * @param notDeletedTime 未删除标志时间
     * @return 平均评分
     */
    @Select("SELECT AVG(score) FROM evaluation WHERE coop_id IN " +
            "(SELECT id FROM cooperation WHERE service_id = #{serviceId}) " +
            "AND deleted = #{notDeletedTime}")
    Double getAvgScoreByServiceId(@Param("serviceId") Long serviceId,
                                  @Param("notDeletedTime") String notDeletedTime);

    /**
     * 获取服务商的评价数量
     * @param serviceId 服务商ID
     * @param notDeletedTime 未删除标志时间
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM evaluation WHERE coop_id IN " +
            "(SELECT id FROM cooperation WHERE service_id = #{serviceId}) " +
            "AND deleted = #{notDeletedTime}")
    Integer getCountByServiceId(@Param("serviceId") Long serviceId,
                                @Param("notDeletedTime") String notDeletedTime);
}