/**
 * 检查当前用户是否有权限访问目标路由
 * @param {Object} to - Vue Router 的目标路由对象
 * @param {Object} userStore - 用户状态 store，需包含 userInfo 和 role 信息
 * @returns {boolean} - true 表示允许访问，false 表示拒绝访问
 */
export function checkRoleAccess(to, userStore) {
  // 从目标路由的 meta 中获取允许访问的角色列表
  const allowedRoles = to.meta.roles;

  // 如果路由没有设置 roles 限制，则任何用户均可访问（返回 true）
  if (!allowedRoles) return true;

  // 获取当前用户的角色（假设存储在 userStore.userInfo.role 中）
  const userRole = userStore.userInfo?.role;

  // 如果路由有角色限制但用户角色信息缺失，则拒绝访问（返回 false）
  if (!userRole) return false;

  // 检查用户角色是否在允许的角色列表中
  return allowedRoles.includes(userRole);
}
