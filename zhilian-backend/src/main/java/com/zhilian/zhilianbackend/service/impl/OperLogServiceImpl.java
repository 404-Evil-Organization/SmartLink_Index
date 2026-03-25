package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.dto.response.OperLogVO;
import com.zhilian.zhilianbackend.entity.OperLog;
import com.zhilian.zhilianbackend.mapper.OperLogMapper;
import com.zhilian.zhilianbackend.service.OperLogService;
import com.zhilian.zhilianbackend.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 22:45
 * @Description: 操作日志表业务逻辑实现类
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25 22:45
     * @Param: page 当前页码
     * @Param: size 每页条数
     * @Param: username 操作人用户名（模糊匹配）
     * @Param: operation 操作类型（精确匹配）
     * @Param: startTime 开始时间
     * @Param: endTime 结束时间
     * @Return: IPage<OperLogVO> 分页结果（VO 类型）
     * @Description: 分页查询操作日志，支持多条件筛选，并将实体转换为 VO
     */
    @Override
    public IPage<OperLogVO> listOperLogs(Integer page, Integer size, String username, String operation,
                                         Date startTime, Date endTime) {
        // 构建分页对象
        Page<OperLog> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊匹配（转义通配符，防止 SQL 注入/通配符放大）
        if (StringUtils.isNotBlank(username)) {
            String escaped = SqlUtils.escapeSqlLike(username);
            wrapper.apply("username LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escaped);
        }

        // 操作类型精确匹配
        if (StringUtils.isNotBlank(operation)) {
            wrapper.eq(OperLog::getOperation, operation);
        }

        // 时间范围查询
        if (Objects.nonNull(startTime)) {
            wrapper.ge(OperLog::getCreateTime, startTime);
        }
        if (Objects.nonNull(endTime)) {
            wrapper.le(OperLog::getCreateTime, endTime);
        }

        // 按创建时间倒序排序（最新的在前）
        wrapper.orderByDesc(OperLog::getCreateTime);

        // 执行分页查询
        IPage<OperLog> entityPage = this.baseMapper.selectPage(pageParam, wrapper);

        // 使用 convert 方法将实体转换为 VO，保持分页元信息不变
        return entityPage.convert(this::convertToVO);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25 22:45
     * @Param: entity 操作日志实体
     * @Return: OperLogVO 操作日志 VO
     * @Description: 将实体转换为 VO，隐藏内部字段
     */
    private OperLogVO convertToVO(OperLog entity) {
        if (entity == null) {
            return null;
        }
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}