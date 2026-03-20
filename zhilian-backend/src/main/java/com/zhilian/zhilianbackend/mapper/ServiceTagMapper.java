package com.zhilian.zhilianbackend.mapper;

import com.zhilian.zhilianbackend.entity.ServiceTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:51
 * @Description: 服务商能力标签表Mapper接口
 */
public interface ServiceTagMapper extends BaseMapper<ServiceTag> {

    /**
     * 批量插入或更新服务商标签关联
     * 使用 INSERT ... ON DUPLICATE KEY UPDATE 实现存在即更新（仅更新 update_time），不存在则插入
     *
     * @param list 待插入/更新的关联列表
     */
    void insertOrUpdateBatch(@Param("list") List<ServiceTag> list);
}