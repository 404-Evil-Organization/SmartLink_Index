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
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.ManufactureMapper;
import com.zhilian.zhilianbackend.service.ManufactureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:31
 * @Description: 制造企业 Service 实现类
 **/
@Slf4j
@Service
public class ManufactureServiceImpl extends ServiceImpl<ManufactureMapper, Manufacture> implements ManufactureService {

    // ==================== 常量定义 ====================
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String SCALE_REGEX = "micro|small|medium|large";
    private static final BigDecimal MAX_ANNUAL_REVENUE = new BigDecimal("1000000"); // 100亿元
    private static final int MAX_EMPLOYEE_COUNT = 100000; // 10万人

    // ==================== 公共查询方法 ====================

    @Override
    public IPage<ManufactureListVO> getManufactureList(ManufactureListRequestDTO requestDTO) {
        // 1. 构建分页对象
        Page<Manufacture> page = new Page<>(requestDTO.getPage(), requestDTO.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();

        // 注意：@TableLogic 会自动添加 deleted IS NULL 条件，无需手动添加

        // 区域筛选（精确匹配）
        if (StringUtils.isNotBlank(requestDTO.getRegion())) {
            queryWrapper.eq(Manufacture::getRegion, requestDTO.getRegion());
        }

        // 规模筛选（精确匹配）
        if (StringUtils.isNotBlank(requestDTO.getScale())) {
            queryWrapper.eq(Manufacture::getScale, requestDTO.getScale());
        }

        // 主营产品类型模糊匹配
        if (StringUtils.isNotBlank(requestDTO.getProductType())) {
            queryWrapper.like(Manufacture::getProductType, requestDTO.getProductType());
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Manufacture::getCreateTime);

        // 3. 执行分页查询
        Page<Manufacture> manufacturePage = this.page(page, queryWrapper);

        // 4. 转换为返回对象
        IPage<ManufactureListVO> resultPage = new Page<>(manufacturePage.getCurrent(), manufacturePage.getSize(), manufacturePage.getTotal());
        resultPage.setRecords(manufacturePage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList()));

        return resultPage;
    }

    @Override
    public ManufactureDetailVO getManufactureDetail(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        // 2. 查询企业信息
        // @TableLogic 会自动过滤已删除记录，如果已删除则返回null
        Manufacture manufacture = this.getById(id);

        // 3. 检查是否存在
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        // 注意：无需手动检查 deleted != null，因为 getById 已自动过滤

        // 4. 转换为返回对象
        return convertToDetailVO(manufacture);
    }

    // ==================== 新增方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManufactureAddVO addManufacture(ManufactureAddRequestDTO requestDTO) {
        // 1. 参数校验（DTO上的注解已处理基础校验，这里处理业务校验）
        validateManufactureData(requestDTO.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                null);

        // 2. 创建实体对象
        Manufacture manufacture = new Manufacture();
        BeanUtils.copyProperties(requestDTO, manufacture);

        // 3. 保存到数据库
        boolean saved = this.save(manufacture);
        if (!saved) {
            throw new BusinessException(500, "新增企业失败");
        }

        log.info("企业新增成功，ID：{}，企业名称：{}", manufacture.getId(), manufacture.getCompanyName());

