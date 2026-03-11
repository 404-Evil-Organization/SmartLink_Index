import request from "@/utils/request";

// 获取制造企业列表（分页+筛选）
export function getManufactureList(params) {
  return request({
    url: "/manufacture/list",
    method: "get",
    params,
  });
}

// 获取制造企业详情
export function getManufactureDetail(id) {
  return request({
    url: `/manufacture/${id}`,
    method: "get",
  });
}

// 新增制造企业
export function addManufacture(data) {
  return request({
    url: "/manufacture",
    method: "post",
    data,
  });
}

// 修改制造企业
export function updateManufacture(id, data) {
  return request({
    url: `/manufacture/${id}`,
    method: "put",
    data,
  });
}

// 删除制造企业（逻辑删除）
export function deleteManufacture(id) {
  return request({
    url: `/manufacture/${id}`,
    method: "delete",
  });
}
