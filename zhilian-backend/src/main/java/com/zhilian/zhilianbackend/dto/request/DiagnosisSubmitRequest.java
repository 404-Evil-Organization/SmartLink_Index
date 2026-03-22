package com.zhilian.zhilianbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 6017
 * @Date: 2026/3/17 21:06
 * @Param:
 * @Return:
 * @Description: 诊断问卷提交请求DTO，包含企业ID和各维度得分
 **/
@Data
public class DiagnosisSubmitRequest {
    @NotNull(message = "制造企业ID不能为空")
    @Min(value = 1, message = "制造企业ID必须为正数")
    private Long manuId;

    @NotNull(message = "信息化得分不能为空")
    @Min(value = 1, message = "信息化得分必须在1-5之间")
    @Max(value = 5, message = "信息化得分必须在1-5之间")
    private Integer infoScore;

    @NotNull(message = "自动化得分不能为空")
    @Min(value = 1, message = "自动化得分必须在1-5之间")
    @Max(value = 5, message = "自动化得分必须在1-5之间")
    private Integer autoScore;

    @NotNull(message = "数据应用得分不能为空")
    @Min(value = 1, message = "数据应用得分必须在1-5之间")
    @Max(value = 5, message = "数据应用得分必须在1-5之间")
    private Integer dataScore;

    @NotNull(message = "服务协同得分不能为空")
    @Min(value = 1, message = "服务协同得分必须在1-5之间")
    @Max(value = 5, message = "服务协同得分必须在1-5之间")
    private Integer serviceScore;
}
