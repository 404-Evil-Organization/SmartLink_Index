package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "用户列表项")
public class UserListVO {
    private Long id;
    private String username;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}