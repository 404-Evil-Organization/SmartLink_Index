import request from "@/utils/request";

/**
 * 获取统计卡片数据
 * @returns {Promise<{
 *   manufactureCount: number,
 *   serviceCount: number,
 *   demandCount: number,
 *   cooperationCount: number
 * }>}
 */
export function getStatistics() {
  return request({
    url: "/dashboard/statistics",
    method: "get",
  });
}

/**
 * 获取热力图数据
 * @param {Object} params 查询参数
 * @param {string} [params.start] 开始日期
 * @param {string} [params.end] 结束日期
 * @returns {Promise<Array<{ region: string, value: number }>>}
 */
export function getHeatmap(params) {
  return request({
    url: "/dashboard/heatmap",
    method: "get",
    params,
  });
}

/**
 * 获取热门需求
 * @param {Object} params 查询参数
 * @param {number} [params.top=5] 返回数量
 * @returns {Promise<Array<{ serviceType: string, count: number }>>}
 */
export function getTopDemands(params) {
  return request({
    url: "/dashboard/topDemands",
    method: "get",
    params,
  });
}

/**
 * 获取网络关系数据
 * @returns {Promise<{
 *   nodes: Array<{ id: string, name: string, type: string }>,
 *   links: Array<{ source: string, target: string, value: number }>
 * }>}
 */
export function getNetwork() {
  return request({
    url: "/dashboard/network",
    method: "get",
  });
}
