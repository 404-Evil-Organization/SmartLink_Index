import request from '@/utils/request'

/**
 * 获取服务商评价列表
 * @param {number} serviceId 服务商ID
 * @param {Object} params 分页参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页条数
 * @returns {Promise<{ total: number, records: Array }>}
 */
export function getEvaluationList(serviceId, params) {
  return request({
    url: `/evaluation/list/${serviceId}`,
    method: 'get',
    params
  })
}