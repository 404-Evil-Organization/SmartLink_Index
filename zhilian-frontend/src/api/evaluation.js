import request from "@/utils/request";

/**
 * 提交评价
 * @param {Object} data - 评价数据
 * @param {number} data.coopId - 合作记录ID
 * @param {number} data.score - 评分（1-5星）
 * @param {string} [data.content] - 评价内容
 * @param {boolean} [data.isAnonymous] - 是否匿名
 * @returns {Promise}
 */
export function submitEvaluation(data) {
  return request({
    url: "/evaluation/submit",
    method: "post",
    data,
  });
}

/**
 * 获取服务商评价列表（用于服务商详情页）
 * @param {number} serviceId - 服务商ID
 * @param {Object} params - 分页参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @returns {Promise}
 */
export function getEvaluationList(serviceId, params = {}) {
  return request({
    url: `/evaluation/list/${serviceId}`,
    method: "get",
    params,
  });
}
