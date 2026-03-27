package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "成功案例查询请求")
public class AbroadCaseQueryRequest {

    @Schema(description = "目标国家", example = "欧盟")
    private String country;

    @Schema(description = "服务类型", example = "CE认证")
    private String serviceType;

    @Schema(description = "状态", example = "1")
    private Byte status;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer size = 10;
}