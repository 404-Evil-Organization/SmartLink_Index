package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseCreateRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseDetailResponse;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseListResponse;
import com.zhilian.zhilianbackend.entity.AbroadCase;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.AbroadCaseMapper;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import com.zhilian.zhilianbackend.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * @Description: 出海案例Service实现类
 * @Author: 6017
 * @Date: 2026/3/25
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AbroadCaseServiceImpl implements AbroadCaseService {

    private final AbroadCaseMapper abroadCaseMapper;
    private final OssService ossService;

    /**
     * 有效的状态值集合
     */
    private static final Byte STATUS_DRAFT = 0;
    private static final Byte STATUS_PUBLISHED = 1;

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:05
     * @Param: queryRequest 查询请求参数
     * @Return: Page<AbroadCaseListResponse> 分页案例列表
     * @Description: 分页查询出海案例列表（管理员）
     **/
    @Override
    public Page<AbroadCaseListResponse> listByPage(AbroadCaseQueryRequest queryRequest) {
        // 构建查询条件
        LambdaQueryWrapper<AbroadCase> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryRequest.getCountry())) {
            wrapper.eq(AbroadCase::getCountry, queryRequest.getCountry());
        }

        if (queryRequest.getStatus() != null) {
            // 校验查询状态值
            validateStatus(queryRequest.getStatus());
            wrapper.eq(AbroadCase::getStatus, queryRequest.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(AbroadCase::getCreateTime);

        // 分页查询
        Page<AbroadCase> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());
        Page<AbroadCase> resultPage = abroadCaseMapper.selectPage(page, wrapper);

        // 转换为Response
        Page<AbroadCaseListResponse> responsePage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        responsePage.setRecords(resultPage.getRecords().stream().map(entity -> {
            AbroadCaseListResponse response = new AbroadCaseListResponse();
            BeanUtils.copyProperties(entity, response);
            return response;
        }).collect(Collectors.toList()));

        return responsePage;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:05
     * @Param: id 案例ID
     * @Return: AbroadCaseDetailResponse 案例详情
     * @Description: 获取出海案例详情
     **/
    @Override
    public AbroadCaseDetailResponse getDetail(Long id) {
        AbroadCase entity = abroadCaseMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "出海案例不存在");
        }

        AbroadCaseDetailResponse response = new AbroadCaseDetailResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:05
     * @Param: request 创建请求参数
     * @Param: adminId 管理员ID
     * @Param: adminName 管理员名称
     * @Return: Long 新创建的案例ID
     * @Description: 新增出海案例
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AbroadCaseCreateRequest request, Long adminId, String adminName) {
        // 防御性校验：验证状态值
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
        }

        // 处理封面图片上传
        String coverImageUrl = null;
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            try {
                coverImageUrl = ossService.uploadFile(request.getCoverImageFile());
                log.info("上传出海案例封面成功: {}", coverImageUrl);
            } catch (Exception e) {
                log.error("上传封面图片失败", e);
                String safeMessage = StringUtils.hasText(e.getMessage()) ? e.getMessage() : "请稍后重试";
                throw new BusinessException(500, "封面图片上传失败: " + safeMessage);
            }
        }

        // 构建实体
        AbroadCase entity = new AbroadCase();
        BeanUtils.copyProperties(request, entity);
        entity.setCoverImage(coverImageUrl);
        // 仅当状态为“已发布”时设置发布时间，草稿不应有发布时间
        if (entity.getStatus() != null && entity.getStatus() == 1) {
            entity.setPublishTime(new Date());
        }

        // 保存
        int result = abroadCaseMapper.insert(entity);
        if (result <= 0) {
            throw new BusinessException(500, "创建出海案例失败");
        }

        // 记录操作日志（可选）
        log.info("管理员创建出海案例成功, adminId: {}, caseId: {}", adminId, entity.getId());

        return entity.getId();
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:05
     * @Param: id 案例ID
     * @Param: request 更新请求参数
     * @Return: void
     * @Description: 更新出海案例（支持部分字段更新）
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AbroadCaseUpdateRequest request) {
        // 查询原记录
        AbroadCase existing = abroadCaseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "出海案例不存在");
        }

        // 防御性校验：验证状态值
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
        }

        // 处理封面图片更新
        String coverImageUrl = existing.getCoverImage();
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            // 上传新图片
            try {
                coverImageUrl = ossService.uploadFile(request.getCoverImageFile());
                log.info("更新出海案例封面成功: {}", coverImageUrl);

                // 删除旧图片（可选，根据业务需求决定是否删除）
                if (StringUtils.hasText(existing.getCoverImage())) {
                    try {
                        ossService.deleteFile(existing.getCoverImage());
                        log.info("删除旧封面成功: {}", existing.getCoverImage());
                    } catch (Exception e) {
                        log.warn("删除旧封面失败: {}", existing.getCoverImage(), e);
                        // 删除失败不影响主流程
                    }
                }
            } catch (Exception e) {
                log.error("上传新封面图片失败", e);
                // 对外仅返回固定文案，避免将底层异常信息暴露给前端
                throw new BusinessException(500, "封面图片上传失败，请稍后重试");
            }
        }

        // 构建更新实体（只更新非空字段）
        AbroadCase entity = new AbroadCase();
        entity.setId(id);

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getCompanyName() != null) {
            entity.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyType() != null) {
            entity.setCompanyType(request.getCompanyType());
        }
        if (request.getCountry() != null) {
            entity.setCountry(request.getCountry());
        }
        if (request.getServiceType() != null) {
            entity.setServiceType(request.getServiceType());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
            // 只有发布时且原状态不是发布（包括原状态为 null）才更新发布时间
            Byte newStatus = request.getStatus();
            Byte oldStatus = existing.getStatus();
            if (newStatus != null
                    && newStatus.equals(STATUS_PUBLISHED)
                    && (oldStatus == null || !oldStatus.equals(STATUS_PUBLISHED))) {
                entity.setPublishTime(new Date());
            }
        }
        entity.setCoverImage(coverImageUrl);

        int result = abroadCaseMapper.updateById(entity);
        // 在已确认记录存在的前提下，result == 0 更可能表示“无字段变更”的幂等成功
        if (result == 0) {
            log.info("更新出海案例无字段变更, 视为幂等成功, caseId: {}", id);
            return;
        }

        log.info("更新出海案例成功, caseId: {}", id);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:05
     * @Param: id 案例ID
     * @Return: void
     * @Description: 逻辑删除出海案例
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 查询原记录，获取封面图URL（用于后续删除）
        AbroadCase existing = abroadCaseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "出海案例不存在");
        }

        // 逻辑删除
        int result = abroadCaseMapper.deleteById(id);
        if (result <= 0) {
            // 在已校验存在的前提下，delete 返回 0 更可能是并发场景下记录已被他人删除/逻辑删除，视为资源不存在
            throw new BusinessException(404, "出海案例不存在或已被删除");
        }

        // 删除OSS中的封面图片（可选，根据业务需求决定是否删除）
        if (StringUtils.hasText(existing.getCoverImage())) {
            try {
                ossService.deleteFile(existing.getCoverImage());
                log.info("删除案例封面成功: {}", existing.getCoverImage());
            } catch (Exception e) {
                log.warn("删除案例封面失败: {}", existing.getCoverImage(), e);
                // 删除失败不影响主流程
            }
        }

        log.info("删除出海案例成功, caseId: {}", id);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:40
     * @Param: status 状态值
     * @Return: void
     * @Description: 校验状态值是否有效（0-草稿，1-发布）
     **/
    private void validateStatus(Byte status) {
        if (status != null && status != STATUS_DRAFT && status != STATUS_PUBLISHED) {
            throw new BusinessException(400, "状态值无效，有效值为0(草稿)或1(发布)");
        }
    }
}