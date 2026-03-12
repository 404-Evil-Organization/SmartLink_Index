package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:38
 * @Param:
 * @Return:
 * @Description: 用户登录请求参数DTO
 **/
@Data
public class UserLoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
