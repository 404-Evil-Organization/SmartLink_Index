package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改国家指南请求")
public class CountryGuideUpdateRequest {

    @Size(max = 50, message = "国家名称长度不能超过50个字符")
    @Schema(description = "国家名称")
    private String country;

    @Size(max = 500, message = "准入要求长度不能超过500个字符")
    @Schema(description = "准入要求")
    private String requirements;

    @Size(max = 500, message = "办理流程长度不能超过500个字符")
    @Schema(description = "办理流程")
    private String process;

    @Size(max = 2000, message = "所需材料长度不能超过2000个字符")
    @Schema(description = "所需材料")
    private String documents;
}