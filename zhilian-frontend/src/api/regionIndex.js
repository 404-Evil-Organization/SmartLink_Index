import request from "@/utils/request";

// 获取区域指数列表
export function getRegionIndexList(params) {
  return request({
    url: '/region-index/list',
    method: 'get',
    params
  })
}

// 新增区域指数
export function addRegionIndex(data) {
  return request({
    url: '/region-index',
    method: 'post',
    data
  })
}

// 修改区域指数
export function updateRegionIndex(id, data) {
  return request({
    url: `/region-index/${id}`,
    method: 'put',
    data
  })
}

// 删除区域指数
export function deleteRegionIndex(id) {
  return request({
    url: `/region-index/${id}`,
    method: 'delete'
  })
}