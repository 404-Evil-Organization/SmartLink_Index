import request from '@/utils/request'

// 获取用户列表（管理员）
export function getUserList(params) {
  return request({
    url: "/admin/user/list",
    method: "get",
    params
  })
}

// 修改用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/status/${id}`,
    method: "put",
    data: { status }
  })
}

// 重置用户密码
export function resetUserPassword(id) {
  return request({
    url: `/admin/user/reset-password/${id}`,
    method: "post"
  })
}

// 获取用户详情（预留）
export function getUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: "get"
  })
}