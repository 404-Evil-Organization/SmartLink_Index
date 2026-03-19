package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/19 17:30
 * @Description: 合作记录Mapper接口
 */
public interface CooperationMapper extends BaseMapper<Cooperation> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/19 17:30
     * @Param: page 分页参数
     * @Param: companyId 当前用户的企业ID
     * @Param: role 当前用户角色（决定使用 manu_id 还是 service_id 作为筛选条件）
     * @Param: userId 当前用户ID（用于判断是否已评价）
     * @Param: status 合作状态筛选
     * @return 分页的合作记录VO列表
     * @Description: 自定义分页查询当前用户的合作记录
     */
    IPage<CooperationRecordVO> selectMyCooperations(Page<?> page,
                                                    @Param("companyId") Long companyId,
                                                    @Param("role") String role,
                                                    @Param("userId") Long userId,
                                                    @Param("status") String status);
}