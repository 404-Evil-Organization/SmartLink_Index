package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/25 18:03
 * @Param:
 * @Return:
 * @Description: 国家准入指南 Mapper 接口
 **/
@Mapper
public interface CountryGuideMapper extends BaseMapper<CountryGuide> {
}
