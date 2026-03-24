package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.DemandApproveRequest;
import com.zhilian.zhilianbackend.dto.request.DemandPublishRequest;
import com.zhilian.zhilianbackend.dto.request.DemandUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.DemandMyListVO;
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
import com.zhilian.zhilianbackend.utils.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: request 发布需求请求参数
     * @Param: userId 当前登录用户ID
     * @Return: DemandPublishResponse 包含需求ID和审核状态
     * @Description: 发布需求，校验用户为制造企业且只能为自己的企业发布，保存需求及标签关联，默认状态为待审核
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandPublishResponse publishDemand(DemandPublishRequest request, Long userId) {
        // 1. 校验用户角色是否为制造企业
        String role = securityUtils.getCurrentUserRole();
        if (!"manufacture".equals(role)) {
            throw new BusinessException(403, "只有制造企业可以发布需求");
        }

        // 2. 根据传入的 manuId 查询制造企业
        Long manuId = request.getManuId();
        Manufacture manufacture = manufactureMapper.selectById(manuId);
        if (manufacture == null) {
            throw new BusinessException(404, "制造企业不存在");
        }

        // 3. 校验该制造企业是否属于当前用户
        if (!manufacture.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能为自己的企业发布需求");
        }

        // 4. 构建需求实体
        Demand demand = new Demand();
        demand.setManuId(manuId);
        demand.setTitle(request.getTitle());
        demand.setDescription(request.getDescription());
        demand.setExpectedBudget(request.getExpectedBudget());
        demand.setDeadline(request.getDeadline());
        demand.setStatus("draft");
        demand.setAuditStatus("pending");
        demand.setViews(0);

        // 5. 保存需求
        boolean saved = this.save(demand);
        if (!saved) {
            throw new BusinessException(500, "发布需求失败");
        }

        // 6. 保存标签关联
        if (!CollectionUtils.isEmpty(request.getTags())) {
            // 先对标签 ID 去重，避免重复 ID 影响存在性校验和唯一键约束
            Set<Long> distinctTagIds = new LinkedHashSet<>(request.getTags());
            // 校验标签是否存在（基于去重后的 ID 集合）
            List<Tag> tags = tagService.listByIds(distinctTagIds);
            if (tags.size() != distinctTagIds.size()) {
                Set<Long> existingIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
                List<Long> missingIds = distinctTagIds.stream()
                        .filter(id -> !existingIds.contains(id))
                        .collect(Collectors.toList());
                throw new BusinessException(400, "以下标签 ID 不存在: " + missingIds);
            }
            List<DemandTag> demandTags = distinctTagIds.stream()
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
        // 纵深防御：待审核需求列表仅允许管理员访问，防止其他调用方绕过 Controller 权限校验
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限访问该资源");
        }
        Page<DemandPendingVO> mpPage = new Page<>(page, size);
        IPage<DemandPendingVO> voPage = baseMapper.selectPendingDemandPage(mpPage, DateConstants.getNotDeletedLocalDateTime());
        List<DemandPendingVO> records = voPage.getRecords();

        if (records.isEmpty()) {
            return PageResult.from(voPage);
        }

        List<Long> demandIds = records.stream()
                .map(DemandPendingVO::getId)
                .collect(Collectors.toList());

        List<DemandTag> demandTags = demandTagMapper.selectList(
                new LambdaQueryWrapper<DemandTag>()
                        .in(DemandTag::getDemandId, demandIds)
                        .eq(DemandTag::getDeleted, DateConstants.getNotDeletedTime())
        );

        List<Long> tagIds = demandTags.stream()
                .map(DemandTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, String> tagIdToNameMap = tagIds.isEmpty() ?
                Collections.emptyMap() :
                tagService.listByIds(tagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getName));

        Map<Long, List<DemandPendingVO.TagSimpleVO>> demandTagsMap = demandTags.stream()
                .map(dt -> {
                    String tagName = tagIdToNameMap.get(dt.getTagId());
                    if (tagName == null) {
                        log.warn("标签 ID {} 已不存在或被删除，需求 ID {} 的标签将被忽略", dt.getTagId(), dt.getDemandId());
                        return null;
                    }
                    DemandPendingVO.TagSimpleVO tagVO = new DemandPendingVO.TagSimpleVO();
                    tagVO.setId(dt.getTagId());
                    tagVO.setName(tagName);
                    return new AbstractMap.SimpleEntry<>(dt.getDemandId(), tagVO);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

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
     * @Description: 审核需求，通过时更新审核状态和业务状态；驳回时更新审核状态为rejected，业务状态保持draft。
     *               使用带条件的原子更新避免并发重复审核。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveDemand(Long demandId, DemandApproveRequest request, Long adminUserId) {
        // 纵深防御：在 Service 层校验管理员权限
        if (!securityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限操作");
        }

        String status = request.getStatus();
        // 构造原子更新条件：id 且 audit_status = 'pending'
        LambdaUpdateWrapper<Demand> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Demand::getId, demandId)
                .eq(Demand::getAuditStatus, "pending");

        if ("approved".equals(status)) {
            updateWrapper.set(Demand::getAuditStatus, "approved")
                    .set(Demand::getStatus, "published")
                    .set(Demand::getAuditRemark, request.getRemark())
                    .set(Demand::getAuditTime, new Date())
                    .set(Demand::getAuditUserId, adminUserId);
        } else if ("rejected".equals(status)) {
            // 驳回时校验意见
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException(400, "驳回时必须填写审核意见");
            }
            updateWrapper.set(Demand::getAuditStatus, "rejected")
                    .set(Demand::getStatus, "draft")
                    .set(Demand::getAuditRemark, request.getRemark())
                    .set(Demand::getAuditTime, new Date())
                    .set(Demand::getAuditUserId, adminUserId);
        } else {
            throw new BusinessException(400, "审核状态不合法");
        }

        // 执行条件更新
        boolean updated = this.update(updateWrapper);

        if (!updated) {
            // 更新失败，可能因为需求不存在或状态已不是 pending
            // 再查询一次，获取更精确的错误原因
            Demand demand = this.getById(demandId);
            if (demand == null) {
                throw new BusinessException(404, "需求不存在");
            }
            // 状态已变
            throw new BusinessException(400, "该需求已审核过，不能重复审核");
        }

        log.info("需求审核{}，ID：{}，审核人：{}",
                "approved".equals(status) ? "通过" : "驳回", demandId, adminUserId);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Description: 编辑需求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemand(Long id, DemandUpdateRequest request, Long userId) {
        Demand demand = this.getById(id);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 权限校验：如果是 admin 则跳过企业归属检查
        if (!securityUtils.isAdmin()) {
            Manufacture manufacture = manufactureMapper.selectById(demand.getManuId());
            if (manufacture == null || !manufacture.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权操作此需求");
            }
        }

        String status = demand.getStatus();
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException(400, "当前状态不可编辑");
        }

        // 记录是否有字段实际变更（用于幂等处理）
        boolean hasChange = false;
        if (request.getTitle() != null && !request.getTitle().equals(demand.getTitle())) {
            demand.setTitle(request.getTitle());
            hasChange = true;
        }
        if (request.getDescription() != null && !request.getDescription().equals(demand.getDescription())) {
            demand.setDescription(request.getDescription());
            hasChange = true;
        }
        if (request.getExpectedBudget() != null && (demand.getExpectedBudget() == null || !request.getExpectedBudget().equals(demand.getExpectedBudget()))) {
            demand.setExpectedBudget(request.getExpectedBudget());
            hasChange = true;
        }
        if (request.getDeadline() != null && (demand.getDeadline() == null || !request.getDeadline().equals(demand.getDeadline()))) {
            demand.setDeadline(request.getDeadline());
            hasChange = true;
        }

        if (hasChange) {
            boolean updated = this.updateById(demand);
            if (!updated) {
                // 理论上记录已存在，更新失败可能是并发删除，抛出异常
                throw new BusinessException(500, "编辑需求失败");
            }
        } else {
            // 无字段变更，视为幂等成功
            log.info("需求编辑无实际变更，ID：{}", id);
        }

        // 处理标签更新（先删后增）
        if (request.getTags() != null) {
            // 删除旧标签关联（逻辑删除）
            LambdaUpdateWrapper<DemandTag> deleteWrapper = new LambdaUpdateWrapper<>();
            deleteWrapper.eq(DemandTag::getDemandId, id)
                    .eq(DemandTag::getDeleted, DateConstants.getNotDeletedTime());
            demandTagMapper.delete(deleteWrapper);

            // 插入新标签
            if (!CollectionUtils.isEmpty(request.getTags())) {
                // 先对标签 ID 进行空值过滤与去重，避免校验误判和唯一键冲突
                List<Long> distinctTagIds = request.getTags().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                // 去重后如果没有有效标签，则无需继续校验和插入
                if (!distinctTagIds.isEmpty()) {
                    // 校验标签是否存在（基于去重后的标签 ID）
                    List<Tag> tags = tagService.listByIds(distinctTagIds);
                    if (tags.size() != distinctTagIds.size()) {
                        Set<Long> existingIds = tags.stream()
                                .map(Tag::getId)
                                .collect(Collectors.toSet());
                        List<Long> missingIds = distinctTagIds.stream()
                                .filter(tagId -> !existingIds.contains(tagId))
                                .collect(Collectors.toList());
                        throw new BusinessException(400, "以下标签 ID 不存在: " + missingIds);
                    }
                    // 基于去重后的标签 ID 构造需求-标签关联，避免唯一键冲突
                    List<DemandTag> demandTags = distinctTagIds.stream()
                            .map(tagId -> new DemandTag()
                                    .setDemandId(id)
                                    .setTagId(tagId)
                                    .setDeleted(DateConstants.getNotDeletedTime()))
                            .collect(Collectors.toList());
                    demandTagMapper.insertBatch(demandTags);
                }
            }
        }

        log.info("需求编辑成功，ID：{}", id);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Description: 逻辑删除需求（级联删除关联标签）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemand(Long id, Long userId) {
        Demand demand = this.getById(id);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 权限校验：如果是 admin 则跳过企业归属检查
        if (!securityUtils.isAdmin()) {
            Manufacture manufacture = manufactureMapper.selectById(demand.getManuId());
            if (manufacture == null || !manufacture.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权操作此需求");
            }
        }

        String status = demand.getStatus();
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException(400, "当前状态不可删除");
        }

        // 逻辑删除关联标签（只删除未删除的记录）
        LambdaUpdateWrapper<DemandTag> tagWrapper = new LambdaUpdateWrapper<>();
        tagWrapper.eq(DemandTag::getDemandId, id)
                .eq(DemandTag::getDeleted, DateConstants.getNotDeletedTime());
        boolean deletedTags = demandTagMapper.delete(tagWrapper) > 0;
        if (deletedTags) {
            log.info("已逻辑删除需求 {} 的关联标签", id);
        }

        // 逻辑删除需求本身
        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除需求失败");
        }

        log.info("需求删除成功，ID：{}", id);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/24
     * @Description: 分页获取我的需求列表
     */
    @Override
    public PageResult<DemandMyListVO> getMyDemandList(Integer page, Integer size, Long manuId, String status, Long userId) {
        Manufacture manufacture = manufactureMapper.selectById(manuId);
        if (manufacture == null) {
            throw new BusinessException(404, "制造企业不存在");
        }

        // 权限校验：如果不是 admin，则必须为企业所属用户
        if (!securityUtils.isAdmin() && !manufacture.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看其他企业的需求");
        }

        Page<Demand> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Demand::getManuId, manuId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Demand::getStatus, status);
        }
        wrapper.eq(Demand::getDeleted, DateConstants.getNotDeletedTime());
        wrapper.orderByDesc(Demand::getCreateTime);

        Page<Demand> demandPage = this.page(mpPage, wrapper);
        List<Demand> demands = demandPage.getRecords();

        if (demands.isEmpty()) {
            return PageResult.from(demandPage.convert(d -> null));
        }

        List<Long> demandIds = demands.stream().map(Demand::getId).collect(Collectors.toList());
        List<DemandTag> demandTags = demandTagMapper.selectList(
                new LambdaQueryWrapper<DemandTag>()
                        .in(DemandTag::getDemandId, demandIds)
                        .eq(DemandTag::getDeleted, DateConstants.getNotDeletedTime())
        );

        List<Long> tagIds = demandTags.stream().map(DemandTag::getTagId).distinct().collect(Collectors.toList());
        final Map<Long, String> tagIdToNameMap = tagIds.isEmpty() ?
                Collections.emptyMap() :
                tagService.listByIds(tagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getName));

        Map<Long, List<DemandMyListVO.TagSimpleVO>> demandTagsMap = demandTags.stream()
                .map(dt -> {
                    String tagName = tagIdToNameMap.get(dt.getTagId());
                    if (tagName == null) {
                        log.warn("标签 ID {} 已不存在或被删除，需求 ID {} 的标签将被忽略", dt.getTagId(), dt.getDemandId());
                        return null;
                    }
                    DemandMyListVO.TagSimpleVO tagVO = new DemandMyListVO.TagSimpleVO();
                    tagVO.setId(dt.getTagId());
                    tagVO.setName(tagName);
                    return new AbstractMap.SimpleEntry<>(dt.getDemandId(), tagVO);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<DemandMyListVO> records = demands.stream().map(demand -> {
            DemandMyListVO vo = new DemandMyListVO();
            vo.setId(demand.getId());
            vo.setTitle(demand.getTitle());
            vo.setDescription(demand.getDescription());
            vo.setExpectedBudget(demand.getExpectedBudget());
            vo.setDeadline(demand.getDeadline());
            vo.setStatus(demand.getStatus());
            vo.setCreateTime(demand.getCreateTime());
            vo.setMatchedServiceProvider(null);
            vo.setTags(demandTagsMap.getOrDefault(demand.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        Page<DemandMyListVO> resultPage = new Page<>(demandPage.getCurrent(), demandPage.getSize(), demandPage.getTotal());
        resultPage.setRecords(records);
        return PageResult.from(resultPage);
    }
}