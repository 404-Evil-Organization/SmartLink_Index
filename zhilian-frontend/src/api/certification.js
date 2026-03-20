import request from '@/utils/request'

/**
 * 获取证书列表（按服务商筛选）
 * @param {Object} params
 * @param {number} params.serviceId 服务商ID（必传）
 * @returns {Promise}
 */
export function getCertList(params) {
  return request({
    url: '/certification/list',
    method: 'get',
    params
  })
}