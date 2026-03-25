import request from "@/utils/request";

/**
 * 获取用户列表（管理员）
 * @description 分页查询用户，支持按角色、状态筛选和关键词模糊搜索（8.1.1）
 * @param {Object} params - 请求参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @param {string} [params.role] - 角色筛选：manufacture/service/park/admin
 * @param {number} [params.status] - 状态筛选：0禁用 1正常
 * @param {string} [params.keyword] - 用户名/手机号模糊搜索
 * @returns {Promise<Object>} 返回分页数据
 * @example 返回数据示例：
 * {
 *   "total": 150,
 *   "records": [
 *     {
 *       "id": 1001,
 *       "username": "tech_company",
 *       "role": "manufacture",
 *       "phone": "13800138001",
 *       "email": "test@example.com",
 *       "status": 1,
 *       "createTime": "2026-03-01 10:00:00"
 *     }
 *   ]
 * }
 */
export function getUserList(params) {
  return request({
    url: "/admin/user/list",
    method: "get",
    params,
  });
}

/**
 * 修改用户状态（启用/禁用）
 * @description 管理员修改指定用户的状态（8.1.3）
 * @param {number} id - 用户ID
 * @param {number} status - 状态：0禁用 1正常
 * @returns {Promise<null>} 无返回数据
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/status/${id}`,
    method: "put",
    data: { status },
  });
}

/**
 * 重置用户密码（管理员）
 * @description 管理员重置指定用户的密码，系统生成临时密码（8.1.4）
 * @param {number} id - 用户ID
 * @returns {Promise<Object>} 返回包含临时密码的对象
 * @example 返回数据示例：
 * {
 *   "newPassword": "temp123456"
 * }
 */
export function resetUserPassword(id) {
  return request({
    url: `/admin/user/reset-password/${id}`,
    method: "post",
  });
}

/**
 * 获取用户详情（管理员）
 * @description 管理员获取指定用户的详细信息，包含关联的企业信息（8.1.2）
 * @param {number} id - 用户ID
 * @returns {Promise<Object>} 返回用户详情对象，根据角色可能包含 manufactureInfo 或 serviceProviderInfo
 * @example 返回数据示例（制造企业角色）：
 * {
 *   "id": 1001,
 *   "username": "tech_company",
 *   "role": "manufacture",
 *   "phone": "13800138001",
 *   "email": "test@example.com",
 *   "status": 1,
 *   "createTime": "2026-03-01 10:00:00",
 *   "manufactureInfo": {
 *     "id": 2001,
 *     "companyName": "深圳电子科技",
 *     "region": "深圳",
 *     "scale": "medium"
 *   }
 * }
 */
export function getUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: "get",
  });
}

/**
 * 获取操作日志列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页条数
 * @param {string} params.username - 操作人（模糊匹配）
 * @param {string} params.operation - 操作类型
 * @param {string} params.startTime - 开始日期，格式 YYYY-MM-DD
 * @param {string} params.endTime - 结束日期，格式 YYYY-MM-DD
 * @returns {Promise<{ total: number, records: Array }>}
 */
export function getLogList(params) {
  return request({
    url: '/admin/log/list',
    method: 'get',
    params
  })
}