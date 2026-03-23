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

import java.util.Date;
import java.util.List;
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
            // 根据名称查询标签
            List<Tag> tags = tagService.lambdaQuery()
                    .in(Tag::getName, request.getTags())
                    .list();

            // 检查是否所有标签都存在
            if (tags.size() != request.getTags().size()) {
                List<String> foundNames = tags.stream().map(Tag::getName).collect(Collectors.toList());
                List<String> missingNames = request.getTags().stream()
                        .filter(name -> !foundNames.contains(name))
                        .collect(Collectors.toList());
                throw new BusinessException(400, "以下标签不存在: " + String.join(", ", missingNames));
            }

            // 提取标签 ID
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
     * @Description: 分页查询待审核需求，关联企业名称和标签信息
     */
    @Override
    public PageResult<DemandPendingVO> getPendingDemandList(Integer page, Integer size) {
        Page<DemandPendingVO> mpPage = new Page<>(page, size);
        IPage<DemandPendingVO> voPage = baseMapper.selectPendingDemandPage(mpPage);
        return PageResult.from(voPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: demandId 需求ID
     * @Param: request 审核请求参数（状态、意见）
     * @Param: adminUserId 当前管理员用户ID
     * @Return: void
     * @Description: 审核需求，更新审核状态、审核意见、审核时间和审核人，通过时同时更新业务状态为已发布
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
            demand.setAuditStatus("approved");
            demand.setStatus("published");  // 审核通过后变为已发布
        } else if ("rejected".equals(status)) {
            demand.setAuditStatus("rejected");
            demand.setStatus("draft");      // 驳回后保持草稿
            // 驳回时建议填写审核意见
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException(400, "驳回时必须填写审核意见");
            }
        } else {
            throw new BusinessException(400, "审核状态不合法");
        }

        demand.setAuditRemark(request.getRemark());
        demand.setAuditTime(new Date());
        demand.setAuditUserId(adminUserId);

        boolean updated = this.updateById(demand);
        if (!updated) {
            throw new BusinessException(500, "审核操作失败");
        }

        log.info("需求审核完成，ID：{}，结果：{}，审核人：{}", demandId, status, adminUserId);
    }
}