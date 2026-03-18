package com.zhilian.zhilianbackend.service;

import com.zhilian.zhilianbackend.dto.response.RegionIndexVO;
import com.zhilian.zhilianbackend.dto.response.RegionTrendVO;
import com.zhilian.zhilianbackend.entity.RegionIndex;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:25
 * @Param:
 * @Return:
 * @Description: 区域指数表业务逻辑接口，定义区域指数相关的业务方法
**/
public interface RegionIndexService extends IService<RegionIndex> {


    // ============== 计算和定时任务方法 ==============

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:07
     * @Param: year 年份  quarter 季度
     * @Return:
     * @Description: 计算并保存季度区域指数
    **/
    void calculateAndSaveQuarterIndex(Short year, Byte quarter);

    /**
     * @Author: 6017
     * @Date: 2026/3/18 23:08
     * @Param: year 年份  quarter 季度
     * @Return: 
     * @Description: 手动触发计算（用于测试）
    **/
    void manualCalculate(Short year, Byte quarter);
}
