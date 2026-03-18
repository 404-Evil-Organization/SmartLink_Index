import request from "@/utils/request";

/**
 * 用户登录
 * @param {Object} data 登录信息
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @returns {Promise<{ token: string }>} 返回包含 token 的对象
 */
export function login(data) {
  return request({
    url: "/auth/login",
    method: "post",
    data,
  });
}

/**
 * 用户注册
 * @param {Object} data 注册信息
 * @param {string} data.username 用户名
 * @param {string} data.password 密码
 * @param {string} data.role 角色：manufacture/service/park/admin
 * @param {string} data.phone 联系电话
 * @param {string} data.email 电子邮箱
 * @returns {Promise<{ userId: number, username: string, role: string }>} 返回注册成功的用户信息
 */
export function register(data) {
  return request({
    url: "/auth/register",
    method: "post",
    data,
  });
}

/**
 * 获取当前登录用户信息
 * @returns {Promise<{
 *   id: number,
 *   username: string,
 *   role: string,
 *   phone: string,
 *   email: string,
 *   status: number,
 *   createTime: string
 * }>} 用户详细信息
 */
export function getCurrentUser() {
  return request({
    url: "/auth/me",
    method: "get",
  });
}

/**
 * 修改密码
 * @param {Object} data 密码信息
 * @param {string} data.oldPassword 旧密码
 * @param {string} data.newPassword 新密码
 * @returns {Promise<null>} 无返回数据
 */
export function changePassword(data) {
  return request({
    url: "/auth/change-password",
    method: "post",
    data,
  });
}
