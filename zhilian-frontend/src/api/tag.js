import request from "@/utils/request";

/**
 * 获取标签列表（分页+筛选）
 * @description 分页查询标签，支持按名称模糊匹配和类别筛选（1.6.1）
 * @param {Object} params - 请求参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @param {string} [params.name] - 标签名称（模糊匹配）
 * @param {string} [params.category] - 类别筛选：service/certification/product/general
 * @returns {Promise<Object>} 返回分页数据
 * @example 返回数据示例：
 * {
 *   "total": 100,
 *   "records": [
 *     {
 *       "id": 1,
 *       "name": "CNAS认证",
 *       "category": "certification",
 *       "description": "中国合格评定国家认可委员会认证",
 *       "createTime": "2026-03-01 10:00:00",
 *       "updateTime": "2026-03-01 10:00:00"
 *     }
 *   ]
 * }
 */
export function getTagList(params) {
  return request({
    url: "/tag/list",
    method: "get",
    params,
  });
}

/**
 * 获取标签详情
 * @description 根据标签ID获取详细信息（1.6.2）
 * @param {number} id - 标签ID
 * @returns {Promise<Object>} 返回标签对象
 * @example 返回数据示例：
 * {
 *   "id": 1,
 *   "name": "CNAS认证",
 *   "category": "certification",
 *   "description": "中国合格评定国家认可委员会认证",
 *   "createTime": "2026-03-01 10:00:00",
 *   "updateTime": "2026-03-01 10:00:00"
 * }
 */
export function getTagDetail(id) {
  return request({
    url: `/tag/${id}`,
    method: "get",
  });
}

/**
 * 新增标签
 * @description 创建新标签（1.6.3）
 * @param {Object} data - 标签信息
 * @param {string} data.name - 标签名称（必填）
 * @param {string} [data.category] - 类别（可选）
 * @param {string} [data.description] - 标签说明（可选）
 * @returns {Promise<Object>} 返回创建结果，包含新标签ID
 * @example 返回数据示例：
 * {
 *   "id": 101
 * }
 */
export function addTag(data) {
  return request({
    url: "/tag",
    method: "post",
    data,
  });
}

/**
 * 修改标签
 * @description 更新指定标签的信息（1.6.4）
 * @param {number} id - 标签ID
 * @param {Object} data - 需要更新的字段，全部可选
 * @param {string} [data.name] - 标签名称
 * @param {string} [data.category] - 类别
 * @param {string} [data.description] - 标签说明
 * @returns {Promise<null>} 无返回数据
 */
export function updateTag(id, data) {
  return request({
    url: `/tag/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除标签
 * @description 删除指定ID的标签（1.6.5）
 * @param {number} id - 标签ID
 * @returns {Promise<null>} 无返回数据
 */
export function deleteTag(id) {
  return request({
    url: `/tag/${id}`,
    method: "delete",
  });
}
