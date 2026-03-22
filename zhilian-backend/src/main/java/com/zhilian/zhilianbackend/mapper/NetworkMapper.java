package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.dto.response.NetworkDataResponse;
import com.zhilian.zhilianbackend.entity.Cooperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:41
 * @Description: 网络关系数据 Mapper 接口
 */
@Mapper
public interface NetworkMapper extends BaseMapper<Cooperation> {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: List<NetworkDataResponse.NodeDTO> 制造企业节点列表
     * @Description: 获取制造企业节点列表（只返回有有效合作关系的企业）
     */
    @Select("SELECT CONCAT('m', m.id) AS id, m.company_name AS name, 'manufacture' AS type " +
            "FROM manufacture m " +
            "WHERE m.deleted = #{notDeletedTime} " +
            "AND m.audit_status = 'approved' " +
            "AND EXISTS (" +
            "   SELECT 1 FROM cooperation c " +
            "   INNER JOIN service_provider s ON c.service_id = s.id " +
            "   WHERE c.manu_id = m.id " +
            "   AND c.deleted = #{notDeletedTime} " +
            "   AND s.deleted = #{notDeletedTime} " +
            "   AND s.audit_status = 'approved'" +
            ")")
    List<NetworkDataResponse.NodeDTO> getManufactureNodes(@Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Return: List<NetworkDataResponse.NodeDTO> 服务商节点列表
     * @Description: 获取服务商节点列表（只返回有有效合作关系的服务商）
     */
    @Select("SELECT CONCAT('s', s.id) AS id, s.company_name AS name, 'service' AS type " +
            "FROM service_provider s " +
            "WHERE s.deleted = #{notDeletedTime} " +
            "AND s.audit_status = 'approved' " +
            "AND EXISTS (" +
            "   SELECT 1 FROM cooperation c " +
            "   INNER JOIN manufacture m ON c.manu_id = m.id " +
            "   WHERE c.service_id = s.id " +
            "   AND c.deleted = #{notDeletedTime} " +
            "   AND m.deleted = #{notDeletedTime} " +
            "   AND m.audit_status = 'approved'" +
            ")")
    List<NetworkDataResponse.NodeDTO> getServiceNodes(@Param("notDeletedTime") LocalDateTime notDeletedTime);

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:41
     * @Param: notDeletedTime 逻辑删除时间标记
     * @Param: limit 最大返回边数
     * @Return: List<Map<String, Object>> 连接关系列表
     * @Description: 获取合作关系连接列表（只返回双方都有效的合作记录，按合作次数倒序并限制返回数量）
     */
    @Select("SELECT " +
            "CONCAT('m', c.manu_id) AS source, " +
            "CONCAT('s', c.service_id) AS target, " +
            "COUNT(*) AS value " +
            "FROM cooperation c " +
            "INNER JOIN manufacture m ON c.manu_id = m.id " +
            "INNER JOIN service_provider s ON c.service_id = s.id " +
            "WHERE c.deleted = #{notDeletedTime} " +
            "AND m.deleted = #{notDeletedTime} " +
            "AND m.audit_status = 'approved' " +
            "AND s.deleted = #{notDeletedTime} " +
            "AND s.audit_status = 'approved' " +
            "GROUP BY c.manu_id, c.service_id " +
            "ORDER BY value DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getCooperationLinks(@Param("notDeletedTime") LocalDateTime notDeletedTime, @Param("limit") int limit);
}