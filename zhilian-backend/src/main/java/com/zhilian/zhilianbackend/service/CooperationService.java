package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;

public interface CooperationService extends IService<Cooperation> {

    /**
     * 分页查询当前用户的合作记录
     *
     * @param userId       当前登录用户ID
     * @param userRole     当前登录用户角色（manufacture/service）
     * @param enterpriseId 企业ID（可选），传 null 时根据用户角色自动获取默认企业
     * @param status       合作状态筛选
     * @param page         页码
     * @param size         每页条数
     * @return 分页结果
     */
    PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String userRole, Long enterpriseId, String status, Integer page, Integer size);

    /**
     * 获取合作记录详情
     *
     * @param cooperationId 合作记录ID
     * @param userId        当前登录用户ID
     * @param userRole      当前登录用户角色
     * @return 合作详情
     */
    CooperationDetailVO getCooperationDetail(Long cooperationId, Long userId, String userRole);
}