import request from "@/utils/request";

/**
 * 提交诊断问卷
 * @param {Object} data 诊断数据
 * @param {number} data.manuId 制造企业ID
 * @param {number} data.infoScore 信息化得分（1-5）
 * @param {number} data.autoScore 自动化得分（1-5）
 * @param {number} data.dataScore 数据应用得分（1-5）
 * @param {number} data.serviceScore 服务协同得分（1-5）
 * @returns {Promise<{
 *   diagnosisId: number,
 *   totalScore: number,
 *   level: string,
 *   radarData: { [key: string]: number },
 *   suggestions: string[]
 * }>} 诊断结果
 */
export function submitDiagnosis(data) {
  return request({
    url: "/diagnosis/submit",
    method: "post",
    data,
  });
}

/**
 * 获取诊断报告
 * @param {number} id 诊断记录ID
 * @returns {Promise<{
 *   diagnosisId: number,
 *   manuId: number,
 *   infoScore: number,
 *   autoScore: number,
 *   dataScore: number,
 *   serviceScore: number,
 *   totalScore: number,
 *   level: string,
 *   suggestions: string[],
 *   diagnosisDate: string
 * }>} 诊断报告详情
 */
export function getDiagnosisResult(id) {
  return request({
    url: `/diagnosis/${id}`,
    method: "get",
  });
}
