package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "成功案例视图对象")
public class AbroadCaseVO {

    @Schema(description = "案例ID")
    private Long id;

    @Schema(description = "案例标题")
    private String title;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "企业类型（manufacture/service）")
    private String companyType;

    @Schema(description = "目标国家")
    private String country;

    @Schema(description = "服务类型")
    private String serviceType;

    @Schema(description = "案例详情")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "发布时间")
    private Date publishTime;
}