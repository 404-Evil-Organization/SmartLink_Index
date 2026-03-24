import request from "@/utils/request";

/**
 * 获取资质证书列表
 * @description 根据服务商ID获取其所有证书（1.4.1），分页返回
 * @param {Object} params - 请求参数
 * @param {number} params.serviceId - 服务商ID（必传）
 * @returns {Promise<{total:number, records:Array, page:number, size:number}>} 返回分页对象，records 为证书列表：
 * @example 返回数据示例：
 * {
 *   "total": 1,
 *   "page": 1,
 *   "size": 10,
 *   "records": [
 *     {
 *       "id": 3001,
 *       "serviceId": 2001,
 *       "certName": "CNAS认证",
 *       "certNo": "CNAS L1234",
 *       "issueAuthority": "中国合格评定国家认可委员会",
 *       "issueDate": "2023-01-01",
 *       "expireDate": "2026-12-31",
 *       "certFileUrl": "https://...",
 *       "status": 1,
 *       "createTime": "2026-03-01 10:00:00"
 *     }
 *   ]
 * }
 */
export function getCertList(params) {
  return request({
    url: "/certification/list",
    method: "get",
    params,
  });
}

/**
 * 上传资质证书
 * @description 上传证书文件及元数据（1.4.2）
 * @param {FormData} data - 包含证书信息的 FormData 对象，必含字段：
 *   - serviceId: 服务商ID（long）
 *   - certName: 证书名称（string）
 *   - certNo: 证书编号（string，可选）
 *   - issueAuthority: 发证机构（string，可选）
 *   - issueDate: 发证日期（date，可选，格式 yyyy-MM-dd）
 *   - expireDate: 有效期至（date，可选，格式 yyyy-MM-dd）
 *   - file: 证书文件（File 对象）
 * @returns {Promise<number>} 返回新证书ID（数字）
 * @example 返回数据示例（request 拦截器已解包 Result<Long>）：
 * 3005
 */
export function uploadCert(data) {
  return request({
    url: "/certification/upload",
    method: "post",
    data,
    headers: { "Content-Type": "multipart/form-data" },
  });
}

/**
 * 更新证书信息
 * @description 修改证书的元数据（不替换文件）（1.4.3）
 * @param {number} id - 证书ID
 * @param {Object} data - 需要更新的字段，全部可选
 * @param {string} [data.certName] - 证书名称
 * @param {string} [data.certNo] - 证书编号
 * @param {string} [data.issueAuthority] - 发证机构
 * @param {string} [data.issueDate] - 发证日期（yyyy-MM-dd）
 * @param {string} [data.expireDate] - 有效期至（yyyy-MM-dd）
 * @param {number} [data.status] - 状态：0失效 1有效
 * @returns {Promise<null>} 无返回数据
 */
export function updateCert(id, data) {
  return request({
    url: `/certification/${id}`,
    method: "put",
    data,
  });
}

/**
 * 删除证书
 * @description 删除指定ID的证书（1.4.4）
 * @param {number} id - 证书ID
 * @returns {Promise<null>} 无返回数据
 */
export function deleteCert(id) {
  return request({
    url: `/certification/${id}`,
    method: "delete",
  });
}
