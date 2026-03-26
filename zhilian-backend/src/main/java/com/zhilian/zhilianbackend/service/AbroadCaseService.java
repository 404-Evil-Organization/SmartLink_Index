package com.zhilian.zhilianbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseCreateRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseQueryRequest;
import com.zhilian.zhilianbackend.dto.request.AbroadCaseUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseDetailResponse;
import com.zhilian.zhilianbackend.dto.response.AbroadCaseListResponse;

/**
 * @Description: 出海案例Service接口
 * @Author: 6017
 * @Date: 2026/3/25
 **/
public interface AbroadCaseService {

    /**
     * 分页查询出海案例列表（管理员）
     */
    IPage<AbroadCaseListResponse> listByPage(AbroadCaseQueryRequest queryRequest);

    /**
     * 获取出海案例详情
     */
    AbroadCaseDetailResponse getDetail(Long id);

    /**
     * 新增出海案例
     */
    Long create(AbroadCaseCreateRequest request, Long adminId, String adminName);

    /**
     * 更新出海案例
     */
    void update(Long id, AbroadCaseUpdateRequest request);

    /**
     * 删除出海案例（逻辑删除）
     */
    void delete(Long id);
}