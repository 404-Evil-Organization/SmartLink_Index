package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.validation.constraints.Max;
/**
 * @Author: xiaodengyou
 * @Date: 2026/3/12 20:17
 * @Param:
 * @Return:
 * @Description: 分页请求基础类，所有需要分页的请求都可以继承此类
 **/

@Data
@Schema(description = "分页请求基础类")
public class PageRequestDTO {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，默认1", example = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数，默认10，最大100", example = "10")
    private Integer size = 10;
}