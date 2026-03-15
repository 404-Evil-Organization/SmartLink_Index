package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 22:30
 * @Param:
 * @Return:
 * @Description: 新增制造企业请求参数
 **/
@Getter
@Setter
@ToString
@Schema(description = "新增制造企业请求参数")
public class ManufactureAddRequestDTO {

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    @Schema(description = "关联的用户ID", required = true, example = "1001")
    private Long userId;

    @NotBlank(message = "企业全称不能为空")
    @Schema(description = "企业全称", required = true, example = "深圳电子科技")
    private String companyName;

    @Schema(description = "所在区域", example = "深圳")
    private String region;

    @Schema(description = "详细地址", example = "深圳市南山区科技园")
    private String address;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138001")
    private String contactPhone;

    @Schema(description = "规模：micro/small/medium/large", example = "medium")
    private String scale;

    @PositiveOrZero(message = "员工人数不能为负数")
    @Schema(description = "员工人数", example = "500")
    private Integer employeeCount;

    @PositiveOrZero(message = "年营收不能为负数")
    @Schema(description = "年营收（万元）", example = "8000.00")
    private BigDecimal annualRevenue;

    @Schema(description = "主营产品类型", example = "PCB")
    private String productType;

    @Schema(description = "企业简介", example = "专业PCB制造商")
    private String description;

    @Schema(description = "Logo图片URL", example = "https://example.com/logo.png")
    private String logo;

    @Schema(description = "成立日期", example = "2010-05-01")
    private Date establishedDate;
}