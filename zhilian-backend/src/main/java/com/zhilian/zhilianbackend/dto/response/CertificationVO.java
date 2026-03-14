package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 证书响应VO
 **/
@Data
@Schema(description = "证书信息")
public class CertificationVO {

    @Schema(description = "证书ID", example = "3001")
    private Long id;

    @Schema(description = "服务商ID", example = "2001")
    private Long serviceId;

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

    @Schema(description = "证书文件URL", example = "https://...")
    private String certFileUrl;

    @Schema(description = "状态：0失效 1有效", example = "1")
    private Byte status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "上传时间", example = "2026-03-01 10:00:00")
    private Date createTime;
}