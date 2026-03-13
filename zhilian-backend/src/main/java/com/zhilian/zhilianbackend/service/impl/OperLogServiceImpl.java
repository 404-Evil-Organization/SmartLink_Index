package com.zhilian.zhilianbackend.service.impl;

import com.zhilian.zhilianbackend.entity.OperLog;
import com.zhilian.zhilianbackend.mapper.OperLogMapper;
import com.zhilian.zhilianbackend.service.OperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: taciturn-hg
 * @Date: 2026/3/13 21:54
 * @Param:
 * @Return:
 * @Description: 操作日志表业务逻辑实现类
**/
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {
}
