package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 20:17
 * @Description: 制造企业列表查询请求参数
 **/
@Getter
@Setter
@ToString
@Schema(description = "制造企业列表查询请求参数")
public class ManufactureListRequestDTO extends PageRequestDTO {

    @Schema(description = "区域筛选（如“深圳”）")
    private String region;

    @Schema(description = "规模筛选：micro/small/medium/large")
    private String scale;

    @Schema(description = "主营产品类型（模糊匹配）")
    private String productType;
    
}