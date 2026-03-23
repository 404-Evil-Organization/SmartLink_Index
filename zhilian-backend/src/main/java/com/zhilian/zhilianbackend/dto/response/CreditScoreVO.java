package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 15:01
 * @Param:
 * @Return:
 * @Description: 服务商信用分响应对象
**/
@Data
@Schema(description = "服务商信用分响应")
public class CreditScoreVO {

    @Schema(description = "服务商ID")
    private Long serviceId;

    @Schema(description = "综合信用分")
    private Byte score;

    @Schema(description = "资质分")
    private Byte qualScore;

    @Schema(description = "案例分")
    private Byte caseScore;

    @Schema(description = "评价分")
    private Byte evalScore;

    @Schema(description = "计算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date calTime;  // 与实体 calcTime 对应，返回时使用 calTime 字段名
}