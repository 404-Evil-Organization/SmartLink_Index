package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 证书查询请求DTO
 **/
@Data
@Schema(description = "证书查询请求参数")
public class CertificationQueryRequest {

    @Schema(description = "服务商ID", example = "2001")
    private Long serviceId;

    @Schema(description = "页码", defaultValue = "1", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", defaultValue = "10", example = "10")
    private Integer size = 10;
}