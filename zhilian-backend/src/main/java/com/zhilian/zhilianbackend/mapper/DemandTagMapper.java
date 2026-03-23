package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.entity.DemandTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:51
 * @Description: 需求标签关系表Mapper接口，提供需求标签关联的数据库操作
 */
@Mapper
public interface DemandTagMapper extends BaseMapper<DemandTag> {

    /**
     * @Author: xiaodengyou
     * @Date: 2026/03/23
     * @Param: list 需求标签关联列表
     * @Return: int 插入的记录数
     * @Description: 批量插入需求标签关联
     */
    int insertBatch(@Param("list") List<DemandTag> list);
}