package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:51
 * @Param:
 * @Return:
 * @Description: 标签分页查询请求参数，用于接收前端传递的标签查询条件
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "标签分页查询参数")
public class TagQueryRequest extends PageRequest {

    @Schema(description = "标签名称（模糊匹配）")
    private String name;

    @Schema(description = "类别筛选")
    private String category;
}
