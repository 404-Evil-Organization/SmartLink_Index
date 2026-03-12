package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:39
 * @Param:
 * @Return:
 * @Description: 用户修改密码参数DTO
 **/
@Data
public class UserChangePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
