package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;

/**
 * @Author: 6017
 * @Date: 2026/3/25 21:11
 * @Param: 
 * @Return: 
 * @Description: 出海案例列表查询请求DTO
**/
@Data
public class AbroadCaseQueryRequest extends PageRequest {

    /**
     * 目标国家筛选
     */
    private String country;

    /**
     * 状态：0草稿 1发布
     */
    private Byte status;
}