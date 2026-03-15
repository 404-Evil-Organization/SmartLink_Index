package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 6017
 * @Date: 2026/3/12 23:12
 * @Param:
 * @Return:
 * @Description: 服务标签响应对象，用于返回服务商的服务类型下拉选项
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务标签响应")
public class ServiceTagResponse {

    @Schema(description = "标签ID", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "检测认证")
    private String name;

    @Schema(description = "标签类别英文值", example = "service")
    private String category;

}
