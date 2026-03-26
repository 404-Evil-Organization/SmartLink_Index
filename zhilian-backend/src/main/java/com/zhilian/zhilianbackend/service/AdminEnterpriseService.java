package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.dto.response.PendingEnterpriseResponse;

/**
 * @Author: 6017
 * @Date: 2026/3/24 23:35
 * @Param:
 * @Return:
 * @Description: 管理员企业审核服务接口，提供企业审核相关业务逻辑
**/
public interface AdminEnterpriseService {

    /**
     * @Author: 6017
     * @Date: 2026/3/24 23:35
     * @Param:  page 页码  size 每页条数
     * @Return: IPage<PendingEnterpriseResponse> 待审核企业分页数据
     * @Description: 获取待审核企业列表，包含制造企业和服务商，按创建时间倒序排列
    **/
    IPage<PendingEnterpriseResponse> getPendingEnterpriseList(Integer page, Integer size);

   /**
    * @Author: 6017
    * @Date: 2026/3/24 23:36
    * @Param: enterpriseId 企业ID  type 企业类型（manufacture/service）  status 审核状态（approved/rejected）  remark 审核意见（驳回时必填）  auditUserId 审核人ID（当前登录管理员ID）
    * @Return: 
    * @Description: 审核企业，支持通过或驳回，审核后更新企业状态并记录审核信息
   **/
    void approveEnterprise(Long enterpriseId, String type, String status, String remark, Long auditUserId);
}