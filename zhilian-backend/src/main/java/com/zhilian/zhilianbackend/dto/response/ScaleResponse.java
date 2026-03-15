package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 6017
 * @Date: 2026/3/12 22:37
 * @Param:
 * @Return:
 * @Description: 规模枚举响应对象，用于返回企业规模下拉选项
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规模枚举响应")
public class ScaleResponse {

    @Schema(description = "枚举值", example = "micro")
    private String value;

    @Schema(description = "显示名称", example = "微型企业")
    private String label;
}
