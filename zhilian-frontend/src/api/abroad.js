// src/api/abroad.js
import request from "@/utils/request";

/**
 * 获取出海服务列表（服务商）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页条数，默认10
 * @param {string} params.serviceType - 服务类型筛选
 * @param {string} params.region - 区域筛选
 * @returns {Promise} 返回服务商列表数据
 */
export const getAbroadServiceList = (params) => {
  return request({
    url: "/abroad/services",
    method: "get",
    params,
  });
};

/**
 * 获取出海成功案例列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页条数，默认10
 * @param {string} params.country - 目标国家筛选
 * @param {string} params.serviceType - 服务类型筛选
 * @returns {Promise} 返回案例列表数据
 */
export const getAbroadCaseList = (params) => {
  return request({
    url: "/abroad/cases",
    method: "get",
    params,
  });
};
