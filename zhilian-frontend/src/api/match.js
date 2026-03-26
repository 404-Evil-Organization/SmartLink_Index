// src/api/match.js

import request from "@/utils/request";

/**
 * 获取合作市场需求列表（服务商浏览需求）
 * @param {Object} params - 请求参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=10] - 每页条数
 * @param {string} [params.keyword] - 需求标题关键词（模糊匹配）
 * @param {string} [params.tagIds] - 标签ID，多个用逗号分隔，如 "1,2,3"
 * @param {number} [params.expectedBudgetMin] - 预算最小值（万元）
 * @param {number} [params.expectedBudgetMax] - 预算最大值（万元）
 * @param {string} [params.deadlineStart] - 期望完成日期开始范围，格式 YYYY-MM-DD
 * @param {string} [params.deadlineEnd] - 期望完成日期结束范围，格式 YYYY-MM-DD
 * @returns {Promise<Object>} 返回需求列表数据
 * @example
 * getDemandMarketList({ page: 1, size: 10, keyword: 'PCB' }).then(res => {
 *   console.log(res.records, res.total);
 * });
 */
export function getDemandMarketList(params) {
  return request({
    url: "/demand/market/list",
    method: "get",
    params,
  });
}

/**
 * 服务商接取需求
 * @param {Object} data - 请求体
 * @param {number} data.demandId - 需求ID
 * @param {number} data.serviceId - 服务商企业ID（接取该需求的服务商企业）
 * @returns {Promise<Object>} 返回合作记录ID
 * @example
 * acceptDemand({ demandId: 3001, serviceId: 2001 }).then(res => {
 *   console.log(res.cooperationId);
 * });
 */
export function acceptDemand(data) {
  return request({
    url: "/demand/accept",
    method: "post",
    data,
  });
}
