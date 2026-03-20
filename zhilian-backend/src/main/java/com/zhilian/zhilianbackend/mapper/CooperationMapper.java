package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import org.apache.ibatis.annotations.Param;

public interface CooperationMapper extends BaseMapper<Cooperation> {

    /**
     * 分页查询当前用户的合作记录（非管理员）
     *
     * @param page      分页参数
     * @param companyId 企业ID（根据角色决定是 manu_id 或 service_id）
     * @param role      角色（manufacture/service）
     * @param userId    当前用户ID（用于计算 hasEvaluated）
     * @param status    状态筛选
     */
    IPage<CooperationRecordVO> selectMyCooperations(Page<?> page,
                                                    @Param("companyId") Long companyId,
                                                    @Param("role") String role,
                                                    @Param("userId") Long userId,
                                                    @Param("status") String status);

    /**
     * 分页查询合作记录（管理员专用，不限制角色）
     *
     * @param page        分页参数
     * @param companyId   企业ID（可选，若传入则匹配 manu_id 或 service_id）
     * @param status      状态筛选
     * @param userId      当前用户ID（用于计算 hasEvaluated）
     */
    IPage<CooperationRecordVO> selectMyCooperationsAdmin(Page<?> page,
                                                         @Param("companyId") Long companyId,
                                                         @Param("status") String status,
                                                         @Param("userId") Long userId);
}