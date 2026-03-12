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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 23:31
 * @Param:
 * @Return:
 * @Description: 制造企业 Service 实现类
 **/
@Slf4j
@Service
public class ManufactureServiceImpl extends ServiceImpl<ManufactureMapper, Manufacture> implements ManufactureService {

    @Override
    public IPage<ManufactureListVO> getManufactureList(ManufactureListRequestDTO requestDTO) {
        // 1. 构建分页对象
        Page<Manufacture> page = new Page<>(requestDTO.getPage(), requestDTO.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();

        // 手动添加逻辑删除条件：deleted IS NULL（只查询未删除的记录）
        queryWrapper.isNull(Manufacture::getDeleted);

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
        Manufacture manufacture = this.getById(id);

        // 3. 检查是否存在
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 4. 检查是否已删除
        if (manufacture.getDeleted() != null) {
            throw new BusinessException(404, "企业已删除");
        }

        // 5. 转换为返回对象
        return convertToDetailVO(manufacture);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManufactureAddVO addManufacture(ManufactureAddRequestDTO requestDTO) {
        // 1. 参数校验
        validateAddRequest(requestDTO);

        // 2. 检查企业名称是否已存在
        checkCompanyNameExists(requestDTO.getCompanyName(), null);

        // 3. 创建实体对象
        Manufacture manufacture = new Manufacture();
        BeanUtils.copyProperties(requestDTO, manufacture);

        // 4. 保存到数据库
        boolean saved = this.save(manufacture);
        if (!saved) {
            throw new BusinessException(500, "新增企业失败");
        }

        // 5. 返回新创建的企业ID
        return new ManufactureAddVO(manufacture.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManufacture(Long id, ManufactureUpdateRequestDTO requestDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        // 2. 查询企业是否存在
        Manufacture existingManufacture = this.getById(id);
        if (existingManufacture == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 3. 检查是否已删除
        if (existingManufacture.getDeleted() != null) {
            throw new BusinessException(404, "企业已删除，无法修改");
        }

        // 4. 如果修改了企业名称，检查新名称是否已存在
        if (StringUtils.isNotBlank(requestDTO.getCompanyName()) &&
                !requestDTO.getCompanyName().equals(existingManufacture.getCompanyName())) {
            checkCompanyNameExists(requestDTO.getCompanyName(), id);
        }

        // 5. 校验其他字段（如果填写了）
        validateUpdateFields(requestDTO);

        // 6. 复制非空字段到实体对象
        Manufacture updateManufacture = new Manufacture();
        updateManufacture.setId(id);

        // 只复制不为null的字段
        copyNonNullProperties(requestDTO, updateManufacture);

        // 7. 执行更新
        boolean updated = this.updateById(updateManufacture);
        if (!updated) {
            throw new BusinessException(500, "修改企业信息失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteManufacture(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(400, "企业ID不能为空");
        }

        // 2. 查询企业是否存在
        Manufacture manufacture = this.getById(id);
        if (manufacture == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 3. 检查是否已删除
        if (manufacture.getDeleted() != null) {
            throw new BusinessException(404, "企业已被删除，不能重复删除");
        }

        // 4. 执行逻辑删除
        boolean deleted = this.removeById(id);
        if (!deleted) {
            throw new BusinessException(500, "删除企业失败");
        }

        log.info("企业删除成功，ID：{}，企业名称：{}", id, manufacture.getCompanyName());
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:31
     * @Param: requestDTO 新增请求参数
     * @Return: void
     * @Description: 校验新增请求参数
     **/
    private void validateAddRequest(ManufactureAddRequestDTO requestDTO) {
        // 联系电话格式校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getContactPhone())) {
            if (!requestDTO.getContactPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(400, "联系电话格式不正确，应为11位手机号");
            }
        }

        // 规模枚举值校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getScale())) {
            if (!requestDTO.getScale().matches("micro|small|medium|large")) {
                throw new BusinessException(400, "规模值不正确，应为：micro/small/medium/large");
            }
        }

        // 年营收不能超过合理范围（可选）
        if (requestDTO.getAnnualRevenue() != null &&
                requestDTO.getAnnualRevenue().compareTo(new BigDecimal("1000000")) > 0) {
            throw new BusinessException(400, "年营收不能超过100亿元");
        }

        // 员工人数不能超过合理范围（可选）
        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:31
     * @Param: requestDTO 修改请求参数
     * @Return: void
     * @Description: 校验修改请求的字段
     **/
    private void validateUpdateFields(ManufactureUpdateRequestDTO requestDTO) {
        // 联系电话格式校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getContactPhone())) {
            if (!requestDTO.getContactPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(400, "联系电话格式不正确，应为11位手机号");
            }
        }

        // 规模枚举值校验（如果填写了）
        if (StringUtils.isNotBlank(requestDTO.getScale())) {
            if (!requestDTO.getScale().matches("micro|small|medium|large")) {
                throw new BusinessException(400, "规模值不正确，应为：micro/small/medium/large");
            }
        }

        // 年营收不能超过合理范围（如果填写了）
        if (requestDTO.getAnnualRevenue() != null &&
                requestDTO.getAnnualRevenue().compareTo(new BigDecimal("1000000")) > 0) {
            throw new BusinessException(400, "年营收不能超过100亿元");
        }

        // 员工人数不能超过合理范围（如果填写了）
        if (requestDTO.getEmployeeCount() != null && requestDTO.getEmployeeCount() > 100000) {
            throw new BusinessException(400, "员工人数不能超过10万");
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:31
     * @Param: companyName 企业名称
     * @Param: excludeId 排除的企业ID（更新时使用）
     * @Return: void
     * @Description: 检查企业名称是否已存在
     **/
    private void checkCompanyNameExists(String companyName, Long excludeId) {
        LambdaQueryWrapper<Manufacture> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manufacture::getCompanyName, companyName)
                .isNull(Manufacture::getDeleted);

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
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:31
     * @Param: source 源对象
     * @Param: target 目标对象
     * @Return: void
     * @Description: 复制非空属性（使用反射手动实现）
     **/
    private void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
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
                    // 获取目标对象的对应字段
                    java.lang.reflect.Field targetField = target.getClass().getDeclaredField(field.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (Exception e) {
                // 忽略无法复制的字段（比如没有对应字段的情况）
                log.debug("复制属性失败：{}", field.getName());
            }
        }
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 23:31
     * @Param: manufacture 实体对象
     * @Return: ManufactureListVO 列表返回对象
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
     * @Date: 2026-03-12 23:31
     * @Param: manufacture 实体对象
     * @Return: ManufactureDetailVO 详情返回对象
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