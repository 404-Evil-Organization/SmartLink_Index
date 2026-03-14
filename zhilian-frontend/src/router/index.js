import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";

import dashboardRoutes from "./models/dashboard";
import ManufactureRoutes from "./models/manage";

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
    children: [...dashboardRoutes, ...ManufactureRoutes],
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
