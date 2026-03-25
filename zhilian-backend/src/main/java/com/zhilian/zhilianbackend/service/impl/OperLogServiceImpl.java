package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhilian.zhilianbackend.entity.OperLog;
import com.zhilian.zhilianbackend.mapper.OperLogMapper;
import com.zhilian.zhilianbackend.service.OperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

    @Override
    public IPage<OperLog> listOperLogs(Integer page, Integer size, String username, String operation,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        // 构建分页对象
        Page<OperLog> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊匹配
        if (StringUtils.isNotBlank(username)) {
            wrapper.like(OperLog::getUsername, username);
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

        return this.baseMapper.selectPage(pageParam, wrapper);
    }
}