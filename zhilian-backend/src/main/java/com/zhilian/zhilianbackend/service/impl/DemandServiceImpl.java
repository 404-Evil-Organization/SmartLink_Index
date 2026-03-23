package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.DemandApproveRequest;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.response.DemandPendingVO;
import com.zhilian.zhilianbackend.dto.response.DemandPublishResponse;
import com.zhilian.zhilianbackend.entity.Demand;
import com.zhilian.zhilianbackend.entity.DemandTag;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.DemandMapper;
import com.zhilian.zhilianbackend.mapper.DemandTagMapper;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.DemandService;
import com.zhilian.zhilianbackend.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:29
 * @Description: 需求表业务逻辑实现类，实现需求相关的业务方法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    private final ManufactureMapper manufactureMapper;
    private final TagService tagService;
    private final DemandTagMapper demandTagMapper;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: request 发布需求请求参数
     * @Param: userId 当前登录用户ID
     * @Return: DemandPublishResponse 包含需求ID和审核状态
     * @Description: 发布需求，校验用户为制造企业，保存需求及标签关联，默认状态为待审核
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandPublishResponse publishDemand(DemandPublishRequest request, Long userId) {
        // 1. 校验用户是否是制造企业
        Manufacture manufacture = manufactureMapper.selectOne(
                new LambdaQueryWrapper<Manufacture>()
                        .eq(Manufacture::getUserId, userId)
        );
        if (manufacture == null) {
            throw new BusinessException(403, "只有制造企业可以发布需求");
        }

        // 2. 构建需求实体
        Demand demand = new Demand();
        demand.setManuId(manufacture.getId());
        demand.setTitle(request.getTitle());
        demand.setDescription(request.getDescription());
        demand.setExpectedBudget(request.getExpectedBudget());
        demand.setDeadline(request.getDeadline());
        demand.setStatus("draft");
        demand.setAuditStatus("pending");
        demand.setViews(0);

        // 3. 保存需求
        boolean saved = this.save(demand);
        if (!saved) {
            throw new BusinessException(500, "发布需求失败");
        }

        // 4. 保存标签关联（根据标签名称查询 ID）
        if (!CollectionUtils.isEmpty(request.getTags())) {
            // 根据名称查询标签（不校验是否存在，存在的才插入）
            List<Tag> tags = tagService.lambdaQuery()
                    .in(Tag::getName, request.getTags())
                    .list();

            // 提取标签 ID（只处理存在的标签）
            List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());

            // 批量插入 demand_tag（手动设置 deleted 字段）
            List<DemandTag> demandTags = tagIds.stream()
                    .map(tagId -> new DemandTag()
                            .setDemandId(demand.getId())
                            .setTagId(tagId)
                            .setDeleted(DateConstants.getNotDeletedTime()))
                    .collect(Collectors.toList());
            demandTagMapper.insertBatch(demandTags);
        }

        log.info("需求发布成功，ID：{}，制造企业：{}", demand.getId(), manufacture.getCompanyName());
        return new DemandPublishResponse(demand.getId(), demand.getAuditStatus());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: page 页码
     * @Param: size 每页条数
     * @Return: PageResult<DemandPendingVO> 分页的待审核需求列表
     * @Description: 分页查询待审核需求，先查询需求基本信息，再批量查询标签组装
     */
    @Override
    public PageResult<DemandPendingVO> getPendingDemandList(Integer page, Integer size) {
        // 1. 分页查询需求基本信息（不含标签）
        Page<DemandPendingVO> mpPage = new Page<>(page, size);
        IPage<DemandPendingVO> voPage = baseMapper.selectPendingDemandPage(mpPage);
        List<DemandPendingVO> records = voPage.getRecords();

        // 2. 如果没有数据，直接返回
        if (records.isEmpty()) {
            return PageResult.from(voPage);
        }

        // 3. 收集所有需求 ID
        List<Long> demandIds = records.stream()
                .map(DemandPendingVO::getId)
                .collect(Collectors.toList());

        // 4. 批量查询这些需求的所有标签
        List<DemandTag> demandTags = demandTagMapper.selectList(
                new LambdaQueryWrapper<DemandTag>()
                        .in(DemandTag::getDemandId, demandIds)
                        .eq(DemandTag::getDeleted, DateConstants.getNotDeletedTime())
        );

        // 5. 提取所有标签 ID，并批量查询标签名称
        List<Long> tagIds = demandTags.stream()
                .map(DemandTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        // 使用 final 变量确保 effectively final
        final Map<Long, String> tagIdToNameMap = tagIds.isEmpty() ?
                Collections.emptyMap() :
                tagService.listByIds(tagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getName));

        // 6. 构建 demandId -> List<TagSimpleVO> 映射
        Map<Long, List<DemandPendingVO.TagSimpleVO>> demandTagsMap = demandTags.stream()
                .collect(Collectors.groupingBy(
                        DemandTag::getDemandId,
                        Collectors.mapping(dt -> {
                            DemandPendingVO.TagSimpleVO tagVO = new DemandPendingVO.TagSimpleVO();
                            tagVO.setId(dt.getTagId());
                            tagVO.setName(tagIdToNameMap.get(dt.getTagId()));
                            return tagVO;
                        }, Collectors.toList())
                ));

        // 7. 填充每个需求的标签列表
        for (DemandPendingVO record : records) {
            List<DemandPendingVO.TagSimpleVO> tags = demandTagsMap.getOrDefault(record.getId(), Collections.emptyList());
            record.setTags(tags);
        }

        return PageResult.from(voPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: demandId 需求ID
     * @Param: request 审核请求参数（状态、意见）
     * @Param: adminUserId 当前管理员用户ID
     * @Return: void
     * @Description: 审核需求，通过时更新审核状态和业务状态；驳回时逻辑删除需求及关联的标签
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveDemand(Long demandId, DemandApproveRequest request, Long adminUserId) {
        Demand demand = this.getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 只允许审核待审核状态的需求
        if (!"pending".equals(demand.getAuditStatus())) {
            throw new BusinessException(400, "该需求已审核过，不能重复审核");
        }

        String status = request.getStatus();
        if ("approved".equals(status)) {
            // 审核通过：更新状态
            demand.setAuditStatus("approved");
            demand.setStatus("published");
            demand.setAuditRemark(request.getRemark());
            demand.setAuditTime(new Date());
            demand.setAuditUserId(adminUserId);
            boolean updated = this.updateById(demand);
            if (!updated) {
                throw new BusinessException(500, "审核通过失败");
            }
            log.info("需求审核通过，ID：{}，审核人：{}", demandId, adminUserId);

        } else if ("rejected".equals(status)) {
            // 驳回时，校验审核意见
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException(400, "驳回时必须填写审核意见");
            }

            // 1. 逻辑删除关联的 demand_tag 记录
            LambdaQueryWrapper<DemandTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemandTag::getDemandId, demandId);
            // 逻辑删除：设置 deleted 为当前时间（注意：MyBatis Plus 配置中逻辑删除值使用 now()）
            // 这里手动更新 deleted 字段，但为了保持一致性，我们可以调用 remove 方法（逻辑删除）
            // 如果 DemandTag 有 @TableLogic，调用 remove 会逻辑删除
            boolean deletedTags = demandTagMapper.delete(wrapper) > 0;
            if (deletedTags) {
                log.info("已逻辑删除需求 {} 的关联标签", demandId);
            }

            // 2. 逻辑删除需求本身
            boolean deletedDemand = this.removeById(demandId);
            if (!deletedDemand) {
                throw new BusinessException(500, "驳回删除需求失败");
            }

            log.info("需求已驳回并删除，ID：{}，原因：{}，审核人：{}", demandId, request.getRemark(), adminUserId);
        } else {
            throw new BusinessException(400, "审核状态不合法");
        }
    }
}