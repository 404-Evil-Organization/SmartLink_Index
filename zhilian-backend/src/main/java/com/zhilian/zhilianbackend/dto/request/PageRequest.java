package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:36
 * @Param:
 * @Return:
 * @Description: 分页查询基础参数，所有分页查询请求都继承此类
 **/
@Data
@Schema(description = "分页查询基础参数")
public class PageRequest {

    @Schema(description = "页码，默认1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页条数，默认10")
    @Min(value = 1, message = "每页条数最少为1")
    @Max(value = 100, message = "每页条数最多不超过100")
    private Integer size = 10;
}
