package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:35
 * @Param: 
 * @Return: 
 * @Description: 待审核企业列表查询请求参数，支持分页查询
**/
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "待审核企业列表查询请求参数")
public class EnterprisePendingRequest extends PageRequest {
    // 继承 PageRequest 中的 page 和 size 字段
}