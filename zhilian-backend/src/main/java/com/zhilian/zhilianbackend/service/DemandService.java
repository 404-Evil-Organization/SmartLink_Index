package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.DemandApproveRequest;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.response.DemandPendingVO;
import com.zhilian.zhilianbackend.dto.response.DemandPublishResponse;
import com.zhilian.zhilianbackend.entity.Demand;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:23
 * @Description: 需求表业务逻辑接口，定义需求相关的业务方法
 */
public interface DemandService extends IService<Demand> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: request 发布需求请求参数
     * @Param: userId 当前登录用户ID
     * @Return: DemandPublishResponse 包含需求ID和审核状态
     * @Description: 发布需求，保存需求信息及关联标签，默认状态为待审核
     */
    DemandPublishResponse publishDemand(DemandPublishRequest request, Long userId);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: PageResult<DemandPendingVO> 分页的待审核需求列表
     * @Description: 获取待审核需求列表，包含企业名称和关联标签
     */
    PageResult<DemandPendingVO> getPendingDemandList(Integer page, Integer size);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: demandId 需求ID
     * @Param: request 审核请求参数（状态、意见）
     * @Param: adminUserId 当前管理员用户ID
     * @Return: void
     * @Description: 审核需求，通过或驳回，更新审核状态、审核意见等信息
     */
    void approveDemand(Long demandId, DemandApproveRequest request, Long adminUserId);
}