package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "用户详情")
public class UserDetailVO {
    private Long id;
    private String username;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ManufactureInfo manufactureInfo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServiceProviderInfo serviceProviderInfo;

    @Data
    public static class ManufactureInfo {
        private Long id;
        private String companyName;
        private String region;
        private String scale;
    }

    @Data
    public static class ServiceProviderInfo {
        private Long id;
        private String companyName;
        private String region;
        private String serviceType;
    }
}