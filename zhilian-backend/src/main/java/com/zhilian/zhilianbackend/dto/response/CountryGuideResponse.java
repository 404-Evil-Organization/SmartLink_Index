package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "国家指南响应")
public class CountryGuideResponse {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "国家名称")
    private String country;

    @Schema(description = "准入要求")
    private String requirements;

    @Schema(description = "办理流程")
    private String process;

    @Schema(description = "所需材料")
    private String documents;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;
}