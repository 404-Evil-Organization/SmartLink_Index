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
import com.zhilian.zhilianbackend.entity.ServiceProvider;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.ServiceProviderMapper;
import com.zhilian.zhilianbackend.service.ServiceProviderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
public class ServiceProviderServiceImpl extends ServiceImpl<ServiceProviderMapper, ServiceProvider> implements ServiceProviderService {

    @Override
    public IPage<ServiceProviderListVO> getServiceProviderList(ServiceProviderListRequestDTO requestDTO) {
        // 基础参数校验，防止空指针和异常分页参数导致资源消耗
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
        // 1. 构建分页对象
        Page<ServiceProvider> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();

        // 区域筛选（精确匹配）
        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            queryWrapper.eq(ServiceProvider::getRegion, requestDTO.getRegion());
        }

        // 服务大类筛选（模糊匹配，因为可能是逗号分隔的多选值）
        if (StringUtils.isNotBlank(requestDTO.getServiceType())) {
            queryWrapper.like(ServiceProvider::getServiceType, requestDTO.getServiceType());
        }

        // 仅返回审核通过的服务商
        queryWrapper.eq(ServiceProvider::getAuditStatus, "approved");

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ServiceProvider::getCreateTime);

        // 3. 执行分页查询（MyBatis-Plus会自动处理逻辑删除条件）
        Page<ServiceProvider> providerPage = this.page(page, queryWrapper);

        // 4. 转换为返回对象
        IPage<ServiceProviderListVO> resultPage = new Page<>(providerPage.getCurrent(), providerPage.getSize(), providerPage.getTotal());
        resultPage.setRecords(providerPage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList()));

        return resultPage;
    }

    @Override
    public ServiceProviderDetailVO getServiceProviderDetail(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }

        // 2. 查询服务商信息
        ServiceProvider provider = this.getById(id);

        // 3. 检查是否存在
        if (provider == null) {
            throw new BusinessException(404, "服务商不存在");
        }

        // 4. 审核状态校验：对外公开详情仅允许审核通过的服务商
        //    列表接口已经限制 audit_status=approved，这里保持一致，防止未审核/驳回的数据被直接通过 ID 暴露
        String auditStatus = provider.getAuditStatus();
        if (!"approved".equalsIgnoreCase(auditStatus)) {
            throw new BusinessException(403, "服务商未审核通过，暂不支持查看详情");
        }
        // 5. 检查是否已删除（虽然逻辑删除会自动过滤，但手动检查更安全）
        if (provider.getDeleted() != null) {
            throw new BusinessException(404, "服务商已删除");
        }

        // 6. 转换为返回对象
        return convertToDetailVO(provider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceProviderAddVO addServiceProvider(ServiceProviderAddRequestDTO requestDTO) {
        // 1. 参数校验
        validateAddRequest(requestDTO);

        // 2. 检查企业名称是否已存在
        checkCompanyNameExists(requestDTO.getCompanyName(), null);

        // 3. 检查用户ID是否已被使用（一个用户只能关联一个服务商）
        checkUserIdExists(requestDTO.getUserId(), null);

        // 4. 创建实体对象
        ServiceProvider provider = new ServiceProvider();
        BeanUtils.copyProperties(requestDTO, provider);

        // 设置默认审核状态
        provider.setAuditStatus("pending");

        // 5. 保存到数据库
        boolean saved = this.save(provider);
        if (!saved) {
            throw new BusinessException(500, "新增服务商失败");
        }

        log.info("服务商新增成功，ID：{}，企业名称：{}", provider.getId(), provider.getCompanyName());

        // 6. 返回新创建的服务商ID
        return new ServiceProviderAddVO(provider.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceProvider(Long id, ServiceProviderUpdateRequestDTO requestDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }

        // 2. 查询服务商是否存在
        ServiceProvider existingProvider = this.getById(id);
        if (existingProvider == null) {
            throw new BusinessException(404, "服务商不存在");
        }

        // 3. 检查是否已删除
        if (existingProvider.getDeleted() != null) {
            throw new BusinessException(404, "服务商已删除，无法修改");
        }
        // 4. 如果传入了企业名称但为空串，拒绝本次更新，避免把必填字段清空
        if (requestDTO.getCompanyName() != null && StringUtils.isBlank(requestDTO.getCompanyName())) {
            throw new BusinessException(400, "企业名称不能为空");
        }
        // 5. 如果修改了企业名称，检查新名称是否已存在
        if (StringUtils.isNotBlank(requestDTO.getCompanyName()) &&
                !requestDTO.getCompanyName().equals(existingProvider.getCompanyName())) {
            checkCompanyNameExists(requestDTO.getCompanyName(), id);
        }

        // 6. 校验其他字段（如果填写了）
        validateUpdateFields(requestDTO);

        // 6. 复制非空字段到实体对象
        ServiceProvider updateProvider = new ServiceProvider();
        updateProvider.setId(id);
        copyNonNullProperties(requestDTO, updateProvider);

        // 7. 执行更新
        boolean updated = this.updateById(updateProvider);
        if (!updated) {
            throw new BusinessException(500, "修改服务商信息失败");
        }

        log.info("服务商修改成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceProvider(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "服务商ID不能为空");
        }

        // 2. 查询服务商是否存在
        ServiceProvider provider = this.getById(id);
        if (provider == null) {
            throw new BusinessException(404, "服务商不存在");
        }

        // 3. 检查是否已删除
        if (provider.getDeleted() != null) {
            throw new BusinessException(404, "服务商已被删除，不能重复删除");
        }

        // 4. 执行逻辑删除（使用了 @TableLogic 注解，MyBatis-Plus 会自动将 deleted 字段更新为当前时间）
        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除服务商失败");
        }

        log.info("服务商删除成功，ID：{}，企业名称：{}", id, provider.getCompanyName());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: requestDTO 新增请求参数
     * @Return: void
     * @Description: 校验新增请求参数
     **/
    private void validateAddRequest(ServiceProviderAddRequestDTO requestDTO) {
        // 企业名称长度校验
        if (StringUtils.length(requestDTO.getCompanyName()) > 100) {
            throw new BusinessException(400, "企业名称不能超过100个字符");
        }

        // 网址格式校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getWebsite())) {
            if (!requestDTO.getWebsite().matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$") &&
                    !requestDTO.getWebsite().matches("^(http|https)://.*$")) {
                throw new BusinessException(400, "网址格式不正确");
            }
        }

        // 员工人数不能超过合理范围
        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }

        // 成立日期不能是未来日期
        if (requestDTO.getEstablishedDate() != null &&
                requestDTO.getEstablishedDate().after(new Date())) {
            throw new BusinessException(400, "成立日期不能是未来日期");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: requestDTO 修改请求参数
     * @Return: void
     * @Description: 校验修改请求的字段
     **/
    private void validateUpdateFields(ServiceProviderUpdateRequestDTO requestDTO) {
        // 企业名称长度校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getCompanyName()) &&
                StringUtils.length(requestDTO.getCompanyName()) > 100) {
            throw new BusinessException(400, "企业名称不能超过100个字符");
        }

        // 网址格式校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getWebsite())) {
            if (!requestDTO.getWebsite().matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$") &&
                    !requestDTO.getWebsite().matches("^(http|https)://.*$")) {
                throw new BusinessException(400, "网址格式不正确");
            }
        }

        // 员工人数不能超过合理范围（如果填写了）
        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }

        // 成立日期不能是未来日期（如果填写了）
        if (requestDTO.getEstablishedDate() != null &&
                requestDTO.getEstablishedDate().after(new Date())) {
            throw new BusinessException(400, "成立日期不能是未来日期");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: companyName 企业名称
     * @Param: excludeId 排除的服务商ID（更新时使用）
     * @Return: void
     * @Description: 检查企业名称是否已存在
     **/
    private void checkCompanyNameExists(String companyName, Long excludeId) {
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getCompanyName, companyName);

        // 如果是更新操作，排除当前服务商
        if (excludeId != null) {
            queryWrapper.ne(ServiceProvider::getId, excludeId);
        }

        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(409, "企业名称已存在");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: userId 用户ID
     * @Param: excludeId 排除的服务商ID（更新时使用）
     * @Return: void
     * @Description: 检查用户ID是否已被其他服务商使用
     **/
    private void checkUserIdExists(Long userId, Long excludeId) {
        LambdaQueryWrapper<ServiceProvider> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceProvider::getUserId, userId);

        // 如果是更新操作，排除当前服务商
        if (excludeId != null) {
            queryWrapper.ne(ServiceProvider::getId, excludeId);
        }

        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(409, "该用户已关联其他服务商");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: source 源对象
     * @Param: target 目标对象
     * @Return: void
     * @Description: 复制非空属性（使用反射手动实现）- 改进版异常处理
     **/
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            log.warn("源对象或目标对象为null，source={}, target={}", source, target);
            return;
        }

        // 获取源对象的所有属性
        java.lang.reflect.Field[] fields = source.getClass().getDeclaredFields();

        for (java.lang.reflect.Field field : fields) {
            try {
                // 设置可访问
                field.setAccessible(true);

                // 获取源对象的值
                Object value = field.get(source);

                // 如果值不为null，则复制到目标对象
                if (value != null) {
                    try {
                        // 获取目标对象的对应字段
                        java.lang.reflect.Field targetField = target.getClass().getDeclaredField(field.getName());
                        targetField.setAccessible(true);
                        targetField.set(target, value);
                        log.debug("属性复制成功：{} = {}", field.getName(), value);
                    } catch (NoSuchFieldException e) {
                        // 目标对象不存在该字段，这是预期可能的情况，记录debug级别
                        log.debug("目标对象不存在字段：{}，跳过复制", field.getName());
                    } catch (IllegalAccessException e) {
                        // 字段访问权限问题，记录warn级别
                        log.warn("复制属性失败，无法访问目标字段：{}，错误：{}", field.getName(), e.getMessage(), e);
                    }
                }
            } catch (IllegalAccessException e) {
                // 源字段访问失败，记录error级别
                log.error("复制属性失败，无法访问源字段：{}，错误：{}", field.getName(), e.getMessage(), e);
            } catch (Exception e) {
                // 其他未知异常，记录error级别
                log.error("复制属性时发生未知异常，字段：{}，错误：{}", field.getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-13 01:01
     * @Param: provider 实体对象
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
     * @Date: 2026-03-13 01:01
     * @Param: provider 实体对象
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