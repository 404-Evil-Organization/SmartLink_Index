import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";

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
    children: [],
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
        } catch {
          // 获取失败（如 token 过期），跳转登录页
          userStore.clearToken();
          next("/login");
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
