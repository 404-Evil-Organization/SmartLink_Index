package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(1) // 页码最小为 1，防止出现 0 或负数导致分页插件行为异常
    private Integer page = 1;

    @Schema(description = "每页条数", defaultValue = "10", example = "10")
    @Min(1)  // 每页条数至少为 1
    @Max(100) // 单页最大条数限制为 100，防止一次查询数据量过大导致性能/内存风险
    private Integer size = 10;
}