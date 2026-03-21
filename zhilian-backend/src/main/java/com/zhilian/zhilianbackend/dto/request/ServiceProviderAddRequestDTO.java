package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 00:35
 * @Param:
 * @Return:
 * @Description: 新增服务商请求参数
 **/
@Getter
@Setter
@ToString
@Schema(description = "新增服务商请求参数")
public class ServiceProviderAddRequestDTO {

    @Schema(description = "关联的用户ID（由服务端根据当前登录用户自动填充，客户端可不传）", example = "2001")
    private Long userId;

    @NotBlank(message = "企业全称不能为空")
    @Schema(description = "企业全称", required = true, example = "华测检测认证集团")
    private String companyName;

    @Schema(description = "所在区域", example = "深圳")
    private String region;

    @Schema(description = "详细地址", example = "深圳市南山区科技园")
    private String address;

    @Schema(description = "联系人", example = "王五")
    private String contactPerson;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确，应为11位手机号")
    @Schema(description = "联系电话", example = "13700137003")
    private String contactPhone;

    @Schema(description = "服务大类（可多选，用逗号分隔）", example = "检测认证")
    private String serviceType;

    @Schema(description = "服务介绍", example = "CNAS认可实验室，提供国际认证服务")
    private String description;

    @Schema(description = "Logo图片URL", example = "https://example.com/logo/cti.png")
    private String logo;

    @Schema(description = "企业官网", example = "www.cti.com")
    private String website;

    @Schema(description = "成立日期", example = "2003-01-01")
    private Date establishedDate;

    @PositiveOrZero(message = "员工人数不能为负数")
    @Schema(description = "员工人数", example = "2000")
    private Integer employeeCount;

    @Schema(description = "资质概述", example = "CNAS、CMA")
    private String qualification;
}