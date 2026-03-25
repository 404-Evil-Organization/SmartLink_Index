package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.entity.CountryGuide;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/25 18:04
 * @Param:
 * @Return:
 * @Description: 国家准入指南表业务逻辑接口，定义国家指南相关的业务方法
 **/
public interface CountryGuideService extends IService<CountryGuide> {

    /**
     * @Author: 6017
     * @Date: 2026/3/26 00:02
     * @Param: country 国家名称
     * @Return: CountryGuideResponse 国家准入指南响应对象
     * @Description: 根据国家名称获取准入指南，返回包含准入要求、办理流程和所需材料的响应对象
    **/
    CountryGuideResponse getByCountry(String country);
}