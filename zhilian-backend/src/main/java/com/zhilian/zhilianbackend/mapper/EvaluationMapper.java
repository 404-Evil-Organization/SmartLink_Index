package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    /**
     * 分页获取服务商的评价列表
     * @param page 分页参数
     * @param serviceId 服务商ID
     * @param notDeletedTime 未删除标志时间
     * @return 评价分页数据
     */
    @Select("SELECT e.* FROM evaluation e " +
            "JOIN cooperation c ON e.coop_id = c.id " +
            "WHERE c.service_id = #{serviceId} " +
            "AND e.deleted = #{notDeletedTime} " +
            "AND c.deleted = #{notDeletedTime} " +
            "ORDER BY e.create_time DESC")
    Page<Evaluation> selectEvaluationPageByServiceId(Page<Evaluation> page, 
                                                     @Param("serviceId") Long serviceId, 
                                                     @Param("notDeletedTime") Date notDeletedTime);

    /**
     * 获取服务商的平均评分
     * 仅统计：未删除的合作记录 + 未删除的评价记录 + 制造企业对服务商的评价
     *
     * @param serviceId      服务商ID
     * @param notDeletedTime 未删除标志时间（Date 类型），通常为系统约定的“未删除时间”
     * @return 平均评分
     */
    @Select("SELECT AVG(e.score) " +
            "FROM evaluation e " +
            "JOIN cooperation c ON e.coop_id = c.id " +
            "WHERE c.service_id = #{serviceId} " +
            "AND e.deleted = #{notDeletedTime} " +
            "AND c.deleted = #{notDeletedTime} " +
            "AND e.evaluator_role = 'manufacture'")
    Double getAvgScoreByServiceId(@Param("serviceId") Long serviceId,
                                  @Param("notDeletedTime") Date notDeletedTime);

    /**
     * 获取服务商的评价数量
     * 仅统计：未删除的合作记录 + 未删除的评价记录 + 制造企业对服务商的评价
     *
     * @param serviceId      服务商ID
     * @param notDeletedTime 未删除标志时间（Date 类型），通常为系统约定的“未删除时间”
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) " +
            "FROM evaluation e " +
            "JOIN cooperation c ON e.coop_id = c.id " +
            "WHERE c.service_id = #{serviceId} " +
            "AND e.deleted = #{notDeletedTime} " +
            "AND c.deleted = #{notDeletedTime} " +
            "AND e.evaluator_role = 'manufacture'")
    Integer getCountByServiceId(@Param("serviceId") Long serviceId,
                                @Param("notDeletedTime") Date notDeletedTime);
}