package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: 6017
 * @Date: 2026/3/12 22:51
 * @Param:
 * @Return:
 * @Description: 标签信息响应对象，用于返回标签详情或列表数据
 **/
@Data
@Schema(description = "标签信息")
public class TagResponse {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签类别")
    private String category;

    @Schema(description = "标签说明")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
