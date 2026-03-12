package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 00:36
 * @Param:
 * @Return:
 * @Description: 新增服务商返回对象
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增服务商返回对象")
public class ServiceProviderAddVO {

    @Schema(description = "新创建的服务商ID", example = "2010")
    private Long id;
}