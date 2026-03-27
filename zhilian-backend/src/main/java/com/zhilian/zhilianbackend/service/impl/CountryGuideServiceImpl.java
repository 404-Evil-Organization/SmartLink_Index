package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.CountryGuideCreateRequest;
import com.zhilian.zhilianbackend.dto.request.CountryGuideUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.CountryGuideResponse;
import com.zhilian.zhilianbackend.entity.CountryGuide;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.CountryGuideMapper;
import com.zhilian.zhilianbackend.service.CountryGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

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
        return documents;}
    private final SecurityUtils securityUtils;

    // 分页每页最大条数限制
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: country 国家名称（模糊匹配）
     * @Return: PageResult<CountryGuideResponse> 分页结果
     * @Description: 分页查询国家指南列表，仅管理员可访问
     **/
    @Override
    public PageResult<CountryGuideResponse> listByPage(Integer page, Integer size, String country) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 分页参数默认值
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        // 限制最大分页大小，避免恶意请求
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Page<CountryGuide> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(country)) {
            wrapper.like(CountryGuide::getCountry, country);
        }
        wrapper.orderByDesc(CountryGuide::getCreateTime);

        Page<CountryGuide> resultPage = this.page(mpPage, wrapper);

        List<CountryGuideResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), records, resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/3/27 18:44
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: keyword 国家名称关键词（模糊匹配）
     * @Return: PageResult<CountryGuideResponse> 分页结果
     * @Description: 公开接口-分页查询国家指南列表，无需鉴权
     **/
    @Override
    public PageResult<CountryGuideResponse> publicListByPage(Integer page, Integer size, String keyword) {
        // 分页参数默认值
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        // 限制最大分页大小，避免恶意请求
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Page<CountryGuide> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CountryGuide::getCountry, keyword);
        }
        wrapper.orderByDesc(CountryGuide::getCreateTime);

        Page<CountryGuide> resultPage = this.page(mpPage, wrapper);

        List<CountryGuideResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), records, resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: request 新增国家指南请求参数
     * @Return: Long 新增记录的ID
     * @Description: 新增国家指南，校验唯一性，仅管理员可操作
     **/
    @Override
    @Transactional
    public Long create(CountryGuideCreateRequest request) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // 基础参数校验：请求体与国家名称不能为空，且长度限制为 50 个字符以内
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        String country = request.getCountry();
        if (!StringUtils.hasText(country)) {
            throw new BusinessException(400, "国家名称不能为空");
        }
        if (country.length() > 50) {
            throw new BusinessException(400, "国家名称长度不能超过50个字符");
        }
        // 检查国家名称是否已存在（未删除）
        LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CountryGuide::getCountry, country);
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(409, "国家名称已存在");
        }

        CountryGuide entity = new CountryGuide();
        BeanUtils.copyProperties(request, entity);
        // 设置逻辑删除字段为未删除状态（使用常量）
        entity.setDeleted(DateConstants.getNotDeletedTime());

        try {
            boolean saved = this.save(entity);
            if (!saved) {
                throw new BusinessException(500, "创建失败，请稍后重试");
            }
        } catch (DuplicateKeyException e) {
            // 捕获并发场景下数据库唯一键冲突，转换为业务异常 409
            throw new BusinessException(409, "国家名称已存在");
        }
        return entity.getId();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Param: request 修改国家指南请求参数
     * @Return: void
     * @Description: 修改国家指南，支持部分更新，仅管理员可操作
     **/
    @Override
    @Transactional
    public void update(Long id, CountryGuideUpdateRequest request) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // ID 合法性校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "ID参数非法");
        }

        // 请求体空校验
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }

        // 查询原记录（未删除）
        CountryGuide existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "国家指南不存在");
        }

        // 如果修改了国家名称，检查唯一性
        if (StringUtils.hasText(request.getCountry()) && !request.getCountry().equals(existing.getCountry())) {
            LambdaQueryWrapper<CountryGuide> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CountryGuide::getCountry, request.getCountry());
            long count = this.count(wrapper);
            if (count > 0) {
                throw new BusinessException(409, "国家名称已存在");
            }
            existing.setCountry(request.getCountry());
        }

        // 更新其他字段
        if (request.getRequirements() != null) {
            existing.setRequirements(request.getRequirements());
        }
        if (request.getProcess() != null) {
            existing.setProcess(request.getProcess());
        }
        if (request.getDocuments() != null) {
            existing.setDocuments(request.getDocuments());
        }

        // 显式设置更新时间，确保即使自动填充策略为 strictUpdateFill 时也能正确更新
        existing.setUpdateTime(new Date());

        try {
            boolean updated = this.updateById(existing);
            if (!updated) {
                // updateById 返回 false 不一定表示“更新失败”，可能是无实际字段变更导致 affectedRows = 0
                // 为避免将幂等更新当成 500 错误，这里仅在记录已不存在时返回 404
                CountryGuide latest = this.getById(id);
                if (latest == null) {
                    throw new BusinessException(404, "国家指南不存在或已删除");
                }
                // 记录仍然存在但 updateById 返回 false：视为“无实际变更”的幂等成功，不抛异常
                return;
            }
        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突（并发场景下修改为国家名称已存在）
            throw new BusinessException(409, "国家名称已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: id 国家指南ID
     * @Return: void
     * @Description: 逻辑删除国家指南，仅管理员可操作
     **/
    @Override
    @Transactional
    public void delete(Long id) {
        // 管理员权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问");
        }

        // ID 合法性校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "ID参数非法");
        }

        // 逻辑删除（MyBatis Plus会自动设置deleted为当前时间）
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new BusinessException(404, "国家指南不存在或已删除");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 21:33
     * @Param: entity 国家指南实体
     * @Return: CountryGuideResponse 响应对象
     * @Description: 将实体对象转换为响应对象
     **/
    private CountryGuideResponse convertToResponse(CountryGuide entity) {
        CountryGuideResponse response = new CountryGuideResponse();
        BeanUtils.copyProperties(entity, response);
        
        // 处理 documents 字段
        if (entity.getDocuments() != null && !entity.getDocuments().isEmpty()) {
            List<String> documents = parseDocuments(entity.getDocuments());
            response.setDocuments(documents);
        }
        
        return response;
    }
}