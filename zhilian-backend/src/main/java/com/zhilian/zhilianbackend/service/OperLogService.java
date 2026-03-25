package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.entity.OperLog;

import java.time.LocalDateTime;

public interface OperLogService extends IService<OperLog> {

    /**
     * 分页查询操作日志（支持多条件筛选）
     *
     * @param page      当前页码
     * @param size      每页条数
     * @param username  操作人用户名（模糊匹配）
     * @param operation 操作类型（精确匹配）
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    IPage<OperLog> listOperLogs(Integer page, Integer size, String username, String operation,
                                LocalDateTime startTime, LocalDateTime endTime);
}