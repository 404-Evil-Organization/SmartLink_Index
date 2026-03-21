package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/20 21:46
 * @Param:
 * @Return:
 * @Description: 网络关系数据响应DTO
**/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkDataResponse {
    /**
     * 节点列表
     */
    private List<NodeDTO> nodes;

    /**
     * 连接列表
     */
    private List<LinkDTO> links;

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:46
     * @Param:
     * @Return:
     * @Description: 网络节点DTO
    **/
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeDTO {
        /**
         * 节点ID
         */
        private String id;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点类型：manufacture/service
         */
        private String type;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/20 21:47
     * @Param:
     * @Return:
     * @Description: 网络连接DTO
    **/
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkDTO {
        /**
         * 源节点ID
         */
        private String source;

        /**
         * 目标节点ID
         */
        private String target;

        /**
         * 合作次数/权重（使用 Long 对齐数据库 COUNT(*) 的 BIGINT 类型，避免大数量级溢出）
         */
        private Long value;
    }
}