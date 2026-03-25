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
 * @Author: 6017
 * @Date: 2026/3/25 21:12
 * @Param: 
 * @Return: 
 * @Description: 出海案例Service实现类
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class AbroadCaseServiceImpl implements AbroadCaseService {

    private final AbroadCaseMapper abroadCaseMapper;
    private final OssService ossService;

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:10
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
     * @Date: 2026/3/25 21:12
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
     * @Date: 2026/3/25 21:12
     * @Param: request 创建请求参数  adminId 管理员ID  adminName 管理员名称
     * @Return: Long 新创建的案例ID
     * @Description: 新增出海案例
    **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AbroadCaseCreateRequest request, Long adminId, String adminName) {
        // 处理封面图片上传
        String coverImageUrl = null;
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            try {
                coverImageUrl = ossService.uploadFile(request.getCoverImageFile());
                log.info("上传出海案例封面成功: {}", coverImageUrl);
            } catch (Exception e) {
                log.error("上传封面图片失败", e);
                throw new BusinessException(500, "封面图片上传失败: " + e.getMessage());
            }
        }

        // 构建实体
        AbroadCase entity = new AbroadCase();
        BeanUtils.copyProperties(request, entity);
        entity.setCoverImage(coverImageUrl);
        entity.setPublishTime(new Date());

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
     * @Date: 2026/3/25 21:13
     * @Param: id 案例ID  request 更新请求参数
     * @Return: 
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
                throw new BusinessException(500, "封面图片上传失败: " + e.getMessage());
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
            // 只有发布时更新时间才更新发布时间
            if (request.getStatus() == 1 && existing.getStatus() != 1) {
                entity.setPublishTime(new Date());
            }
        }
        entity.setCoverImage(coverImageUrl);

        int result = abroadCaseMapper.updateById(entity);
        if (result <= 0) {
            throw new BusinessException(500, "更新出海案例失败");
        }

        log.info("更新出海案例成功, caseId: {}", id);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/25 21:13
     * @Param: id 案例ID
     * @Return: 
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
            throw new BusinessException(500, "删除出海案例失败");
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
}