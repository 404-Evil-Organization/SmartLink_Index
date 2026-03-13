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
 * @Date: 2026/3/13 20:26
 * @Param:
 * @Return:
 * @Description: 制造企业 Service 实现类
 **/
@Slf4j
@Service
public class ManufactureServiceImpl extends ServiceImpl<ManufactureMapper, Manufacture> implements ManufactureService {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String SCALE_REGEX = "micro|small|medium|large";
    private static final BigDecimal MAX_ANNUAL_REVENUE = new BigDecimal("1000000");
    private static final int MAX_EMPLOYEE_COUNT = 100000;

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: param
     * @Return: java.lang.String
     * @Description: 对进行 SQL LIKE 查询的入参进行通配符转义，避免用户输入 % 或 _ 被数据库当作通配符使用，导致查询结果范围异常放大。
     * 说明：1. 先将反斜杠进行转义，避免与数据库默认转义字符冲突；
     *      2. 再对 % 和 _ 进行转义，使其按普通字符匹配。
     */
    private String escapeSqlLike(String param) {
        if (StringUtils.isBlank(param)) {
            return param;
        }
        String escaped = param.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return escaped;
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: requestDTO
     * @Return: com.baomidou.mybatisplus.core.metadata.IPage<com.zhilian.zhilianbackend.dto.response.ManufactureListVO>
     * @Description:
     */
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
            String escapedProductType = escapeSqlLike(requestDTO.getProductType());
            queryWrapper.like(Manufacture::getProductType, escapedProductType);
        }

        queryWrapper.orderByDesc(Manufacture::getCreateTime);

        Page<Manufacture> manufacturePage = this.page(page, queryWrapper);

        IPage<ManufactureListVO> resultPage = new Page<>(manufacturePage.getCurrent(), manufacturePage.getSize(), manufacturePage.getTotal());
        resultPage.setRecords(manufacturePage.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList()));

        return resultPage;
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: id
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO
     * @Description:
     */
    @Override
    public ManufactureDetailVO getManufactureDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        Manufacture manufacture = this.getById(id);

        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在或已被删除");
        }

        return convertToDetailVO(manufacture);
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: requestDTO
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureAddVO
     * @Description:
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManufactureAddVO addManufacture(ManufactureAddRequestDTO requestDTO) {
        validateManufactureData(requestDTO.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                null);

        Manufacture manufacture = new Manufacture();
        BeanUtils.copyProperties(requestDTO, manufacture);

        boolean saved = this.save(manufacture);
        if (!saved) {
            throw new BusinessException(500, "新增企业失败");
        }

        log.info("企业新增成功，ID：{}，企业名称：{}", manufacture.getId(), manufacture.getCompanyName());

        return new ManufactureAddVO(manufacture.getId());
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: id
     * @Param: requestDTO
     * @Return: void
     * @Description:
     */
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

        validateManufactureData(
                requestDTO.getCompanyName() != null ? requestDTO.getCompanyName() : existingManufacture.getCompanyName(),
                requestDTO.getContactPhone(),
                requestDTO.getScale(),
                requestDTO.getAnnualRevenue(),
                requestDTO.getEmployeeCount(),
                id
        );

        Manufacture updateManufacture = new Manufacture();
        updateManufacture.setId(id);

        copyNonNullProperties(requestDTO, updateManufacture);

        boolean updated = this.updateById(updateManufacture);
        if (!updated) {
            throw new BusinessException(500, "修改企业信息失败");
        }

        log.info("企业更新成功，ID：{}", id);
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: id
     * @Return: void
     * @Description:
     */
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

        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除企业失败");
        }

        log.info("企业删除成功，ID：{}，企业名称：{}", id, manufacture.getCompanyName());
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: companyName
     * @Param: contactPhone
     * @Param: scale
     * @Param: annualRevenue
     * @Param: employeeCount
     * @Param: excludeId
     * @Return: void
     * @Description: 统一的企业数据校验方法
     */
    private void validateManufactureData(String companyName,
                                         String contactPhone,
                                         String scale,
                                         BigDecimal annualRevenue,
                                         Integer employeeCount,
                                         Long excludeId) {
        if (StringUtils.isNotBlank(companyName)) {
            checkCompanyNameExists(companyName, excludeId);
        }

        if (StringUtils.isNotBlank(contactPhone)) {
            if (!contactPhone.matches(PHONE_REGEX)) {
                throw new BusinessException(400, "联系电话格式不正确，应为11位手机号");
            }
        }

        if (StringUtils.isNotBlank(scale)) {
            if (!scale.matches(SCALE_REGEX)) {
                throw new BusinessException(400, "规模值不正确，应为：micro/small/medium/large");
            }
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
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: companyName
     * @Param: excludeId
     * @Return: void
     * @Description: 检查企业名称是否已存在（逻辑删除的记录不计入）
     */
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
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: source
     * @Param: target
     * @Return: void
     * @Description: 复制非空属性（支持父类字段）
     */
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }

        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: source
     * @Return: java.lang.String[]
     * @Description: 获取对象中值为null的属性名数组
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
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: manufacture
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureListVO
     * @Description: 将实体对象转换为列表返回对象
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
     * @Author:
     * @Date: 2026/3/13 20:26
     * @Param: manufacture
     * @Return: com.zhilian.zhilianbackend.dto.response.ManufactureDetailVO
     * @Description: 将实体对象转换为详情返回对象
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