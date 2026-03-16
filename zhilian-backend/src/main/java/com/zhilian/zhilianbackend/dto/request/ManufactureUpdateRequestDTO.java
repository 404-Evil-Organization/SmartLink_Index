package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:00
 * @Param:
 * @Return:
 * @Description: 修改制造企业请求参数（所有字段都是可选的，只传需要修改的字段）
 **/
@Getter
@Setter
@ToString
@Schema(description = "修改制造企业请求参数")
public class ManufactureUpdateRequestDTO {

    @Schema(description = "企业全称", example = "深圳电子科技集团")
    private String companyName;

    @Schema(description = "所在区域", example = "深圳")
    private String region;

    @Schema(description = "详细地址", example = "深圳市南山区科技园南区")
    private String address;

    @Schema(description = "联系人", example = "张三丰")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138001")
    private String contactPhone;

    @Schema(description = "规模：micro/small/medium/large", example = "large")
    private String scale;

    @PositiveOrZero(message = "员工人数不能为负数")
    @Schema(description = "员工人数", example = "800")
    private Integer employeeCount;

    @PositiveOrZero(message = "年营收不能为负数")
    @Schema(description = "年营收（万元）", example = "12000.00")
    private BigDecimal annualRevenue;

    @Schema(description = "主营产品类型", example = "PCB、半导体")
    private String productType;

    @Schema(description = "企业简介", example = "专业PCB及半导体制造商")
    private String description;

    @Schema(description = "Logo图片URL", example = "https://example.com/logo/updated.png")
    private String logo;

    @Schema(description = "成立日期", example = "2010-05-01")
    private Date establishedDate;
}