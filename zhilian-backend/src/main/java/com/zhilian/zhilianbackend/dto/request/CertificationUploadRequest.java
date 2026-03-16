package com.zhilian.zhilianbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 证书上传请求DTO（包含文件）
 */
@Data
@Schema(description = "证书上传请求参数（含文件）")
public class CertificationUploadRequest {

    @NotBlank(message = "证书名称不能为空")
    @Schema(description = "证书名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNAS认证")
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

    @NotNull(message = "证书文件不能为空")
    @Schema(description = "证书文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;
}