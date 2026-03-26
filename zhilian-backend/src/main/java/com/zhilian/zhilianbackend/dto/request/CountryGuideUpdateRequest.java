package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改国家指南请求")
public class CountryGuideUpdateRequest {

    @Schema(description = "国家名称")
    private String country;

    @Schema(description = "准入要求")
    private String requirements;

    @Schema(description = "办理流程")
    private String process;

    @Schema(description = "所需材料")
    private String documents;
}