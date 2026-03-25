package com.zhilian.zhilianbackend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: 6017
 * @Date: 2026/3/25 21:11
 * @Param: 
 * @Return: 
 * @Description: 出海案例更新请求DTO
**/
@Data
public class AbroadCaseUpdateRequest {

    /**
     * 案例标题
     */
    private String title;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 企业类型：manufacture/service
     */
    private String companyType;

    /**
     * 目标国家
     */
    private String country;

    /**
     * 涉及服务类型
     */
    private String serviceType;

    /**
     * 案例详情
     */
    private String description;

    /**
     * 封面图片文件（更新时可选）
     */
    private MultipartFile coverImageFile;

    /**
     * 状态：0草稿 1发布
     */
    private Byte status;
}