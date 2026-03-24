// src/api/demand.js
import request from "@/utils/request";

/**
 * 发布需求
 * @param {Object} data 需求数据
 * @param {number} data.manuId 制造企业ID
 * @param {string} data.title 需求标题
 * @param {string} [data.description] 详细描述
 * @param {number} [data.expectedBudget] 预算（万元）
 * @param {string} [data.deadline] 期望完成日期，格式 YYYY-MM-DD
 * @param {number[]} [data.tags] 标签ID列表
 * @returns {Promise<{demandId: number, auditStatus: string}>}
 */
export const publishDemand = (data) => {
  return request({
    url: "/demand/publish",
    method: "post",
    data,
  });
};

/**
 * 获取我的需求列表（仅制造企业）
 * @param {Object} params 查询参数
 * @param {number} [params.page=1] 页码
 * @param {number} [params.size=10] 每页条数
 * @param {string} [params.status] 状态筛选，多个用逗号分隔，如 'draft,published'（可选值：draft, published, matched, closed）
 * @param {string} [params.keyword] 标题关键词模糊搜索
 * @returns {Promise<{total: number, records: Array}>}
 */
export const getMyDemandList = (params) => {
  return request({
    url: "/demand/my-list",
    method: "get",
    params,
  });
};

/**
 * 获取需求详情
 * @param {number} id 需求ID
 * @returns {Promise<Object>} 需求对象，包含 manuId, title, description, expectedBudget, deadline, status, tags 等字段
 */
export const getDemandDetail = (id) => {
  return request({
    url: `/demand/${id}`,
    method: "get",
  });
};

/**
 * 编辑需求
 * @param {number} id 需求ID
 * @param {Object} data 更新的字段（可选）
 * @param {string} [data.title] 需求标题
 * @param {string} [data.description] 详细描述
 * @param {number} [data.expectedBudget] 预算（万元）
 * @param {string} [data.deadline] 期望完成日期，格式 YYYY-MM-DD
 * @param {number[]} [data.tags] 标签ID列表
 * @returns {Promise<null>}
 */
export const updateDemand = (id, data) => {
  return request({
    url: `/demand/${id}`,
    method: "put",
    data,
  });
};

/**
 * 删除需求（逻辑删除）
 * @param {number} id 需求ID
 * @returns {Promise<null>}
 */
export const deleteDemand = (id) => {
  return request({
    url: `/demand/${id}`,
    method: "delete",
  });
};
