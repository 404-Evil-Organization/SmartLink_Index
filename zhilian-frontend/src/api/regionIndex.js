import request from "@/utils/request";

/**
 * 获取区域指数列表
 *
 * @param {Object} params 查询参数对象（如区域名称、时间区间、分页信息等）
 * @returns {Promise} 返回后端响应的 Promise，对应区域指数列表数据
 */
export function getRegionIndexList(params) {
  return request({
    url: "/region-index/list",
    method: "get",
    params,
  });
}

/**
 * 新增区域指数
 *
 * @param {Object} data 区域指数新增数据对象
 * @returns {Promise} 返回后端响应的 Promise，一般为新增结果或新增记录信息
 */
export function addRegionIndex(data) {
  return request({
    url: "/region-index",
    method: "post",
    data,
  });
}

/**
 * 修改区域指数
 *
 * @param {string|number} id 区域指数主键 ID
 * @param {Object} data 区域指数更新数据对象
 * @returns {Promise} 返回后端响应的 Promise，一般为更新结果
 */
export function updateRegionIndex(id, data) {
  return request({
    url: `/region-index/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除区域指数
 *
 * @param {string|number} id 区域指数主键 ID
 * @returns {Promise} 返回后端响应的 Promise，一般为删除结果
 */
export function deleteRegionIndex(id) {
  return request({
    url: `/region-index/${id}`,
    method: "delete",
  });
}