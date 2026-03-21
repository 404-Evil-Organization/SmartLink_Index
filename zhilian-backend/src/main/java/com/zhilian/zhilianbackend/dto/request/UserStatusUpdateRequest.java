package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "修改用户状态请求")
public class UserStatusUpdateRequest {
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0禁用 1正常")
    private Integer status;
}