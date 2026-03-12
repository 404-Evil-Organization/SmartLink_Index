package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;
import java.util.Date;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:40
 * @Param:
 * @Return:
 * @Description: 用户信息响应DTO
 **/
@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    private Date createTime;
}
