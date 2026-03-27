package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.request.CountryGuideCreateRequest;
import com.zhilian.zhilianbackend.dto.request.CountryGuideUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import com.zhilian.zhilianbackend.common.result.PageResult;

public interface CountryGuideService extends IService<CountryGuide> {

    /**
     * @Author: 6017
     * @Date: 2026/3/26 00:02
     * @Param: country 国家名称
     * @Return: CountryGuideResponse 国家准入指南响应对象
     * @Description: 根据国家名称获取准入指南，返回包含准入要求、办理流程和所需材料的响应对象
    **/
    CountryGuideResponse getByCountry(String country);
    
    /*
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: countryKeyword 国家名称（模糊匹配）
     * @Return: PageResult<CountryGuideResponse> 分页结果
     * @Description: 分页查询国家指南列表
     **/
    PageResult<CountryGuideResponse> listByPage(Integer page, Integer size, String countryKeyword);

    /**
     * @Author: taciturn-hg
     * @Date: 2026/3/27 18:45
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: countryKeyword 国家名称关键词（模糊匹配）
     * @Return: PageResult<CountryGuideResponse> 分页结果
     * @Description: 公开接口-分页查询国家指南列表
     **/
    PageResult<CountryGuideResponse> publicListByPage(Integer page, Integer size, String countryKeyword);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: request 新增国家指南请求参数
     * @Return: Long 新增记录的ID
     * @Description: 新增国家指南
     **/
    Long create(CountryGuideCreateRequest request);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Param: request 修改国家指南请求参数
     * @Return: void
     * @Description: 修改国家指南
     **/
    void update(Long id, CountryGuideUpdateRequest request);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Return: void
     * @Description: 逻辑删除国家指南
     **/
    void delete(Long id);
}