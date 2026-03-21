/**
 * 统一的管理员路由访问控制函数
 *
 * @param {RouteLocationNormalized} to 目标路由
 * @param {RouteLocationNormalized} from 来源路由
 * @param {Function} next vue-router 导航控制函数
 * @param {ReturnType<typeof useUserStore>} userStore 用户状态仓库
 * @returns {boolean} 是否已在本函数内部处理了导航（包括重定向）
 */
import { ElMessage } from "element-plus";
export function enforceAdminOnly(to, from, next, userStore) {
  const isAdminRoute = to.matched.some(
    (record) => record.meta && record.meta.adminOnly,
  );
  // 来源路由是否也是管理员专属路由
  const fromIsAdminRoute = from.matched.some(
    (record) => record.meta && record.meta.adminOnly,
  );
  // 非管理员专属路由，直接放行，由调用方继续处理
  if (!isAdminRoute) {
    return false;
  }
  const role = userStore.userInfo && userStore.userInfo.role;
  if (role !== "admin") {
    ElMessage.error("当前账号无权限访问该页面");
    // 当来源路由本身也是管理员页，或来源无效/与目标相同，避免重定向循环，统一跳转到安全页面 `/`
    if (fromIsAdminRoute || !from.fullPath || from.fullPath === to.fullPath) {
      next({ path: "/", replace: true });
    } else {
      // 来源为非管理员页且与目标不同，可以安全回退
      next(from.fullPath);
    }
    // 已在本函数中处理导航（重定向）
    return true;
  }
  // 是管理员，允许后续导航逻辑继续执行
  return false;
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
  const roleRecord = to.matched.slice().reverse().find(
    (record) => record.meta && Array.isArray(record.meta.roles)
  );

  if (!roleRecord) {
    return false; // 没有配置 roles 限制，直接放行
  }

  const allowedRoles = roleRecord.meta.roles;
  const userRole = userStore.userInfo && userStore.userInfo.role;

  if (!userRole || !allowedRoles.includes(userRole)) {
    ElMessage.error("当前账号无权限访问该页面");
    
    // 判断来源是否也没有权限（防止循环跳转）
    const fromRoleRecord = from.matched.slice().reverse().find(
      (record) => record.meta && Array.isArray(record.meta.roles)
    );
    const fromAllowedRoles = fromRoleRecord ? fromRoleRecord.meta.roles : null;
    const fromIsForbidden = fromAllowedRoles && (!userRole || !fromAllowedRoles.includes(userRole));

    if (fromIsForbidden || !from.fullPath || from.fullPath === to.fullPath) {
      next({ path: "/", replace: true });
    } else {
      next(from.fullPath);
    }
    return true; // 已处理导航
  }

  return false; // 权限验证通过，允许后续逻辑执行
}
