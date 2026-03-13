package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.entity.OperLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TraeAI
 * @Date: 2026/3/13
 * @Description: 操作日志表Mapper接口
 **/
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {
}
