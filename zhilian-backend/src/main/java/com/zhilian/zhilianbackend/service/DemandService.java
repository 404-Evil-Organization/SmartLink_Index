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

public interface DemandService extends IService<Demand> {
    // 已有方法...
    DemandPublishResponse publishDemand(DemandPublishRequest request, Long userId);
    PageResult<DemandPendingVO> getPendingDemandList(Integer page, Integer size);
    void approveDemand(Long demandId, DemandApproveRequest request, Long adminUserId);

    // 新增方法
    void updateDemand(Long id, DemandUpdateRequest request, Long userId);
    void deleteDemand(Long id, Long userId);
    PageResult<DemandMyListVO> getMyDemandList(Integer page, Integer size, Long manuId, String status, Long userId);
}