import request from "@/utils/request";

/**
 * 获取个人制造企业列表
 * @param {Object} params - 分页参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @returns {Promise}
 */
export function getMyManufactureList(params) {
  return request({
    url: "/enterprise/manufacture/list",
    method: "get",
    params,
  });
}

/**
 * 获取个人服务商列表
 * @param {Object} params - 分页参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @returns {Promise}
 */
export function getMyServiceList(params) {
  return request({
    url: "/enterprise/service/list",
    method: "get",
    params,
  });
}
