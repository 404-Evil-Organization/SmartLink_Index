package com.zhilian.zhilianbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 证书更新请求DTO（可选文件URL）
 **/
@Data
@Schema(description = "证书更新请求参数")
public class CertificationUpdateRequest {

    @Schema(description = "证书名称", example = "CNAS认证")
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

    @Schema(description = "证书文件URL（如需更换文件，先通过/common/upload接口获取新URL）",
            example = "https://smartlink-index.oss-cn-guangzhou.aliyuncs.com/uploads/xxx.pdf")
    private String certFileUrl;

    @Schema(description = "状态：0失效 1有效", example = "1")
    private Byte status;
}