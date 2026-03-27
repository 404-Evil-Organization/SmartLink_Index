package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhilian.zhilianbackend.dto.response.OperLogVO;
import com.zhilian.zhilianbackend.entity.OperLog;

import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 22:45
 * @Description: 操作日志表业务逻辑接口
 */
public interface OperLogService extends IService<OperLog> {
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
     * @Description: 分页查询操作日志（支持多条件筛选）
     */
    IPage<OperLogVO> listOperLogs(Integer page, Integer size, String username, String operation,
                                  Date startTime, Date endTime);

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:17
     * @Param: operation 操作描述
     * @Param: params 请求参数（可为null）
     * @Param: result 操作结果
     * @Param: errorMsg 失败原因（可为null）
     * @Return: void
     * @Description: 记录操作日志，自动获取当前登录用户和IP
     */
    void recordLog(String operation, String params, String result, String errorMsg);

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:17
     * @Param: id 日志ID
     * @Return: OperLogVO 日志详情
     * @Description: 获取操作日志详情
     */
    OperLogVO getOperLogDetail(Long id);
}