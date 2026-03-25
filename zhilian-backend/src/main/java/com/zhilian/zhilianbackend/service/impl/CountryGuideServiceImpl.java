package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import com.zhilian.zhilianbackend.mapper.CountryGuideMapper;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import org.springframework.stereotype.Service;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/25 20:19
 * @Param:
 * @Return:
 * @Description: 国家准入指南表业务逻辑实现类
 **/
@Service
public class CountryGuideServiceImpl extends ServiceImpl<CountryGuideMapper, CountryGuide> implements CountryGuideService {
}