package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.DemandMarketVO;
import com.zhilian.zhilianbackend.dto.response.DemandPendingVO;
import com.zhilian.zhilianbackend.dto.response.TopDemandResponse;
import com.zhilian.zhilianbackend.entity.Demand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:48
 * @Description: 需求 Mapper 接口
 **/
@Mapper
public interface DemandMapper extends BaseMapper<Demand> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:48
     * @Param: top 返回数量
     * @Param: notDeletedTime 逻辑删除时间标记（LocalDateTime 类型）
     * @Return: List<TopDemandResponse> 热门需求列表
     * @Description: 获取热门需求统计（按标签统计）
     */
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
                                          @Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: page 分页参数
     * @Param: keyword 标题关键词
     * @Param: tagIds 标签ID列表
     * @Param: budgetMin 最小预算
     * @Param: budgetMax 最大预算
     * @Param: deadlineStart 截止日期开始范围（LocalDate）
     * @Param: deadlineEnd 截止日期结束范围（LocalDate）
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: 分页的市场需求列表
     * @Description: 分页查询市场需求（已审核通过且已发布的需求）
     */
    IPage<DemandMarketVO> selectMarketDemands(Page<?> page,
                                              @Param("keyword") String keyword,
                                              @Param("tagIds") List<Long> tagIds,
                                              @Param("budgetMin") BigDecimal budgetMin,
                                              @Param("budgetMax") BigDecimal budgetMax,
                                              @Param("deadlineStart") LocalDate deadlineStart,
                                              @Param("deadlineEnd") LocalDate deadlineEnd,
                                              @Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: id 需求ID
     * @Param: notDeletedTime 逻辑删除时间标记（仅锁定未逻辑删除的记录）
     * @Return: 需求实体（带行锁）
     * @Description: 使用行锁查询需求，用于防止并发接单；仅对未逻辑删除的需求加锁
     */
    @Select("SELECT * FROM demand WHERE id = #{id} AND deleted = #{notDeletedTime} FOR UPDATE")
    Demand selectForUpdateById(@Param("id") Long id,
                               @Param("notDeletedTime") LocalDateTime notDeletedTime);
     
     /*
     * @Date: 2026/03/23
     * @Param: page 分页对象
     * @Return: IPage<DemandPendingVO> 分页的待审核需求列表
     * @Description: 分页查询待审核需求（仅需求基本信息 + 企业名称，不含标签）
     */
    IPage<DemandPendingVO> selectPendingDemandPage(Page<DemandPendingVO> page,
                                                   @Param("notDeletedTime") LocalDateTime notDeletedTime);
}