package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/26 00:01
 * @Param: 
 * @Return: 
 * @Description: 国家准入指南响应DTO
**/
@Data
@Schema(description = "国家准入指南响应")
public class CountryGuideResponse {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "国家名称")
    private String country;

    @Schema(description = "准入要求")
    private String requirements;

    @Schema(description = "办理流程")
    private String process;

    @Schema(description = "所需材料列表")
    private List<String> documents;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}