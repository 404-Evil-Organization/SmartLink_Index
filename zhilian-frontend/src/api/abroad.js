// src/api/abroad.js
import request from "@/utils/request";

/**
 * 获取国家准入指南列表（公开接口）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页条数，默认10
 * @param {string} params.keyword - 国家名称关键词（模糊匹配）
 * @returns {Promise} 返回分页列表数据
 */
export function getCountryGuideList(params) {
  return request({
    url: "/abroad/country-guide/list",
    method: "get",
    params,
  });
}

/**
 * 获取特定国家准入指南（公开接口）
 * 根据接口文档 6.2
 * @param {string} country - 国家名称，如“美国”
 * @returns {Promise} 返回该国家的准入指南详情
 */
export function getCountryGuideDetail(country) {
  return request({
    url: `/abroad/country/${encodeURIComponent(country)}`,
    method: "get",
  });
}
