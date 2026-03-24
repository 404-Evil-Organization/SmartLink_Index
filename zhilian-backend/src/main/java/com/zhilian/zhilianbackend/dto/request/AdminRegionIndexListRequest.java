package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "管理员获取区域指数列表请求参数")
public class AdminRegionIndexListRequest {

    @Schema(description = "页码", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    @Max(value = 1000, message = "页码最大为1000")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer size = 10;

    @Schema(description = "区域筛选", example = "深圳")
    private String region;

    @Schema(description = "年份筛选", example = "2026")
    @Min(value = 1900, message = "年份最小为1900")
    @Max(value = 2200, message = "年份最大为2200")
    private Integer year;
}