package com.zhilian.zhilianbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhilian.zhilianbackend.entity.ManufactureTag;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ManufactureTagMapper extends BaseMapper<ManufactureTag> {
    /**
     * 批量插入制造企业标签关联，若唯一键冲突则更新（实际无字段变更，仅用于幂等）
     */
    void insertOrUpdateBatch(@Param("list") List<ManufactureTag> list);
}