import request from '@/utils/request'

/**
 * 获取个人制造企业列表（当前登录用户创建的制造企业，包含所有审核状态）
 * @param {Object} params 查询参数
 * @param {number} [params.page=1] 页码，默认1
 * @param {number} [params.size=10] 每页条数，默认10
 * @returns {Promise<{
 *   total: number,
 *   records: Array<{
 *     id: number,
 *     companyName: string,
 *     region: string,
 *     scale: string,
 *     productType: string,
 *     contactPerson: string,
 *     contactPhone: string,
 *     auditStatus: 'pending' | 'approved' | 'rejected',
 *     auditRemark: string | null,
 *     auditTime: string | null,
 *     createTime: string
 *   }>
 * }>} 分页结果
 */
export function getMyManufactureList(params) {
  return request({
    url: "/enterprise/manufacture/list",
    method: "get",
    params
  })
}