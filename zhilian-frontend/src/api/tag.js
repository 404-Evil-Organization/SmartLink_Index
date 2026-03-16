import request from "@/utils/request";

// 获取标签列表（分页+筛选）
export function getTagList(params) {
  return request({
    url: "/tag/list",
    method: "get",
    params,
  });
}

// 获取标签详情
export function getTagDetail(id) {
  return request({
    url: `/tag/${id}`,
    method: "get",
  });
}

// 新增标签
export function addTag(data) {
  return request({
    url: "/tag",
    method: "post",
    data,
  });
}

// 修改标签
export function updateTag(id, data) {
  return request({
    url: `/tag/${id}`,
    method: "put",
    data,
  });
}

// 删除标签
export function deleteTag(id) {
  return request({
    url: `/tag/${id}`,
    method: "delete",
  });
}
