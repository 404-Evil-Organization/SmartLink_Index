import request from "@/utils/request";

// 获取待审核企业列表
export function getAuditList(params) {
  return request({
    url: "/admin/enterprise/pending",
    method: "get",
    params,
  });
}

// 获取用户列表（管理员）
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

// 审核企业（通过/驳回）
export function auditEnterprise(id, type, status, remark = "") {
  return request({
    url: `/admin/enterprise/approve/${id}`,
    method: "post",
    data: { type, status, remark },
  });
}

// 修改用户状态
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

// 获取国家准入指南列表
export function getCountryGuideList(params) {
  return request({
    url: "/admin/country-guide/list",
    method: "get",
    params,
  });
}

// 新增国家准入指南
export function addCountryGuide(data) {
  return request({
    url: "/admin/country-guide",
    method: "post",
    data,
  });
}

// 修改国家准入指南
export function updateCountryGuide(id, data) {
  return request({
    url: `/admin/country-guide/${id}`,
    method: "put",
    data,
  });
}

// 删除国家准入指南
export function deleteCountryGuide(id) {
  return request({
    url: `/admin/country-guide/${id}`,
    method: "delete",
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
    url: "/admin/log/list",
    method: "get",
    params,
  });
}

/*
 * 获取出海案例列表（管理员）
 * @description 分页查询出海案例，支持按关键词等条件筛选
 * @param {Object} params - 请求参数
 * @param {number} [params.page] - 页码，默认1
 * @param {number} [params.size] - 每页条数，默认10
 * @param {string} [params.keyword] - 标题/企业名称等关键词模糊搜索
 * @returns {Promise<Object>} 返回分页数据
 */
export function getAbroadCaseList(params) {
  return request({
    url: "/admin/abroad-case/list",
    method: "get",
    params,
  });
}

/**
 * 获取待审核需求列表（管理员）
 * @param {Object} params - 请求参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=10] - 每页条数
 * @returns {Promise<{ total: number, records: any[] }>} 返回分页数据，包含总数和记录列表
 * @example
 * getPendingDemands({ page: 1, size: 10 }).then(res => {
 *   console.log(res.records) // 需求列表
 * })
 */
export function getPendingDemands(params) {
  return request({
    url: "/admin/demand/pending",
    method: "get",
    params,
  });
}

/**
 * 新增出海案例（管理员）
 * @description 管理员创建新的出海案例记录
 * @param {Object} data - 出海案例表单数据
 * @returns {Promise<Object>} 返回新增后的案例信息或通用响应结果
 */
export function addAbroadCase(data) {
  return request({
    url: "/admin/abroad-case",
    method: "post",
    data,
  });
}

/**
 * 审核需求（通过/驳回）
 * @param {number} id - 需求ID
 * @param {Object} data - 审核数据
 * @param {string} data.status - 审核状态：'approved' 通过 / 'rejected' 驳回
 * @param {string} [data.remark] - 审核意见（驳回时建议填写）
 * @returns {Promise<null>}
 * @example
 * approveDemand(123, { status: 'approved' })
 * approveDemand(123, { status: 'rejected', remark: '需求描述不清晰' })
 */
export function approveDemand(id, data) {
  return request({
    url: `/admin/demand/approve/${id}`,
    method: "post",
    data,
  });
}

/**
 * 修改出海案例（管理员）
 * @description 管理员根据案例ID更新出海案例信息
 * @param {number} id - 出海案例ID
 * @param {Object} data - 出海案例更新数据
 * @returns {Promise<Object>} 返回更新后的案例信息或通用响应结果
 */
export function updateAbroadCase(id, data) {
  return request({
    url: `/admin/abroad-case/${id}`,
    method: "put",
    data,
  });
}
/**
 * 获取区域指数列表
 *
 * @param {Object} params 查询参数对象（如区域名称、时间区间、分页信息等）
 * @returns {Promise} 返回后端响应的 Promise，对应区域指数列表数据
 */
export function getRegionIndexList(params) {
  return request({
    url: "/admin/region-index/list",
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
    url: "/admin/region-index",
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
    url: `/admin/region-index/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除出海案例（管理员）
 * @description 管理员根据ID删除指定的出海案例
 * @param {number} id - 出海案例ID
 * @returns {Promise<null>} 无返回数据
 */
export function deleteAbroadCase(id) {
  return request({
    url: `/admin/abroad-case/${id}`,
    method: "delete",
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
    url: `/admin/region-index/${id}`,
    method: "delete",
  });
}

/**
 * 获取操作日志详情
 * @param {number} id - 日志ID
 * @returns {Promise<Object>} 返回操作日志详情数据
 */
export function getOperLogDetail(id) {
  return request({
    url: `/admin/log/${id}`,
    method: "get",
  });
}
