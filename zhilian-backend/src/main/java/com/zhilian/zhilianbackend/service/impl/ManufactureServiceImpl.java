package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.request.ManufactureAddRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureListRequestDTO;
import com.zhilian.zhilianbackend.dto.request.ManufactureUpdateRequestDTO;
import com.zhilian.zhilianbackend.dto.response.ManufactureAddVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO;
import com.zhilian.zhilianbackend.dto.response.ManufactureListVO;
import com.zhilian.zhilianbackend.entity.Manufacture;
import com.zhilian.zhilianbackend.entity.ManufactureTag;
import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.ManufactureService;
import com.zhilian.zhilianbackend.service.ManufactureTagService;
import com.zhilian.zhilianbackend.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/13 20:26
 * @Param:
 * @Return:
 * @Description: 制造企业 Service 实现类
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ManufactureServiceImpl extends ServiceImpl<ManufactureMapper, Manufacture> implements ManufactureService {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String SCALE_REGEX = "micro|small|medium|large";
    private static final BigDecimal MAX_ANNUAL_REVENUE = new BigDecimal("1000000");
    private static final int MAX_EMPLOYEE_COUNT = 100000;

    private final TagService tagService;
    private final ManufactureTagService manufactureTagService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: param 需要转义的字符串
     * @Return: java.lang.String 转义后的字符串
     * @Description: 对进行 SQL LIKE 查询的入参进行通配符转义，避免用户输入 % 或 _ 被数据库当作通配符使用，导致查询结果范围异常放大。
     **/
    private String escapeSqlLike(String param) {
        if (StringUtils.isBlank(param)) {
            return param;
        }
        return param.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: requestDTO 查询请求参数
     * @Return: com.baomidou.mybatisplus.core.metadata.IPage<com.zhilian.zhilianbackend.dto.response.ManufactureListVO> 分页结果
     * @Description: 分页查询制造企业列表
     **/
    @Override
    public IPage<ManufactureListVO> getManufactureList(ManufactureListRequestDTO requestDTO) {
        Page<Manufacture> page = new Page<>(requestDTO.getPage(), requestDTO.getSize());

        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            queryWrapper.eq(Manufacture::getRegion, requestDTO.getRegion());
        }
        if (StringUtils.isNotBlank(requestDTO.getScale())) {
            queryWrapper.eq(Manufacture::getScale, requestDTO.getScale());
        }
        if (StringUtils.isNotBlank(requestDTO.getProductType())) {
            String escaped = escapeSqlLike(requestDTO.getProductType());
            // 使用 ESCAPE '\\' 显式指定反斜杠为 LIKE 转义字符，提升跨数据库兼容性
            queryWrapper.apply("product_type LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escaped);
        }
        queryWrapper.eq(Manufacture::getAuditStatus, "approved")
                .orderByDesc(Manufacture::getCreateTime);

        Page<Manufacture> manufacturePage = this.page(page, queryWrapper);

        IPage<ManufactureListVO> resultPage = new Page<>(manufacturePage.getCurrent(), manufacturePage.getSize(), manufacturePage.getTotal());
        resultPage.setRecords(manufacturePage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList()));
        return resultPage;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO 企业详情
     * @Description: 根据ID获取制造企业详情
     **/
    @Override
    public ManufactureDetailVO getManufactureDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }
        Manufacture manufacture = this.getById(id);
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }
        // ========== 权限与审核状态校验 ==========
        // 当前登录用户ID（可能为 null，表示未登录）
        Long currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception ex) {
            // 这里不抛出异常，未登录用户也可以访问已审核通过的企业
            log.debug("获取当前用户ID失败，视为未登录访问企业详情", ex);
        }
        String auditStatus = manufacture.getAuditStatus();
        boolean approved = StringUtils.equalsIgnoreCase("approved", auditStatus);
        boolean isOwner = currentUserId != null && currentUserId.equals(manufacture.getUserId());
        boolean admin = isAdmin();
        // 非本企业用户且非管理员时，仅允许访问审核通过的企业详情
        if (!approved && !isOwner && !admin) {
            throw new BusinessException(403, "暂无权限查看该企业详情");
        }

        return convertToDetailVO(manufacture);
    }
    /**
     * 判断当前登录用户是否为管理员
     *
     * @return true 表示管理员，false 表示非管理员或未登录
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: requestDTO 新增企业请求参数
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureAddVO 新增结果（返回新ID）
     * @Description: 新增制造企业
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManufactureAddVO addManufacture(ManufactureAddRequestDTO requestDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        validateManufactureData(requestDTO.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                null);

        checkUserHasManufacture(userId);

        Manufacture manufacture = new Manufacture();
        BeanUtils.copyProperties(requestDTO, manufacture);
        manufacture.setUserId(userId);
        manufacture.setAuditStatus("pending");

        boolean saved = this.save(manufacture);
        if (!saved) {
            throw new BusinessException(500, "新增企业失败");
        }

        // ========== 新增标签关联 ==========
        if (StringUtils.isNotBlank(requestDTO.getProductType())) {
            createTagAssociations(requestDTO.getProductType(), manufacture.getId());
        }

        log.info("企业新增成功，ID：{}，企业名称：{}", manufacture.getId(), manufacture.getCompanyName());
        return new ManufactureAddVO(manufacture.getId());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID, requestDTO 修改企业请求参数
     * @Return: void
     * @Description: 修改制造企业信息
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManufacture(Long id, ManufactureUpdateRequestDTO requestDTO) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        Manufacture existingManufacture = this.getById(id);
        if (existingManufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        checkPermission(existingManufacture.getUserId());

        validateManufactureData(
                requestDTO.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                id
        );

        Manufacture updateManufacture = new Manufacture();
        updateManufacture.setId(id);
        copyNonNullProperties(requestDTO, updateManufacture);

        updateManufacture.setAuditStatus("pending");

        boolean updated = this.updateById(updateManufacture);
        if (!updated) {
            throw new BusinessException(500, "修改企业信息失败");
        }

        // ========== 处理标签变更 ==========
        if (requestDTO.getProductType() != null) {
            // 先删除所有旧关联
            deleteTagAssociations(id);
            // 再根据新的 productType 创建关联（可能为空串，此时不会插入任何记录）
            createTagAssociations(requestDTO.getProductType(), id);
        }

        log.info("企业更新成功，ID：{}", id);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: id 企业ID
     * @Return: void
     * @Description: 删除制造企业（逻辑删除）
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteManufacture(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        Manufacture manufacture = this.getById(id);
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        checkPermission(manufacture.getUserId());

        // ========== 先逻辑删除标签关联 ==========
        deleteTagAssociations(id);

        // ========== 再逻辑删除企业自身 ==========
        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除企业失败");
        }

        log.info("企业删除成功，ID：{}，企业名称：{}", id, manufacture.getCompanyName());
    }

    // ==================== 标签处理私有方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: manufactureId 制造企业ID
     * @Return: void
     * @Description: 逻辑删除指定企业的所有标签关联
     **/
    private void deleteTagAssociations(Long manufactureId) {
        manufactureTagService.lambdaUpdate()
                .eq(ManufactureTag::getManufactureId, manufactureId)
                .remove();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: productType 逗号分隔的标签名, manufactureId 制造企业ID
     * @Return: void
     * @Description: 根据逗号分隔的 productType 字符串创建标签关联（批量查询/批量插入，避免 N+1 查询）
     **/
    private void createTagAssociations(String productType, Long manufactureId) {
        if (StringUtils.isBlank(productType)) {
            return;
        }

        // 解析标签名（去重、去空格）
        Set<String> tagNames = Arrays.stream(productType.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        if (tagNames.isEmpty()) {
            return;
        }

        // 1. 一次性批量查询已存在的标签（category 固定为 product）
        List<Tag> existingTags = tagService.lambdaQuery()
                .in(Tag::getName, tagNames)
                .eq(Tag::getCategory, "product")
                .list();
        Map<String, Long> nameToId = new HashMap<>();
        if (existingTags != null && !existingTags.isEmpty()) {
            for (Tag tag : existingTags) {
                nameToId.put(tag.getName(), tag.getId());
            }
        }
        // 2. 计算还不存在的标签名
        Set<String> missingNames = new HashSet<>(tagNames);
        missingNames.removeAll(nameToId.keySet());
        // 3. 对不存在的标签批量插入，并在并发场景下通过捕获 DuplicateKeyException 实现幂等
        if (!missingNames.isEmpty()) {
            List<Tag> newTags = new ArrayList<>(missingNames.size());
            for (String name : missingNames) {
                Tag newTag = new Tag();
                newTag.setName(name);
                newTag.setCategory("product");
                // description 可留空
                newTags.add(newTag);
            }
            try {
                // 直接批量插入新标签；如遇唯一键冲突，说明有并发请求已插入相同标签
                tagService.saveBatch(newTags);
            } catch (DuplicateKeyException e) {
                // 并发下的唯一键竞争视为正常业务场景，记录告警日志后继续流程
                log.warn("并发插入标签时出现唯一键冲突，将忽略本次冲突并重新查询标签。tagNames={}", missingNames, e);
            }
        }
        // 3.1 为了应对并发下的唯一键竞争，这里统一重新查询一次所有标签，确保拿到最新的 ID
        List<Tag> finalTags = tagService.list(
                new LambdaQueryWrapper<Tag>()
                        .in(Tag::getName, tagNames)
        );
        nameToId.clear();
        if (finalTags != null && !finalTags.isEmpty()) {
            for (Tag tag : finalTags) {
                nameToId.put(tag.getName(), tag.getId());
            }
        }
        // 4. 构造最终的 tagId 列表，如果有标签仍未获取到 ID，则认为处理失败
        List<Long> tagIds = tagNames.stream()
                .map(name -> {
                    Long id = nameToId.get(name);
                    if (id == null) {
                        throw new BusinessException(500, "标签处理失败，请稍后重试");
                    }
                    return id;
                })
                .collect(Collectors.toList());

        // 5.批量插入 manufacture_tag
        List<ManufactureTag> manufactureTags = tagIds.stream()
                .map(tagId -> new ManufactureTag().setManufactureId(manufactureId).setTagId(tagId))
                .collect(Collectors.toList());

        manufactureTagService.saveBatch(manufactureTags);
    }

    // ==================== 原有私有方法 ====================

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: targetUserId 目标数据所属用户ID
     * @Return: void
     * @Description: 检查当前用户是否有权限操作目标数据
     **/
    private void checkPermission(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        if (!currentUserId.equals(targetUserId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null &&
                    auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                return;
            }
            throw new BusinessException(403, "无权操作他人数据");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param:
     * @Return: java.lang.Long 当前登录用户ID
     * @Description: 获取当前登录用户ID
     **/
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                log.error("无法从 UserDetails 的 username 解析 userId: {}", username);
                return null;
            }
        }
        return null;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: userId 用户ID
     * @Return: void
     * @Description: 检查用户是否已创建过企业
     **/
    private void checkUserHasManufacture(Long userId) {
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manufacture::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(409, "该用户已创建过制造企业，不可重复创建");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: companyName 企业名称, contactPhone 联系电话, scale 规模, annualRevenue 年营收, employeeCount 员工人数, excludeId 排除的企业ID（用于修改）
     * @Return: void
     * @Description: 统一的企业数据校验方法
     **/
    private void validateManufactureData(String companyName, String contactPhone, String scale,
                                         BigDecimal annualRevenue, Integer employeeCount, Long excludeId) {
        if (excludeId == null) {
            if (StringUtils.isBlank(companyName)) {
                throw new BusinessException(400, "企业名称不能为空");
            }
            checkCompanyNameExists(companyName, null);
        } else {
            if (StringUtils.isNotBlank(companyName)) {
                checkCompanyNameExists(companyName, excludeId);
            }
        }
        if (StringUtils.isNotBlank(contactPhone) && !contactPhone.matches(PHONE_REGEX)) {
            throw new BusinessException(400, "联系电话格式不正确，应为11位手机号");
        }
        if (StringUtils.isNotBlank(scale) && !scale.matches(SCALE_REGEX)) {
            throw new BusinessException(400, "规模值不正确，应为：micro/small/medium/large");
        }
        if (annualRevenue != null) {
            if (annualRevenue.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "年营收不能为负数");
            }
            if (annualRevenue.compareTo(MAX_ANNUAL_REVENUE) > 0) {
                throw new BusinessException(400, "年营收不能超过100亿元");
            }
        }
        if (employeeCount != null) {
            if (employeeCount < 0) {
                throw new BusinessException(400, "员工人数不能为负数");
            }
            if (employeeCount > MAX_EMPLOYEE_COUNT) {
                throw new BusinessException(400, "员工人数不能超过10万");
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: companyName 企业名称, excludeId 排除的企业ID
     * @Return: void
     * @Description: 检查企业名称是否已存在（逻辑删除的记录不计入）
     **/
    private void checkCompanyNameExists(String companyName, Long excludeId) {
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manufacture::getCompanyName, companyName);
        if (excludeId != null) {
            queryWrapper.ne(Manufacture::getId, excludeId);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(409, "企业名称已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: source 源对象, target 目标对象
     * @Return: void
     * @Description: 复制非空属性（支持父类字段）
     **/
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: source 源对象
     * @Return: java.lang.String[] 值为null的属性名数组
     * @Description: 获取对象中值为null的属性名数组
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
     * @Date: 2026-03-12 23:32
     * @Param: manufacture 制造企业实体
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureListVO 列表返回对象
     * @Description: 将实体对象转换为列表返回对象
     **/
    private ManufactureListVO convertToListVO(Manufacture manufacture) {
        if (manufacture == null) {
            return null;
        }
        ManufactureListVO vo = new ManufactureListVO();
        BeanUtils.copyProperties(manufacture, vo);
        return vo;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:32
     * @Param: manufacture 制造企业实体
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO 详情返回对象
     * @Description: 将实体对象转换为详情返回对象
     **/
    private ManufactureDetailVO convertToDetailVO(Manufacture manufacture) {
        if (manufacture == null) {
            return null;
        }
        ManufactureDetailVO vo = new ManufactureDetailVO();
        BeanUtils.copyProperties(manufacture, vo);
        return vo;
    }
}