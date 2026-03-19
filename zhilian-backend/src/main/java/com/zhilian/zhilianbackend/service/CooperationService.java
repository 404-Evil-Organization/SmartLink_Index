package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录业务逻辑接口
 */
public interface CooperationService extends IService<Cooperation> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 17:30
     * @Param: userId 当前登录用户ID
     * @Param: role 用户角色（manufacture/service）
     * @Param: status 合作状态筛选
     * @Param: page 页码
     * @Param: size 每页条数
     * @return 分页结果
     * @Description: 分页查询当前用户的合作记录
     */
    PageResult<CooperationRecordVO> pageMyCooperations(Long userId, String role, String status, Integer page, Integer size);
}