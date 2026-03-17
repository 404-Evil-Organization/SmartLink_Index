import request from "@/utils/request";

/**
 * 获取制造企业列表（公共列表）
 * @param {Object} params 查询参数
 * @param {number} [params.page=1] 页码
 * @param {number} [params.size=10] 每页条数
 * @param {string} [params.region] 区域筛选
 * @param {string} [params.scale] 规模筛选：micro/small/medium/large
 * @param {string} [params.productType] 主营产品类型（模糊匹配）
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
 *     auditStatus: string
 *   }>
 * }>} 分页结果（仅包含审核通过的企业）
 */
export function getManufactureList(params) {
  return request({
    url: "/manufacture/list",
    method: "get",
    params,
  });
}

/**
 * 获取制造企业详情
 * @param {number} id 企业ID
 * @returns {Promise<{
 *   id: number,
 *   userId: number,
 *   companyName: string,
 *   region: string,
 *   address: string,
 *   contactPerson: string,
 *   contactPhone: string,
 *   scale: string,
 *   employeeCount: number,
 *   annualRevenue: number,
 *   productType: string,
 *   description: string,
 *   logo: string,
 *   establishedDate: string,
 *   auditStatus: string,
 *   auditRemark: string | null,
 *   auditTime: string | null,
 *   createTime: string,
 *   updateTime: string
 * }>} 企业详细信息
 */
export function getManufactureDetail(id) {
  return request({
    url: `/manufacture/${id}`,
    method: "get",
  });
}

/**
 * 新增制造企业
 * @param {Object} data 企业信息
 * @param {string} data.companyName 企业全称
 * @param {string} [data.region] 区域
 * @param {string} [data.address] 详细地址
 * @param {string} [data.contactPerson] 联系人
 * @param {string} [data.contactPhone] 联系电话
 * @param {string} [data.scale] 规模枚举
 * @param {number} [data.employeeCount] 员工人数
 * @param {number} [data.annualRevenue] 年营收（万元）
 * @param {string} [data.productType] 主营产品类型
 * @param {string} [data.description] 企业简介
 * @param {string} [data.logo] Logo图片URL
 * @param {string} [data.establishedDate] 成立日期
 * @returns {Promise<{ id: number, auditStatus: string }>} 新增成功返回ID和审核状态（pending）
 */
export function addManufacture(data) {
  return request({
    url: "/manufacture",
    method: "post",
    data,
  });
}

/**
 * 修改制造企业信息
 * @param {number} id 企业ID
 * @param {Object} data 要修改的字段（全部可选）
 * @returns {Promise<null>} 无返回数据
 */
export function updateManufacture(id, data) {
  return request({
    url: `/manufacture/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除制造企业（逻辑删除）
 * @param {number} id 企业ID
 * @returns {Promise<null>} 无返回数据
 */
export function deleteManufacture(id) {
  return request({
    url: `/manufacture/${id}`,
    method: "delete",
  });
}
