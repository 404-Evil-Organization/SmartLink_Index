package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:35
 * @Description: 待审核企业响应对象，返回待审核企业的基本信息
 **/
@Data
@Schema(description = "待审核企业响应对象")
public class PendingEnterpriseResponse {

    @Schema(description = "企业ID")
    private Long id;

    @Schema(description = "企业类型：manufacture 制造企业 / service 服务商")
    private String type;

    @Schema(description = "企业全称")
    private String companyName;

    @Schema(description = "所在区域")
    private String region;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "审核状态：pending 待审核")
    private String auditStatus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}