        // 4. 返回新创建的企业ID
        return new ManufactureAddVO(manufacture.getId());
    }

    // ==================== 更新方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManufacture(Long id, ManufactureUpdateRequestDTO requestDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        // 2. 查询企业是否存在
        // @TableLogic 会自动过滤已删除记录
        Manufacture existingManufacture = this.getById(id);
        if (existingManufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        // 注意：无需手动检查 deleted != null

        // 3. 业务校验（如果有修改相关字段）
        validateManufactureData(
                requestDTO.getCompanyName() != null ? requestDTO.getCompanyName() : existingManufacture.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                requestDTO.getCompanyName() != null ? id : null  // 如果修改了企业名称，需要检查重名
        );

        // 4. 复制非空字段到实体对象
        Manufacture updateManufacture = new Manufacture();
        updateManufacture.setId(id);

        // 使用工具方法复制非空属性
        copyNonNullProperties(requestDTO, updateManufacture);

        // 5. 执行更新
        boolean updated = this.updateById(updateManufacture);
        if (!updated) {
            throw new BusinessException(500, "修改企业信息失败");
        }

        log.info("企业更新成功，ID：{}", id);
    }

    // ==================== 删除方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteManufacture(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        // 2. 查询企业是否存在
        // @TableLogic 会自动过滤已删除记录
        Manufacture manufacture = this.getById(id);
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        // 注意：无需手动检查 deleted != null

        // 3. 执行逻辑删除
        // @TableLogic 配置了逻辑删除，会自动将 deleted 字段更新为 now()
        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除企业失败");
        }

        log.info("企业删除成功，ID：{}，企业名称：{}", id, manufacture.getCompanyName());
    }

    // ==================== 私有工具方法 ====================

    /**
     * 统一的企业数据校验方法
     *
     * @param companyName   企业名称（用于检查重名）
     * @param contactPhone  联系电话
     * @param scale         规模
     * @param annualRevenue 年营收
     * @param employeeCount 员工人数
     * @param excludeId     排除的企业ID（更新时使用）
     */
    private void validateManufactureData(String companyName,
                                         String contactPhone,
                                         String scale,
                                         BigDecimal annualRevenue,
                                         Integer employeeCount,
                                         Long excludeId) {
        // 1. 企业名称唯一性校验（如果有名称需要校验）
        if (StringUtils.isNotBlank(companyName)) {
            checkCompanyNameExists(companyName, excludeId);
        }

        // 2. 联系电话格式校验
        if (StringUtils.isNotBlank(contactPhone)) {
            if (!contactPhone.matches(PHONE_REGEX)) {
                throw new BusinessException(400, "联系电话格式不正确，应为11位手机号");
            }
        }

        // 3. 规模枚举值校验
        if (StringUtils.isNotBlank(scale)) {
            if (!scale.matches(SCALE_REGEX)) {
                throw new BusinessException(400, "规模值不正确，应为：micro/small/medium/large");
            }
        }

        // 4. 年营收范围校验
        if (annualRevenue != null) {
            if (annualRevenue.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "年营收不能为负数");
            }
            if (annualRevenue.compareTo(MAX_ANNUAL_REVENUE) > 0) {
                throw new BusinessException(400, "年营收不能超过100亿元");
            }
        }

        // 5. 员工人数范围校验
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
     * 检查企业名称是否已存在（逻辑删除的记录不计入）
     *
     * @param companyName 企业名称
     * @param excludeId   排除的企业ID（更新时使用）
     */
    private void checkCompanyNameExists(String companyName, Long excludeId) {
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manufacture::getCompanyName, companyName);

        // 注意：@TableLogic 会自动添加 deleted IS NULL 条件，无需手动添加

        // 如果是更新操作，排除当前企业
        if (excludeId != null) {
            queryWrapper.ne(Manufacture::getId, excludeId);
        }

        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(409, "企业名称已存在");
        }
    }

    /**
     * 复制非空属性（支持父类字段）
     *
     * @param source 源对象
     * @param target 目标对象
     */
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }

        // 使用Spring的BeanUtils配合空属性数组
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 获取对象中值为null的属性名数组
     *
     * @param source 源对象
     * @return null属性名数组
     */
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
     * 将实体对象转换为列表返回对象
     *
     * @param manufacture 实体对象
     * @return 列表返回对象
     */
    private ManufactureListVO convertToListVO(Manufacture manufacture) {
        if (manufacture == null) {
            return null;
        }
        ManufactureListVO vo = new ManufactureListVO();
        BeanUtils.copyProperties(manufacture, vo);
        return vo;
    }

    /**
     * 将实体对象转换为详情返回对象
     *
     * @param manufacture 实体对象
     * @return 详情返回对象
     */
    private ManufactureDetailVO convertToDetailVO(Manufacture manufacture) {
        if (manufacture == null) {
            return null;
        }
        ManufactureDetailVO vo = new ManufactureDetailVO();
        BeanUtils.copyProperties(manufacture, vo);
        return vo;
    }
}