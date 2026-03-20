package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.dto.response.NetworkDataResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:21
 * @Param:
 * @Return:
 * @Description: 网络关系数据 Mapper 接口
 **/
@Mapper
public interface NetworkMapper {

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:49
     * @Param:
     * @Return: List<NetworkDataResponse.NodeDTO> 制造企业节点列表
     * @Description: 获取制造企业节点列表
    **/
    @Select("SELECT CONCAT('m', m.id) AS id, m.company_name AS name, 'manufacture' AS type " +
            "FROM manufacture m " +
            "WHERE m.deleted = '1970-01-01 00:00:00' " +
            "AND m.audit_status = 'approved' " +
            "AND EXISTS (SELECT 1 FROM cooperation c WHERE c.manu_id = m.id AND c.deleted = '1970-01-01 00:00:00')")
    List<NetworkDataResponse.NodeDTO> getManufactureNodes();

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:49
     * @Param: 
     * @Return: List<NetworkDataResponse.NodeDTO> 服务商节点列表
     * @Description: 获取服务商节点列表
    **/
    @Select("SELECT CONCAT('s', s.id) AS id, s.company_name AS name, 'service' AS type " +
            "FROM service_provider s " +
            "WHERE s.deleted = '1970-01-01 00:00:00' " +
            "AND s.audit_status = 'approved' " +
            "AND EXISTS (SELECT 1 FROM cooperation c WHERE c.service_id = s.id AND c.deleted = '1970-01-01 00:00:00')")
    List<NetworkDataResponse.NodeDTO> getServiceNodes();

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:50
     * @Param: 
     * @Return: List<NetworkDataResponse.NodeDTO> 服务商节点列表
     * @Description: 获取服务商节点列表
    **/
    @Select("SELECT " +
            "CONCAT('m', c.manu_id) AS source, " +
            "CONCAT('s', c.service_id) AS target, " +
            "COUNT(*) AS value " +
            "FROM cooperation c " +
            "WHERE c.deleted = '1970-01-01 00:00:00' " +
            "GROUP BY c.manu_id, c.service_id " +
            "ORDER BY value DESC")
    List<Map<String, Object>> getCooperationLinks();
}
