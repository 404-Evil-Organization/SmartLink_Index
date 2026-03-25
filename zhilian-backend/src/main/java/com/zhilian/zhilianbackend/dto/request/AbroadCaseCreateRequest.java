package com.zhilian.zhilianbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: 6017
 * @Date: 2026/3/25 21:11
 * @Param: 
 * @Return: 
 * @Description: 出海案例创建请求DTO
**/
@Data
public class AbroadCaseCreateRequest {

    @NotBlank(message = "案例标题不能为空")
    private String title;

    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    @NotBlank(message = "企业类型不能为空")
    private String companyType;

    @NotBlank(message = "目标国家不能为空")
    private String country;

    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    @NotBlank(message = "案例详情不能为空")
    private String description;

    /**
     * 封面图片文件（上传时使用）
     */
    private MultipartFile coverImageFile;

    /**
     * 状态：0草稿 1发布，默认1
     */
    private Byte status = 1;
}