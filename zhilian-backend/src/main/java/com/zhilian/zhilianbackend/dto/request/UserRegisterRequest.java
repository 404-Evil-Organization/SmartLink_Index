package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:37
 * @Param:
 * @Return:
 * @Description: 用户注册请求参数DTO
 **/
@Data
public class UserRegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;  // manufacture, service, park, admin

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    private String email;
}
