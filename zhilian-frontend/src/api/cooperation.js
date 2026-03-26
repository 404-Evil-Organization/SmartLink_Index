import request from "@/utils/request";

/**
 * 获取我的合作记录列表（5.4）
 * @param {Object} params
 * @param {string} [params.status] - 合作状态：ongoing/completed/cancelled
 * @param {number} [params.enterpriseId] - 企业ID
 * @param {number} [params.page] - 页码
 * @param {number} [params.size] - 每页条数
 */
export function getMyCooperationList(params) {
  return request({
    url: "/cooperation/my-list",
    method: "get",
    params,
  });
}

/**
 * 获取合作记录详情（5.5）
 * @param {number} id - 合作记录ID
 */
export function getCooperationDetail(id) {
  return request({
    url: `/cooperation/${id}`,
    method: "get",
  });
}

/**
 * 取消合作（4.7）
 * @param {number} id - 合作记录ID
 * @param {string} [reason] - 取消原因
 */
export function cancelCooperation(id, reason = "") {
  return request({
    url: `/cooperation/cancel/${id}`,
    method: "post",
    data: { reason },
  });
}
