package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.response.DemandMarketVO;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.entity.*;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.*;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.utils.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25
 * @Description: 需求业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandServiceImpl implements DemandService {

    private final DemandMapper demandMapper;
    private final DemandTagMapper demandTagMapper;
    private final TagMapper tagMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final CooperationMapper cooperationMapper;

    // 使用 java.util.Date 类型，与数据库 deleted 字段保持一致（用于 LambdaQueryWrapper）
    private final Date notDeletedTime = DateConstants.getNotDeletedTime();

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: page 页码
     * @Param: size 每页条数
     * @Param: keyword 标题关键词
     * @Param: tagIds 标签ID列表
     * @Param: budgetMin 最小预算
     * @Param: budgetMax 最大预算
     * @Param: deadlineStart 截止日期开始范围（包含）
     * @Param: deadlineEnd 截止日期结束范围（包含）
     * @Return: 分页的市场需求列表
     * @Description: 分页查询市场需求列表（已审核通过且已发布的需求），并填充制造企业信息和标签
     */
    @Override
    public PageResult<DemandMarketVO> pageMarketDemands(
            Integer page, Integer size,
            String keyword, List<Long> tagIds,
            BigDecimal budgetMin, BigDecimal budgetMax,
            LocalDate deadlineStart, LocalDate deadlineEnd) {

        // 对关键词进行 SQL LIKE 转义，防止用户输入的通配符影响查询结果
        String escapedKeyword = SqlUtils.escapeSqlLike(keyword);

        // 将 LocalDate 转换为 LocalDateTime，确保查询范围包含完整日期
        // deadlineStart: 当天 00:00:00
        // deadlineEnd: 当天 23:59:59.999999999（使用 LocalTime.MAX）
        LocalDateTime startDateTime = deadlineStart != null
                ? deadlineStart.atStartOfDay()
                : null;
        LocalDateTime endDateTime = deadlineEnd != null
                ? deadlineEnd.atTime(LocalTime.MAX)
                : null;

        Page<DemandMarketVO> pageParam = new Page<>(page, size);
        IPage<DemandMarketVO> iPage = demandMapper.selectMarketDemands(
                pageParam, escapedKeyword, tagIds, budgetMin, budgetMax,
                startDateTime, endDateTime, DateConstants.getNotDeletedLocalDateTime()
        );

        List<DemandMarketVO> records = iPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.from(iPage);
        }

        // 初始化 tags 字段为空列表，避免返回 null
        for (DemandMarketVO vo : records) {
            vo.setTags(new ArrayList<>());
        }

        // 批量查询标签
        List<Long> demandIds = records.stream().map(DemandMarketVO::getId).toList();
        LambdaQueryWrapper<DemandTag> dtWrapper = new LambdaQueryWrapper<>();
        dtWrapper.in(DemandTag::getDemandId, demandIds)
                .eq(DemandTag::getDeleted, notDeletedTime);
        List<DemandTag> demandTags = demandTagMapper.selectList(dtWrapper);

        if (!demandTags.isEmpty()) {
            Set<Long> tagIdSet = demandTags.stream().map(DemandTag::getTagId).collect(Collectors.toSet());
            List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getId, tagIdSet)
                    .eq(Tag::getDeleted, notDeletedTime));
            Map<Long, Tag> tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, t -> t));

            // 构建需求ID -> 标签列表的映射
            Map<Long, List<TagResponse>> demandTagMap = new HashMap<>();
            for (DemandTag dt : demandTags) {
                Tag tag = tagMap.get(dt.getTagId());
                if (tag != null) {
                    TagResponse tagResp = new TagResponse();
                    tagResp.setId(tag.getId());
                    tagResp.setName(tag.getName());
                    tagResp.setCategory(tag.getCategory());
                    tagResp.setDescription(tag.getDescription());
                    tagResp.setCreateTime(tag.getCreateTime());
                    tagResp.setUpdateTime(tag.getUpdateTime());
                    demandTagMap.computeIfAbsent(dt.getDemandId(), k -> new ArrayList<>()).add(tagResp);
                }
            }

            // 设置标签（已有初始空列表，覆盖即可）
            for (DemandMarketVO vo : records) {
                vo.setTags(demandTagMap.getOrDefault(vo.getId(), new ArrayList<>()));
            }
        }

        return PageResult.from(iPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: demandId 需求ID
     * @Param: serviceId 服务商企业ID
     * @Param: currentUserId 当前用户ID
     * @Return: 新创建的合作记录ID
     * @Description: 服务商接取需求，使用行锁防止并发，校验服务商资质和需求状态，成功后创建合作记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long acceptDemand(Long demandId, Long serviceId, Long currentUserId) {
        // 1. 校验服务商
        ServiceProvider sp = serviceProviderMapper.selectById(serviceId);
        if (sp == null) {
            throw new BusinessException(404, "服务商企业不存在");
        }
        if (!sp.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作该服务商企业");
        }
        if (!"approved".equals(sp.getAuditStatus())) {
            throw new BusinessException(403, "服务商企业未审核通过，无法接取需求");
        }

        // 2. 行锁获取需求（SQL 已过滤逻辑删除）
        Demand demand = demandMapper.selectForUpdateById(demandId, DateConstants.getNotDeletedLocalDateTime());
        if (demand == null) {
            throw new BusinessException(404, "需求不存在或已被删除");
        }
        // 状态校验（SQL 已过滤 deleted，无需额外校验）
        if (!"published".equals(demand.getStatus())) {
            throw new BusinessException(409, "需求状态不可接取");
        }
        if (!"approved".equals(demand.getAuditStatus())) {
            throw new BusinessException(409, "需求未审核通过");
        }

        // 3. 更新需求状态
        demand.setStatus("matched");
        demand.setUpdateTime(new Date());
        int updateRows = demandMapper.updateById(demand);
        if (updateRows == 0) {
            throw new BusinessException(500, "更新需求状态失败");
        }

        // 4. 创建合作记录
        Cooperation cooperation = new Cooperation();
        cooperation.setManuId(demand.getManuId());
        cooperation.setServiceId(serviceId);
        cooperation.setDemandId(demandId);
        cooperation.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        cooperation.setAmount(demand.getExpectedBudget());
        cooperation.setDescription("通过接取需求建立合作");
        cooperation.setStatus("ongoing");
        cooperation.setDeleted(notDeletedTime);
        cooperation.setCreateTime(new Date());
        cooperation.setUpdateTime(new Date());

        int insertRows = cooperationMapper.insert(cooperation);
        if (insertRows == 0) {
            throw new BusinessException(500, "创建合作记录失败");
        }

        log.info("服务商接取需求成功，需求ID: {}, 服务商ID: {}, 合作ID: {}", demandId, serviceId, cooperation.getId());
        return cooperation.getId();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25
     * @Param: demandId 需求ID
     * @Return: 无
     * @Description: 将需求状态重置为已发布（用于取消合作时），使用条件更新确保并发安全
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetDemandStatusToPublished(Long demandId) {
        // 使用条件更新，只有当需求存在、未逻辑删除且状态为 matched 时才更新为 published
        LambdaUpdateWrapper<Demand> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Demand::getId, demandId)
                .eq(Demand::getDeleted, notDeletedTime)      // 未逻辑删除
                .eq(Demand::getStatus, "matched")           // 仅 matched 状态
                .set(Demand::getStatus, "published")
                .set(Demand::getUpdateTime, new Date());

        int updateRows = demandMapper.update(null, updateWrapper);
        if (updateRows == 0) {
            // 未匹配到符合条件的记录，可能是需求不存在、已逻辑删除或状态已变更
            log.warn("重置需求状态失败，需求ID: {}，可能已被删除或状态已变更", demandId);
            throw new BusinessException(409, "需求状态已变更，无法重置为已发布");
        }
        log.info("需求状态重置为 published，需求ID: {}", demandId);
    }
}