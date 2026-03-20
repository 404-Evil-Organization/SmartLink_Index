package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.response.CooperationRecordVO;
import com.zhilian.zhilianbackend.entity.Cooperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface CooperationMapper extends BaseMapper<Cooperation> {

    IPage<CooperationRecordVO> selectMyCooperations(Page<?> page,
                                                    @Param("companyId") Long companyId,
                                                    @Param("role") String role,
                                                    @Param("userId") Long userId,
                                                    @Param("status") String status);
}