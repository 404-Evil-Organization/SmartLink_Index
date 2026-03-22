import { ElMessage } from "element-plus";

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

/**
 * 统一的基于角色的路由访问控制函数
 *
 * @param {RouteLocationNormalized} to 目标路由
 * @param {RouteLocationNormalized} from 来源路由
 * @param {Function} next vue-router 导航控制函数
 * @param {ReturnType<typeof useUserStore>} userStore 用户状态仓库
 * @returns {boolean} 是否已在本函数内部处理了导航（包括重定向）
 */
export function enforceRoles(to, from, next, userStore) {
  // 查找匹配路由中是否配置了 roles
  const roleRecord = to.matched
    .slice()
    .reverse()
    .find((record) => record.meta && Array.isArray(record.meta.roles));

  if (!roleRecord) {
    return false; // 没有配置 roles 限制，直接放行
  }

  const allowedRoles = roleRecord.meta.roles;
  const userRole = userStore.userInfo && userStore.userInfo.role;

  if (!userRole || !allowedRoles.includes(userRole)) {
    ElMessage.error("当前账号无权限访问该页面");

    // 判断来源是否也没有权限（防止循环跳转）
    const fromRoleRecord = from.matched
      .slice()
      .reverse()
      .find((record) => record.meta && Array.isArray(record.meta.roles));
    const fromAllowedRoles = fromRoleRecord ? fromRoleRecord.meta.roles : null;
    const fromIsForbidden =
      fromAllowedRoles && (!userRole || !fromAllowedRoles.includes(userRole));

    if (fromIsForbidden || !from.fullPath || from.fullPath === to.fullPath) {
      next({ path: "/", replace: true });
    } else {
      next(from.fullPath);
    }
    return true; // 已处理导航
  }

  return false; // 权限验证通过，允许后续逻辑执行
}
