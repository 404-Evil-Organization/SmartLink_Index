package com.zhilian.zhilianbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 证书上传请求DTO（先只保存到数据库，不包含文件）
 **/
@Data
@Schema(description = "证书上传请求参数")
public class CertificationUploadRequest {

    @NotBlank(message = "证书名称不能为空")
    @Schema(description = "证书名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNAS认证")
    private String certName;

    @Schema(description = "证书编号", example = "CNAS L1234")
    private String certNo;

    @Schema(description = "发证机构", example = "中国合格评定国家认可委员会")
    private String issueAuthority;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "发证日期", example = "2023-01-01")
    private Date issueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "有效期至", example = "2026-12-31")
    private Date expireDate;

    /**
     * 注意：先不包含file字段，只保存到数据库
     * 待接口测试无误后再添加文件上传功能
     */
}