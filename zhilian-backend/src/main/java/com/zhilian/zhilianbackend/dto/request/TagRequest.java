package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:51
 * @Param:
 * @Return:
 * @Description: 标签分页查询请求参数，继承分页基础类
 **/
@Data
@Schema(description = "标签新增/修改请求参数")
public class TagRequest {

    @Schema(description = "标签名称", required = true, example = "CNAS认证")
    private String name;

    @Schema(description = "标签类别", example = "certification")
    private String category;

    @Schema(description = "标签说明", example = "中国合格评定国家认可委员会认证")
    private String description;
}
