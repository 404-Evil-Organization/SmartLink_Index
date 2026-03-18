package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ServiceProviderUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderAddVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderDetailVO;
import com.zhilian.zhilianbackend.dto.response.ServiceProviderListVO;
import com.zhilian.zhilianbackend.dto.response.UserInfoResponse;
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.entity.ServiceTag;
import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.mapper.ServiceTagMapper;
import com.zhilian.zhilianbackend.mapper.TagMapper;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import com.zhilian.zhilianbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-13 01:01
 * @Param:
 * @Return:
 * @Description: 服务商 Service 实现类
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceProviderServiceImpl extends ServiceImpl<ServiceProviderMapper, ServiceProvider> implements ServiceProviderService {

    private final TagMapper tagMapper;
    private final ServiceTagMapper serviceTagMapper;
    private final UserService userService;
    private static final Timestamp NOT_DELETED = Timestamp.valueOf("1970-01-01 00:00:00");

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: requestDTO 查询请求参数
     * @Return: IPage<ServiceProviderListVO> 分页结果
     * @Description: 分页查询服务商列表
     **/
    @Override
    public IPage<ServiceProviderListVO> getServiceProviderList(ServiceProviderListRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        Integer pageNum = requestDTO.getPage();
        Integer pageSize = requestDTO.getSize();
        if (pageNum == null || pageNum < 1) {
            throw new BusinessException(400, "分页参数 page 不合法，必须从 1 开始");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "分页参数 size 不合法，取值范围为 1-100");
        }

        Page<ServiceProvider> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            queryWrapper.eq(ServiceProvider::getRegion, requestDTO.getRegion());
        }
        if (StringUtils.isNotBlank(requestDTO.getServiceType())) {
            queryWrapper.like(ServiceProvider::getServiceType, requestDTO.getServiceType());
        }
        queryWrapper.eq(ServiceProvider::getAuditStatus, "approved");
        queryWrapper.orderByDesc(ServiceProvider::getCreateTime);

        Page<ServiceProvider> providerPage = this.page(page, queryWrapper);

        log.debug("分页查询结果：总记录数={}, 当前页记录数={}", providerPage.getTotal(), providerPage.getRecords().size());

        IPage<ServiceProviderListVO> resultPage = new Page<>(providerPage.getCurrent(), providerPage.getSize(), providerPage.getTotal());
        resultPage.setRecords(providerPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList()));

        return resultPage;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID
     * @Return: ServiceProviderDetailVO 服务商详情
     * @Description: 根据ID获取服务商详情
     **/
    @Override
    public ServiceProviderDetailVO getServiceProviderDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }

        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getId, id);

        ServiceProvider provider = this.getOne(queryWrapper);
        if (provider == null) {
            log.warn("服务商不存在或已删除，ID：{}", id);
            throw new BusinessException(404, "服务商不存在");
        }

        String auditStatus = provider.getAuditStatus();
        if (!"approved".equalsIgnoreCase(auditStatus)) {
            log.warn("服务商未审核通过，ID：{}，状态：{}", id, auditStatus);
            throw new BusinessException(403, "服务商未审核通过，暂不支持查看详情");
        }

        return convertToDetailVO(provider);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: requestDTO 新增服务商请求参数
     * @Return: ServiceProviderAddVO 新增结果（返回新ID）
     * @Description: 新增服务商（审核状态默认为 pending）
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceProviderAddVO addServiceProvider(ServiceProviderAddRequestDTO requestDTO) {
        validateAddRequest(requestDTO);

        if (requestDTO.getUserId() == null) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        // 使用 UserService 检查用户是否存在（不存在时会抛出 BusinessException）
        userService.getCurrentUser(requestDTO.getUserId());

        checkUserIdExists(requestDTO.getUserId(), null);
        checkCompanyNameExists(requestDTO.getCompanyName(), null);

        ServiceProvider provider = new ServiceProvider();
        BeanUtils.copyProperties(requestDTO, provider);
        provider.setAuditStatus("pending");
        provider.setDeleted(NOT_DELETED);

        boolean saved = this.save(provider);
        if (!saved) {
            throw new BusinessException(500, "新增服务商失败");
        }

        updateServiceTags(provider.getId(), requestDTO.getServiceType());

        log.info("服务商新增成功，ID：{}，企业名称：{}，审核状态：pending", provider.getId(), provider.getCompanyName());
        return new ServiceProviderAddVO(provider.getId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID, requestDTO 修改服务商请求参数, currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 修改服务商信息，需要校验操作权限
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceProvider(Long id, ServiceProviderUpdateRequestDTO requestDTO, Long currentUserId) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }
        if (currentUserId == null) {
            throw new BusinessException(403, "无法识别当前用户身份，禁止访问该接口");
        }

        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getId, id);

        ServiceProvider existingProvider = this.getOne(queryWrapper);
        if (existingProvider == null) {
            throw new BusinessException(404, "服务商不存在");
        }

        checkUpdatePermission(existingProvider, currentUserId);

        if (requestDTO.getCompanyName() != null && StringUtils.isBlank(requestDTO.getCompanyName())) {
            throw new BusinessException(400, "企业名称不能为空");
        }

        if (StringUtils.isNotBlank(requestDTO.getCompanyName()) &&
                !requestDTO.getCompanyName().equals(existingProvider.getCompanyName())) {
            checkCompanyNameExists(requestDTO.getCompanyName(), id);
        }

        validateUpdateFields(requestDTO);

        ServiceProvider updateProvider = new ServiceProvider();
        updateProvider.setId(id);
        copyNonNullProperties(requestDTO, updateProvider);

        boolean updated = this.updateById(updateProvider);
        if (!updated) {
            throw new BusinessException(500, "修改服务商信息失败");
        }

        if (requestDTO.getServiceType() != null) {
            updateServiceTags(id, requestDTO.getServiceType());
        }

        log.info("服务商修改成功，ID：{}，操作人ID：{}", id, currentUserId);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: id 服务商ID, currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 删除服务商（逻辑删除），需要校验操作权限
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceProvider(Long id, Long currentUserId) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }
        if (currentUserId == null) {
            throw new BusinessException(403, "无法识别当前用户身份，禁止访问该接口");
        }

        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getId, id);

        ServiceProvider provider = this.getOne(queryWrapper);
        if (provider == null) {
            throw new BusinessException(404, "服务商不存在");
        }

        checkDeletePermission(provider, currentUserId);

        // 逻辑删除关联标签
        LambdaQueryWrapper<ServiceTag> deleteTagRelWrapper = new LambdaQueryWrapper<>();
        deleteTagRelWrapper.eq(ServiceTag::getServiceId, id);
        serviceTagMapper.delete(deleteTagRelWrapper);

        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除服务商失败");
        }

        log.info("服务商删除成功，ID：{}，企业名称：{}，操作人ID：{}", id, provider.getCompanyName(), currentUserId);
    }

    // ==================== 处理服务类型标签 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: serviceId 服务商ID, serviceType 服务类型字符串（逗号分隔）
     * @Return: void
     * @Description: 根据服务类型字符串更新服务商与标签的关联关系，先逻辑删除所有现有关联，再将新的标签列表批量插入（使用 ON DUPLICATE KEY UPDATE）
     **/
    private void updateServiceTags(Long serviceId, String serviceType) {
        // 1. 逻辑删除当前所有关联
        LambdaQueryWrapper<ServiceTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ServiceTag::getServiceId, serviceId);
        serviceTagMapper.delete(deleteWrapper);

        if (StringUtils.isBlank(serviceType)) {
            return;
        }

        // 2. 解析标签名称，获取或创建对应的标签ID
        String[] tagNames = serviceType.split("\\s*,\\s*");
        List<ServiceTag> tagList = new ArrayList<>();
        Set<Long> handledTagIds = new HashSet<>();

        for (String tagName : tagNames) {
            if (StringUtils.isBlank(tagName)) {
                continue;
            }
            Long tagId = getOrCreateTag(tagName.trim(), "service");
            if (handledTagIds.contains(tagId)) {
                continue;
            }
            handledTagIds.add(tagId);

            ServiceTag serviceTag = new ServiceTag();
            serviceTag.setServiceId(serviceId);
            serviceTag.setTagId(tagId);
            serviceTag.setDeleted(NOT_DELETED);
            tagList.add(serviceTag);
        }

        // 3. 批量插入/更新（存在则更新时间，不存在则插入）
        if (!tagList.isEmpty()) {
            serviceTagMapper.insertOrUpdateBatch(tagList);
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: tagName 标签名称, category 标签类别（如 "service"）
     * @Return: Long 标签ID
     * @Description: 根据标签名称和类别获取标签ID，若标签不存在则插入新标签，处理并发冲突
     **/
    private Long getOrCreateTag(String tagName, String category) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, tagName)
                .eq(Tag::getCategory, category);

        Tag existingTag = tagMapper.selectOne(queryWrapper);
        if (existingTag != null) {
            return existingTag.getId();
        }

        Tag newTag = new Tag();
        newTag.setName(tagName);
        newTag.setCategory(category);
        newTag.setDeleted(NOT_DELETED);

        try {
            tagMapper.insert(newTag);
        } catch (DuplicateKeyException e) {
            // 并发冲突：其他线程已插入相同标签，重新查询并返回ID
            log.debug("并发插入标签 {} 冲突，重新查询", tagName);
            Tag conflictTag = tagMapper.selectOne(queryWrapper);
            if (conflictTag != null) {
                return conflictTag.getId();
            }
            // 理论上不应进入此分支，若进入说明严重异常
            throw new BusinessException(500, "标签处理失败，请稍后重试");
        } catch (Exception e) {
            // 其他数据库异常，记录日志并抛出
            log.error("插入标签失败，tagName={}, category={}", tagName, category, e);
            throw new BusinessException(500, "标签创建失败，请稍后重试");
        }
        return newTag.getId();
    }

    // ==================== 工具方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: source 源对象
     * @Return: String[] 值为null的属性名数组
     * @Description: 获取对象中值为null的属性名数组（支持父类字段）
     **/
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: source 源对象, target 目标对象
     * @Return: void
     * @Description: 复制非空属性（支持父类字段），使用 Spring BeanUtils 实现
     **/
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    // ==================== 权限检查 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: provider 服务商对象, currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 检查当前用户是否有权限修改服务商信息
     **/
    private void checkUpdatePermission(ServiceProvider provider, Long currentUserId) {
        UserInfoResponse currentUser = userService.getCurrentUser(currentUserId);
        String role = currentUser.getRole();

        if ("admin".equals(role)) {
            return;
        }

        if ("service".equals(role)) {
            if (!Objects.equals(provider.getUserId(), currentUserId)) {
                log.warn("服务商越权修改，服务商ID：{}，所属用户ID：{}，操作人ID：{}",
                        provider.getId(), provider.getUserId(), currentUserId);
                throw new BusinessException(403, "无权修改其他服务商的信息");
            }
            return;
        }

        throw new BusinessException(403, "当前角色无权修改服务商信息");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: provider 服务商对象, currentUserId 当前操作用户ID
     * @Return: void
     * @Description: 检查当前用户是否有权限删除服务商信息
     **/
    private void checkDeletePermission(ServiceProvider provider, Long currentUserId) {
        UserInfoResponse currentUser = userService.getCurrentUser(currentUserId);
        String role = currentUser.getRole();

        if ("admin".equals(role)) {
            return;
        }

        if ("service".equals(role)) {
            if (!Objects.equals(provider.getUserId(), currentUserId)) {
                log.warn("服务商越权删除，服务商ID：{}，所属用户ID：{}，操作人ID：{}",
                        provider.getId(), provider.getUserId(), currentUserId);
                throw new BusinessException(403, "无权删除其他服务商的信息");
            }
            return;
        }

        throw new BusinessException(403, "当前角色无权删除服务商信息");
    }

    // ==================== 参数校验 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: requestDTO 新增服务商请求参数
     * @Return: void
     * @Description: 校验新增服务商请求参数
     **/
    private void validateAddRequest(ServiceProviderAddRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }

        if (StringUtils.isBlank(requestDTO.getCompanyName())) {
            throw new BusinessException(400, "企业名称不能为空");
        }

        if (StringUtils.length(requestDTO.getCompanyName()) > 100) {
            throw new BusinessException(400, "企业名称不能超过100个字符");
        }

        if (StringUtils.isNotBlank(requestDTO.getWebsite())) {
            if (!requestDTO.getWebsite().matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$") &&
                    !requestDTO.getWebsite().matches("^(http|https)://.*$")) {
                throw new BusinessException(400, "网址格式不正确");
            }
        }

        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }

        if (requestDTO.getEstablishedDate() != null &&
                requestDTO.getEstablishedDate().after(new Date())) {
            throw new BusinessException(400, "成立日期不能是未来日期");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: requestDTO 修改服务商请求参数
     * @Return: void
     * @Description: 校验修改服务商请求参数
     **/
    private void validateUpdateFields(ServiceProviderUpdateRequestDTO requestDTO) {
        if (StringUtils.isNotBlank(requestDTO.getCompanyName()) &&
                StringUtils.length(requestDTO.getCompanyName()) > 100) {
            throw new BusinessException(400, "企业名称不能超过100个字符");
        }

        if (StringUtils.isNotBlank(requestDTO.getWebsite())) {
            if (!requestDTO.getWebsite().matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$") &&
                    !requestDTO.getWebsite().matches("^(http|https)://.*$")) {
                throw new BusinessException(400, "网址格式不正确");
            }
        }

        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }

        if (requestDTO.getEstablishedDate() != null &&
                requestDTO.getEstablishedDate().after(new Date())) {
            throw new BusinessException(400, "成立日期不能是未来日期");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: companyName 企业名称, excludeId 排除的服务商ID
     * @Return: void
     * @Description: 检查企业名称是否已存在
     **/
    private void checkCompanyNameExists(String companyName, Long excludeId) {
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getCompanyName, companyName);

        if (excludeId != null) {
            queryWrapper.ne(ServiceProvider::getId, excludeId);
        }

        long count = this.count(queryWrapper);
        if (count > 0) {
            log.warn("企业名称已存在：{}", companyName);
            throw new BusinessException(409, "企业名称已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-17 01:00
     * @Param: userId 用户ID, excludeId 排除的服务商ID
     * @Return: void
     * @Description: 检查用户ID是否已被其他服务商关联
     **/
    private void checkUserIdExists(Long userId, Long excludeId) {
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getUserId, userId);

        if (excludeId != null) {
            queryWrapper.ne(ServiceProvider::getId, excludeId);
        }

        long count = this.count(queryWrapper);
        if (count > 0) {
            log.warn("用户ID已被使用：{}", userId);
            throw new BusinessException(409, "该用户已关联其他服务商");
        }
    }

    // ==================== 对象转换 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: provider 服务商实体
     * @Return: ServiceProviderListVO 列表返回对象
     * @Description: 将实体对象转换为列表返回对象
     **/
    private ServiceProviderListVO convertToListVO(ServiceProvider provider) {
        if (provider == null) {
            return null;
        }
        ServiceProviderListVO vo = new ServiceProviderListVO();
        BeanUtils.copyProperties(provider, vo);
        return vo;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:00
     * @Param: provider 服务商实体
     * @Return: ServiceProviderDetailVO 详情返回对象
     * @Description: 将实体对象转换为详情返回对象
     **/
    private ServiceProviderDetailVO convertToDetailVO(ServiceProvider provider) {
        if (provider == null) {
            return null;
        }
        ServiceProviderDetailVO vo = new ServiceProviderDetailVO();
        BeanUtils.copyProperties(provider, vo);
        return vo;
    }
}