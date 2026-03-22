package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.dto.response.HeatmapDataResponse;
import com.zhilian.zhilianbackend.entity.Cooperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:41
 * @Description: 合作记录 Mapper 接口
 */
@Mapper
public interface CooperationMapper extends BaseMapper<Cooperation> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: startDate 开始日期时间（LocalDateTime 类型，避免字符串转换）
     * @Param: endDate 结束日期时间（LocalDateTime 类型，避免字符串转换）
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: List<HeatmapDataResponse> 热力图数据列表
     * @Description: 统计各区域的合作次数（热力图数据）
     */
    @Select("<script>" +
            "SELECT m.region AS region, COUNT(c.id) AS value " +
            "FROM cooperation c " +
            "INNER JOIN manufacture m ON c.manu_id = m.id " +
            "WHERE m.region IS NOT NULL AND m.region != '' " +
            "AND c.deleted = #{notDeletedTime} " +
            "AND m.deleted = #{notDeletedTime} " +
            "AND m.audit_status = 'approved' " +
            "<if test='startDate != null'>" +
            "AND c.create_time >= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "AND c.create_time &lt;= #{endDate} " +
            "</if>" +
            "GROUP BY m.region " +
            "ORDER BY value DESC" +
            "</script>")
    List<HeatmapDataResponse> getHeatmapData(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: page 分页参数
     * @Param: companyId 企业ID（根据角色决定是 manu_id 或 service_id）
     * @Param: role 角色（manufacture/service）
     * @Param: userId 当前用户ID（用于计算 hasEvaluated）
     * @Param: status 状态筛选
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: 分页的合作记录视图对象
     * @Description: 分页查询当前用户的合作记录（非管理员）
     */
    IPage<CooperationRecordVO> selectMyCooperations(Page<?> page,
                                                    @Param("companyId") Long companyId,
                                                    @Param("role") String role,
                                                    @Param("userId") Long userId,
                                                    @Param("status") String status,
                                                    @Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: page 分页参数
     * @Param: companyId 企业ID（可选，若传入则匹配 manu_id 或 service_id）
     * @Param: status 状态筛选
     * @Param: userId 当前用户ID（用于计算 hasEvaluated）
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: 分页的合作记录视图对象
     * @Description: 分页查询合作记录（管理员专用，不限制角色）
     */
    IPage<CooperationRecordVO> selectMyCooperationsAdmin(Page<?> page,
                                                         @Param("companyId") Long companyId,
                                                         @Param("status") String status,
                                                         @Param("userId") Long userId,
                                                         @Param("notDeletedTime") LocalDateTime notDeletedTime);
}