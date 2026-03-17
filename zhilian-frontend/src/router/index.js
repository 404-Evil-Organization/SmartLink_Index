import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";
import { enforceAdminOnly } from "@/router/permission";

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
      {
        path: "",
        name: "home",
        component: () => import("@/views/home.vue"),
      },
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
          if (enforceAdminOnly(to, from, next, userStore)) {
            return;
          }
          next();
        } catch (error) {
          // 根据错误状态码决定行为
          if (error.response?.status === 401) {
            next("/login");
          } else {
            // 非 401 错误（网络、500等）：管理员路由保持 fail-close，普通路由可继续访问
            if (to.matched.some((record) => record.meta.adminOnly)) {
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
        if (enforceAdminOnly(to, from, next, userStore)) {
          return;
        }
        next();
      }
    }
  } else {
    // 未登录用户：需认证页面跳登录，否则放行
    if (to.matched.some((record) => record.meta.requiresAuth)) {
      next("/login");
    } else {
      next();
    }
  }
});

export default router;
