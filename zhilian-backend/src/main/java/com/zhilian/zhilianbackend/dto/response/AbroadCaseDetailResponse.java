package com.zhilian.zhilianbackend.dto.response;

import lombok.Data;

import java.util.Date;

/**
 * @Author: 6017
 * @Date: 2026/3/25 21:10
 * @Param: 
 * @Return: 
 * @Description: 出海案例详情返回VO
**/
@Data
public class AbroadCaseDetailResponse {

    private Long id;

    private String title;

    private String companyName;

    private String companyType;

    private String country;

    private String serviceType;

    private String description;

    private String coverImage;

    private Date publishTime;

    private Byte status;

    private Date createTime;

    private Date updateTime;
}