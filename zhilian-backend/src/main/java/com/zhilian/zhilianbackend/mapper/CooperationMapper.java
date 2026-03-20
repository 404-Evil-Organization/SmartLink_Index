package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/20 19:00
 * @Description: 合作记录 Mapper 接口，提供合作记录的自定义分页查询方法
 */
public interface CooperationMapper extends BaseMapper<Cooperation> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param:
     * @Return:
     * @Description: 分页查询当前用户的合作记录（非管理员）
     */
    IPage<CooperationRecordVO> selectMyCooperations(Page<?> page,
                                                    @Param("companyId") Long companyId,
                                                    @Param("role") String role,
                                                    @Param("userId") Long userId,
                                                    @Param("status") String status);

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/20 19:00
     * @Param:
     * @Return:
     * @Description: 分页查询合作记录（管理员专用，不限制角色）
     */
    IPage<CooperationRecordVO> selectMyCooperationsAdmin(Page<?> page,
                                                         @Param("companyId") Long companyId,
                                                         @Param("status") String status,
                                                         @Param("userId") Long userId);
}