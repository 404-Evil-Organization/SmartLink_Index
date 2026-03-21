package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
@Schema(description = "用户列表查询参数")
public class UserListRequest {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码，从1开始", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "角色筛选：manufacture/service/park/admin")
    private String role;

    @Schema(description = "状态筛选：0禁用 1正常")
    private Integer status;

    @Schema(description = "用户名/手机号模糊搜索")
    private String keyword;
}