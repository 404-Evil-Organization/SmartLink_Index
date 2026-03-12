package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:39
 * @Param:
 * @Return:
 * @Description: 用户注册响应DTO
 **/
@Data
public class UserRegisterResponse {
    private Long userId;
    private String username;
    private String role;
}
