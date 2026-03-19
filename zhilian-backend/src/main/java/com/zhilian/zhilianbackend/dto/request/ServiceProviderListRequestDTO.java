package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:50
 * @Param:
 * @Return:
 * @Description: 服务商列表查询请求参数
 **/
@Getter
@Setter
@ToString
@Schema(description = "服务商列表查询请求参数")
public class ServiceProviderListRequestDTO {

    @Schema(description = "页码，默认1")
    private Integer page = 1;

    @Schema(description = "每页条数，默认10")
    private Integer size = 10;

    @Schema(description = "区域筛选（如“深圳”）")
    private String region;

    @Schema(description = "服务大类筛选（如“检测认证”）")
    private String serviceType;
}