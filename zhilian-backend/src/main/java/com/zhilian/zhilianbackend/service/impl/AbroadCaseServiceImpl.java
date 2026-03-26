package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.common.constant.DateConstants;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseVO;
import com.zhilian.zhilianbackend.entity.AbroadCase;
import com.zhilian.zhilianbackend.mapper.AbroadCaseMapper;
import com.zhilian.zhilianbackend.service.AbroadCaseService;
import com.zhilian.zhilianbackend.utils.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/26 15:51
 * @Description: 出海成功案例业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AbroadCaseServiceImpl extends ServiceImpl<AbroadCaseMapper, AbroadCase> implements AbroadCaseService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: request 成功案例查询请求参数（含分页、筛选条件）
     * @Return: PageResult<AbroadCaseVO> 分页封装的成功案例视图对象
     * @Description: 分页查询已发布的成功案例，支持按国家、服务类型模糊筛选
     */
    @Override
    public PageResult<AbroadCaseVO> getAbroadCases(AbroadCaseQueryRequest request) {
        String country = request.getCountry();
        String serviceType = request.getServiceType();
        int pageNum = request.getPage();
        int pageSize = request.getSize();

        log.debug("查询成功案例，国家：{}，服务类型：{}，页码：{}，每页条数：{}",
                country, serviceType, pageNum, pageSize);

        Page<AbroadCase> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<AbroadCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AbroadCase::getStatus, 1)
                .apply("deleted = {0}", DateConstants.getNotDeletedTimeStr());

        if (StringUtils.hasText(country)) {
            String escapedCountry = SqlUtils.escapeSqlLike(country);
            // 使用四个反斜杠，最终 SQL 为 ESCAPE '\\'
            wrapper.apply("country LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escapedCountry);
        }
        if (StringUtils.hasText(serviceType)) {
            String escapedServiceType = SqlUtils.escapeSqlLike(serviceType);
            wrapper.apply("service_type LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escapedServiceType);
        }

        wrapper.orderByDesc(AbroadCase::getPublishTime);

        IPage<AbroadCase> pageResult = this.page(page, wrapper);
        IPage<AbroadCaseVO> voPage = pageResult.convert(this::convertToVO);

        log.info("查询成功案例成功，总记录数：{}，本次返回：{}条", voPage.getTotal(), voPage.getRecords().size());

        return PageResult.from(voPage);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/26 15:51
     * @Param: abroadCase 实体对象
     * @Return: AbroadCaseVO 视图对象
     * @Description: 将 AbroadCase 实体转换为 AbroadCaseVO 视图对象
     */
    private AbroadCaseVO convertToVO(AbroadCase abroadCase) {
        AbroadCaseVO vo = new AbroadCaseVO();
        vo.setId(abroadCase.getId());
        vo.setTitle(abroadCase.getTitle());
        vo.setCompanyName(abroadCase.getCompanyName());
        vo.setCompanyType(abroadCase.getCompanyType());
        vo.setCountry(abroadCase.getCountry());
        vo.setServiceType(abroadCase.getServiceType());
        vo.setDescription(abroadCase.getDescription());
        vo.setCoverImage(abroadCase.getCoverImage());
        vo.setPublishTime(abroadCase.getPublishTime());
        return vo;
    }
}