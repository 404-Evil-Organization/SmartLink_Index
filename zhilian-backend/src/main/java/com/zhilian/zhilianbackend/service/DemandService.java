package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.DemandApproveRequest;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.request.DemandUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.DemandMyListVO;
import com.zhilian.zhilianbackend.dto.response.DemandPendingVO;
import com.zhilian.zhilianbackend.dto.response.DemandPublishResponse;
import com.zhilian.zhilianbackend.entity.Demand;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 16:06
 * @Param:
 * @Return:
 * @Description: 需求发布记录业务逻辑接口
**/

public interface DemandService extends IService<Demand> {
    DemandPublishResponse publishDemand(DemandPublishRequest request, Long userId);
    PageResult<DemandPendingVO> getPendingDemandList(Integer page, Integer size);
    void approveDemand(Long demandId, DemandApproveRequest request, Long adminUserId);

    void updateDemand(Long id, DemandUpdateRequest request, Long userId);
    void deleteDemand(Long id, Long userId);
    PageResult<DemandMyListVO> getMyDemandList(Integer page, Integer size, Long manuId, String status, Long userId);
    
    com.zhilian.zhilianbackend.dto.response.DemandDetailVO getDemandDetail(Long id, Long userId);
}