package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CountryGuideMapper;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/25 20:19
 * @Param:
 * @Return:
 * @Description: 国家准入指南表业务逻辑实现类
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class CountryGuideServiceImpl extends ServiceImpl<CountryGuideMapper, CountryGuide> implements CountryGuideService {

    private final ObjectMapper objectMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @Author: 6017
     * @Date: 2026/3/26 00:02
     * @Param: country 国家名称
     * @Return: CountryGuideResponse 国家准入指南响应对象
     * @Description: 根据国家名称从数据库查询准入指南，处理documents字段的JSON解析，格式化时间字段
    **/
    @Override
    public CountryGuideResponse getByCountry(String country) {
        log.info("查询国家准入指南，country: {}", country);

        // 参数校验
        if (country == null || country.trim().isEmpty()) {
            throw new BusinessException(400, "国家名称不能为空");
        }

        // 构建查询条件：国家名称匹配 + 未删除（@TableLogic 会自动处理）
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CountryGuide::getCountry, country.trim());

        CountryGuide guide = this.getOne(wrapper);

        // 检查是否找到数据
        if (guide == null) {
            log.warn("未找到国家准入指南，country: {}", country);
            throw new BusinessException(404, "未找到该国家的准入指南，请检查国家名称是否正确");
        }

        // 转换为响应DTO
        CountryGuideResponse response = new CountryGuideResponse();
        BeanUtils.copyProperties(guide, response);

        // 处理 documents 字段（使用 Jackson 解析 JSON）
        if (guide.getDocuments() != null && !guide.getDocuments().isEmpty()) {
            try {
                // 尝试解析为 JSON 数组
                List<String> documents = objectMapper.readValue(
                        guide.getDocuments(),
                        new TypeReference<List<String>>() {}
                );
                response.setDocuments(documents);
            } catch (JsonProcessingException e) {
                // 如果 JSON 解析失败，则按逗号分隔处理
                log.warn("解析 documents 字段为 JSON 失败，使用逗号分隔: {}", guide.getDocuments(), e);
                String[] docArray = guide.getDocuments().split(",");
                response.setDocuments(Arrays.asList(docArray));
            }
        }

        // 格式化时间字段
        if (guide.getCreateTime() != null) {
            response.setCreateTime(DATE_FORMAT.format(guide.getCreateTime()));
        }
        if (guide.getUpdateTime() != null) {
            response.setUpdateTime(DATE_FORMAT.format(guide.getUpdateTime()));
        }

        log.info("查询国家准入指南成功，country: {}, id: {}", country, guide.getId());
        return response;
    }
}