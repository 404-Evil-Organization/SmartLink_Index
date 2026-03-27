package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationDetailVO;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 23:17
 * @Description: 合作记录业务逻辑接口，定义合作记录的列表查询和详情查询方法
 */
public interface CooperationService extends IService<Cooperation> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: userId 当前用户ID
     * @Param: userRole 当前用户角色
     * @Param: enterpriseId 企业ID（可选）
     * @Param: status 状态筛选
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: 分页的合作记录列表
     * @Description: 分页查询当前用户的合作记录（非管理员）
     */
    PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String userRole, Long enterpriseId, String status, Integer page, Integer size);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: userId 当前用户ID
     * @Param: enterpriseId 企业ID（可选）
     * @Param: status 状态筛选
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: 分页的合作记录列表
     * @Description: 分页查询合作记录（管理员专用，不限制企业）
     */
    PageResult<CooperationRecordVO> pageMyCooperationsAdmin(Long userId, Long enterpriseId, String status, Integer page, Integer size);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: cooperationId 合作记录ID
     * @Param: userId 当前用户ID
     * @Param: userRole 当前用户角色
     * @Return: 合作记录详情
     * @Description: 获取合作记录详情（非管理员，需校验权限）
     */
    CooperationDetailVO getCooperationDetail(Long cooperationId, Long userId, String userRole);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param: cooperationId 合作记录ID
     * @Param: userId 当前用户ID
     * @Return: 合作记录详情
     * @Description: 获取合作记录详情（管理员专用，无权限校验）
     */
    CooperationDetailVO getCooperationDetailAdmin(Long cooperationId, Long userId);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: cooperationId 合作记录ID
     * @Param: currentUserId 当前用户ID
     * @Param: currentUserRole 当前用户角色
     * @Return: 无
     * @Description: 取消合作，仅合作双方或管理员可操作
     */
    void cancelCooperation(Long cooperationId, Long currentUserId, String currentUserRole);
}