import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";
import { enforceRoles, checkRoleAccess } from "@/router/permission";

import dashboardRoutes from "./models/dashboard";
import serviceListRoutes from "./models/service";
import manufactureRoutes from "./models/manufacture";
import adminRoutes from "./models/admin";
import diagnosisRoutes from "./models/diagnosis";
import errorRoutes from "./models/error";
import enterpriseRoutes from "./models/enterprise";

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
  ...errorRoutes,
  {
    path: "/",
    component: () => import("@/layouts/BasicLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      ...dashboardRoutes,
      ...diagnosisRoutes,
      ...serviceListRoutes,
      ...manufactureRoutes,
      ...enterpriseRoutes,
      // 管理端路由统一标记为仅管理员可访问
      ...adminRoutes.map((route) => ({
        ...route,
        meta: {
          ...(route.meta || {}),
          roles: ["admin"],
        },
      })),
    ],
  },
];

// 仅在开发环境注册 chart 测试路由，防止生产环境暴露调试入口
if (import.meta.env.DEV) {
  routes.push({
    path: "/chart-test",
    name: "ChartTest",
    component: () => import("@/views/test/ChartTest.vue"),
    // 即使未来误在非 DEV 环境启用，也要求登录后才能访问
    meta: {
      requiresAuth: true,
    },
  });
}

routes.push({
  path: "/",
  component: () => import("@/layouts/BasicLayout.vue"),
  meta: { requiresAuth: true },
  children: [
    ...dashboardRoutes,
    ...manufactureRoutes,
    // 管理端路由统一标记为仅管理员可访问
    ...adminRoutes.map((route) => ({
      ...route,
      meta: {
        ...(route.meta || {}),
        adminOnly: true,
      },
    })),
  ],
});

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token;

  // 判断当前路由是否需要认证（只要定义了 roles 或 requiresAuth 就需要登录）
  const requiresAuth =
    to.matched.some((record) => record.meta.requiresAuth) || !!to.meta.roles;

  if (token) {
    if (to.path === "/login") {
      next("/");
    } else {
      if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
        try {
          await userStore.fetchUserInfo();
          // 用户信息加载完成后，进行角色权限检查
          if (!checkRoleAccess(to, userStore)) {
            return next("/403"); // 无权限跳转到403页面
          }
          // 基于角色的权限校验
          if (enforceRoles(to, from, next, userStore)) {
            return;
          }
          next();
        } catch (error) {
          if (error.response?.status === 401) {
            // 401 统一交由 axios 响应拦截器负责跳转至登录页并弹出提示，这里仅中止当前导航以避免重复导航/重复提示
            next(false);
          } else {
            // 非 401 错误（网络、500等）：如果目标路由有角色限制，则阻止访问；否则放行但提示
            if (to.meta.roles) {
              ElMessage.error(
                "用户信息加载失败，暂无法验证访问权限，请稍后重试",
              );
              next("/");
            } else {
              ElMessage.error("部分用户信息加载失败，请刷新重试");
              next();
            }
          }
        }
      } else {
        // 已有用户信息，直接检查角色权限
        if (!checkRoleAccess(to, userStore)) {
          return next("/403");
        }
        if (enforceRoles(to, from, next, userStore)) {
          return;
        }
        next();
      }
    }
  } else {
    // 未登录用户：如果需要认证（requiresAuth 或 roles），则跳登录，否则放行
    if (requiresAuth) {
      next("/login");
    } else {
      next(); // 注意这里用 next() 而不是 return true
    }
  }
});

export default router;
