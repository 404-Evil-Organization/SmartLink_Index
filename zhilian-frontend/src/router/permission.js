/**
 * 统一的管理员路由访问控制函数
 *
 * @param {RouteLocationNormalized} to 目标路由
 * @param {RouteLocationNormalized} from 来源路由
 * @param {Function} next vue-router 导航控制函数
 * @param {ReturnType<typeof useUserStore>} userStore 用户状态仓库
 * @returns {boolean} 是否已在本函数内部处理了导航（包括重定向）
 */
export function enforceAdminOnly(to, from, next, userStore) {
  const isAdminRoute = to.matched.some(
    (record) => record.meta && record.meta.adminOnly,
  );
  // 非管理员专属路由，直接放行，由调用方继续处理
  if (!isAdminRoute) {
    return false;
  }
  const role = userStore.userInfo && userStore.userInfo.role;
  if (role !== "admin") {
    ElMessage.error("当前账号无权限访问该页面");
    next(from.fullPath && from.fullPath !== to.fullPath ? from.fullPath : "/");
    // 已在本函数中处理导航（重定向）
    return true;
  }
  // 是管理员，允许后续导航逻辑继续执行
  return false;
}