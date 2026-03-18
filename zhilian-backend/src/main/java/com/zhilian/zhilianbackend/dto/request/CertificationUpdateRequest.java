package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 证书更新请求DTO（可选文件）
 */
@Data
@Schema(description = "证书更新请求参数（可选文件）")
public class CertificationUpdateRequest {

    @Schema(description = "证书名称", example = "CNAS认证")
    private String certName;

    @Schema(description = "证书编号", example = "CNAS L1234")
    private String certNo;

    @Schema(description = "发证机构", example = "中国合格评定国家认可委员会")
    private String issueAuthority;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "发证日期", example = "2023-01-01")
    private Date issueDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "有效期至", example = "2026-12-31")
    private Date expireDate;

    @Schema(description = "证书文件（不传则不替换文件）")
    private MultipartFile file;

    @Schema(description = "状态：0失效 1有效", example = "1")
    private Byte status;
}