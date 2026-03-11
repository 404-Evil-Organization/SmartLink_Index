// src/api/service-provider.js
import request from '@/utils/request'

/**
 * 获取服务商列表（分页 + 筛选）
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页条数
 * @param {string} params.companyName 服务商名称（可选）
 * @param {string} params.region 区域（可选）
 * @param {string} params.serviceType 服务类型（可选）
 * @returns {Promise}
 */
export function getServiceProviderList(params) {
  return request({
    url: '/service-provider/list',
    method: 'get',
    params
  })
}

/**
 * 获取服务商详情
 * @param {number} id 服务商ID
 * @returns {Promise}
 */
export function getServiceProviderDetail(id) {
  return request({
    url: `/service-provider/${id}`,
    method: 'get'
  })
}

/**
 * 新增服务商
 * @param {Object} data 服务商信息
 * @returns {Promise}
 */
export function addServiceProvider(data) {
  return request({
    url: '/service-provider',
    method: 'post',
    data
  })
}

/**
 * 修改服务商信息
 * @param {number} id 服务商ID
 * @param {Object} data 要更新的字段
 * @returns {Promise}
 */
export function updateServiceProvider(id, data) {
  return request({
    url: `/service-provider/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除服务商（逻辑删除）
 * @param {number} id 服务商ID
 * @returns {Promise}
 */
export function deleteServiceProvider(id) {
  return request({
    url: `/service-provider/${id}`,
    method: 'delete'
  })
}