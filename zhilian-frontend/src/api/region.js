import request from "@/utils/request";

/**
 * 3.1 获取所有区域指数
 * @param {Object} params - 查询参数（可选）
 * @param {string} params.quarter - 季度，如 "2025Q1"
 * @param {number} params.year - 年份
 * @param {number} params.month - 月份
 * @returns {Promise}
 */
export function getRegionIndexList(params) {
  return request({
    url: "/index/region/list",
    method: "get",
    params,
  });
}

/**
 * 3.2 获取特定区域指数
 * @param {string} region - 区域名称，如 "深圳"
 * @param {Object} params - 查询参数（可选）
 * @param {number} params.year - 年份，与 quarter 或 month 配合使用
 * @param {number} params.quarter - 季度（1-4），与 year 配合使用
 * @param {number} params.month - 月份（1-12），与 year 配合使用
 * @returns {Promise}
 */
export function getRegionIndex(region, params) {
  return request({
    url: `/index/region/${region}`,
    method: "get",
    params,
  });
}

/**
 * 3.3 获取趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.region - 区域
 * @param {string} params.start - 开始时间，格式 "YYYY-MM-DD"
 * @param {string} params.end - 结束时间，格式 "YYYY-MM-DD"
 * @returns {Promise}
 */
export function getTrendData(params) {
  return request({
    url: "/index/trend",
    method: "get",
    params,
  });
}
