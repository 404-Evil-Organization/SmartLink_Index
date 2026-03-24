package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/17 8:28
 * @Description: 资质证书更新请求参数
 */
@Data
@Schema(description = "资质证书更新请求参数")
public class CertificationUpdateRequest {

    @Schema(description = "证书名称", example = "CNAS认证")
    private String certName;

    @Schema(description = "证书编号", example = "CNAS L1234")
    private String certNo;

    @Schema(description = "发证机构", example = "中国合格评定国家认可委员会")
    private String issueAuthority;

    @Schema(description = "发证日期(yyyy-MM-dd)", example = "2023-01-01")
    private Date issueDate;

    @Schema(description = "有效期至(yyyy-MM-dd)", example = "2026-12-31")
    private Date expireDate;

    @Schema(description = "状态：0失效 1有效", example = "1")
    private Byte status;
}