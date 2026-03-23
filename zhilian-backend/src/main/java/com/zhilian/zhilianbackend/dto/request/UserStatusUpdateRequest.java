package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "修改用户状态请求")
public class UserStatusUpdateRequest {
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为 0 或 1")
    @Max(value = 1, message = "状态值必须为 0 或 1")
    @Schema(description = "状态：0禁用 1正常")
    private Integer status;
}