import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";

import dashboardRoutes from "./models/dashboard";
import adminRoutes from "./models/admin";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/auth/Login.vue"),
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/auth/Register.vue"),
  },
  {
    path: "/",
    component: () => import("@/layouts/BasicLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      ...dashboardRoutes,
      // 管理端路由统一标记为仅管理员可访问
      ...adminRoutes.map((route) => ({
        ...route,
        meta: {
          ...(route.meta || {}),
          adminOnly: true,
        },
      })),
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token;

  if (token) {
    // 已登录用户访问登录页，重定向到首页
    if (to.path === "/login") {
      next("/");
    } else {
      // 如果已登录但用户信息为空（刷新页面导致），尝试获取
      if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
        try {
          await userStore.fetchUserInfo();
          // 加载完用户信息后再做管理员路由权限判断
          const isAdminRoute = to.matched.some(
            (record) => record.meta && record.meta.adminOnly,
          );
          if (isAdminRoute) {
            const role = userStore.userInfo && userStore.userInfo.role;
            if (role !== "admin") {
              ElMessage.error("当前账号无权限访问该页面");
              return next(
                from.fullPath && from.fullPath !== to.fullPath
                  ? from.fullPath
                  : "/",
              );
            }
          }
          next();
        } catch (error) {
          // 根据错误状态码决定行为
          if (error.response?.status === 401) {
            next("/login");
          } else {
            // 非 401 错误（网络、500等）：仍可放行，但提示用户
            ElMessage.error("部分用户信息加载失败，请刷新重试");
            next();
          }
        }
      } else {
        // 已有用户信息，直接做管理员路由权限判断
        const isAdminRoute = to.matched.some(
          (record) => record.meta && record.meta.adminOnly,
        );
        if (isAdminRoute) {
          const role = userStore.userInfo && userStore.userInfo.role;
          if (role !== "admin") {
            ElMessage.error("当前账号无权限访问该页面");
            return next(
              from.fullPath && from.fullPath !== to.fullPath
                ? from.fullPath
                : "/",
            );
          }
        }
        next();
      }
    }
  } else {
    // 未登录用户：需认证页面跳登录，否则放行
    if (to.meta.requiresAuth) {
      next("/login");
    } else {
      next();
    }
  }
});

export default router;
