package com.zhilian.zhilianbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 22:45
 * @Description: 操作日志响应对象（VO），用于管理员查询接口返回
 */
@Getter
@Setter
@Schema(description = "操作日志响应对象")
public class OperLogVO {

    @Schema(description = "日志唯一标识")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "请求参数（JSON格式）")
    private String params;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "客户端IP地址")
    private String ip;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}