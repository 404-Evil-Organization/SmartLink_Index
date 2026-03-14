package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.ServiceTagResponse;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:23
 * @Param: 
 * @Return: 
 * @Description: 标签字典表业务逻辑接口，定义标签相关的业务方法
**/
public interface TagService extends IService<Tag> {

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:49
     * @Param: queryRequest 分页查询参数
     * @Return: IPage<TagResponse> 分页后的标签列表
     * @Description: 分页查询标签，支持按名称模糊查询和按类别精确筛选
    **/
    IPage<TagResponse> pageQuery(TagQueryRequest queryRequest);

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:49
     * @Param: request 新增标签请求参数
     * @Return: Long 新增标签的ID
     * @Description: 新增标签，会校验标签名称是否已存在
    **/
    Long addTag(TagRequest request);

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:50
     * @Param: id 要修改的标签ID;request 修改标签请求参数
     * @Return: 
     * @Description: 修改标签信息，如果修改名称会检查新名称是否与其他标签冲突
    **/
    void updateTag(Long id, TagRequest request);

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:50
     * @Param: id 要删除的标签ID
     * @Return: 
     * @Description: 逻辑删除标签
    **/
    void deleteTag(Long id);

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:50
     * @Param: id 要查询的标签ID
     * @Return: TagResponse 标签详情
     * @Description: 根据ID获取标签详细信息
    **/
    TagResponse getTagDetail(Long id);

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:51
     * @Param: 
     * @Return: List<ServiceTagResponse> 服务标签列表
     * @Description: 获取所有类别为'service'的标签，用于服务商的服务类型多选
    **/
    List<ServiceTagResponse> getServiceTags();

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: 
     * @Return: List<ServiceTagResponse> 认证类型标签列表
     * @Description: 获取所有类别为'certification'的标签，用于证书类型选择
    **/
    List<ServiceTagResponse> getCertificationTags();

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: 
     * @Return: List<ServiceTagResponse> 产品类型标签列表
     * @Description: 获取所有类别为'product'的标签，用于产品类型选择
    **/
    List<ServiceTagResponse> getProductTags();

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: 
     * @Return: List<ServiceTagResponse> 其他类型标签列表
     * @Description: 获取所有类别为'rests'的标签，用于通用标签选择
    **/
    List<ServiceTagResponse> getRestsTags();
}
