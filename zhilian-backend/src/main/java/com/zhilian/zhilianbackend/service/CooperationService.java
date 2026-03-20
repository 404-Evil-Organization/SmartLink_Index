package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;

public interface CooperationService extends IService<Cooperation> {

    /**
     * 分页查询当前用户的合作记录（非管理员）
     */
    PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String userRole, Long enterpriseId, String status, Integer page, Integer size);

    /**
     * 分页查询合作记录（管理员专用，不限制企业）
     */
    PageResult<CooperationRecordVO> pageMyCooperationsAdmin(Long userId, Long enterpriseId, String status, Integer page, Integer size);

    /**
     * 获取合作记录详情（非管理员，需校验权限）
     */
    CooperationDetailVO getCooperationDetail(Long cooperationId, Long userId, String userRole);

    /**
     * 获取合作记录详情（管理员专用，无权限校验）
     */
    CooperationDetailVO getCooperationDetailAdmin(Long cooperationId);
}