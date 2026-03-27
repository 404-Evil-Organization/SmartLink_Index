package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "出海服务商视图对象")
public class AbroadServiceVO {

    @Schema(description = "服务商ID")
    private Long id;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "服务类型")
    private String serviceType;

    @Schema(description = "服务介绍")
    private String description;

    @Schema(description = "Logo URL")
    private String logo;

    @Schema(description = "官网")
    private String website;

    @Schema(description = "成立日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date establishedDate;

    @Schema(description = "员工人数")
    private Integer employeeCount;

    @Schema(description = "覆盖国家")
    private String countryCoverage;

    @Schema(description = "资质概述")
    private String qualification;
}