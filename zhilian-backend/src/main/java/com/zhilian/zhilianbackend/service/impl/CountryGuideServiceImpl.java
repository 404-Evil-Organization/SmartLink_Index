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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 使用线程安全的 DateTimeFormatter 替代 SimpleDateFormat
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        wrapper.eq(CountryGuide::getCountry, country.trim())
                // 按更新时间倒序，确保在存在多条记录时优先获取最新一条
                .orderByDesc(CountryGuide::getUpdateTime)
                // 使用 LIMIT 1 明确只取一条，避免 getOne 在多条结果时抛 TooManyResultsException
                .last("LIMIT 1");

        CountryGuide guide = this.getOne(wrapper);

        // 检查是否找到数据
        if (guide == null) {
            log.warn("未找到国家准入指南，country: {}", country);
            throw new BusinessException(404, "未找到该国家的准入指南，请检查国家名称是否正确");
        }

        // 转换为响应DTO
        CountryGuideResponse response = new CountryGuideResponse();
        BeanUtils.copyProperties(guide, response);

        // 处理 documents 字段
        if (guide.getDocuments() != null && !guide.getDocuments().isEmpty()) {
            List<String> documents = parseDocuments(guide.getDocuments());
            response.setDocuments(documents);
        }

        // 格式化时间字段
        if (guide.getCreateTime() != null) {
            LocalDateTime createDateTime = guide.getCreateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            response.setCreateTime(createDateTime.format(DATE_TIME_FORMATTER));
        }
        if (guide.getUpdateTime() != null) {
            LocalDateTime updateDateTime = guide.getUpdateTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            response.setUpdateTime(updateDateTime.format(DATE_TIME_FORMATTER));
        }

        log.info("查询国家准入指南成功，country: {}, id: {}", country, guide.getId());
        return response;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/26 00:15
     * @Param: documentsStr 原始 documents 字符串（可能是 JSON 或逗号分隔的文本）
     * @Return: List<String> 清理后的文档列表
     * @Description: 解析 documents 字段，优先尝试 JSON 解析，失败则按逗号分隔并清理空格和空字符串
     **/
    private List<String> parseDocuments(String documentsStr) {
        // 优先尝试 JSON 解析
        try {
            List<String> documents = objectMapper.readValue(
                    documentsStr,
                    new TypeReference<List<String>>() {}
            );
            log.debug("JSON 解析 documents 成功，共 {} 项", documents.size());
            return documents;
        } catch (JsonProcessingException e) {
            // JSON 解析失败，使用逗号分隔处理
            log.warn("解析 documents 字段为 JSON 失败，使用逗号分隔: {}", documentsStr, e);
            return parseCommaSeparatedDocuments(documentsStr);
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/26 00:15
     * @Param: documentsStr 逗号分隔的字符串
     * @Return: List<String> 清理后的文档列表
     * @Description: 解析逗号分隔的 documents，去除每项前后空格，过滤空字符串
     **/
    private List<String> parseCommaSeparatedDocuments(String documentsStr) {
        if (documentsStr == null || documentsStr.trim().isEmpty()) {
            return List.of();
        }

        List<String> documents = Arrays.stream(documentsStr.split(","))
                .map(String::trim)           // 去除前后空格
                .filter(s -> !s.isEmpty())   // 过滤空字符串
                .collect(Collectors.toList());

        log.debug("逗号分隔解析 documents 成功，共 {} 项", documents.size());
        return documents;
    }
}