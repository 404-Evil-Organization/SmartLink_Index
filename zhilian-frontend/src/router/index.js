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

  if (to.path !== "/login" && to.path !== "/register" && !token) {
    return next("/login");
  }

  if (token) {
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
  } else {
    if (to.meta.requiresAuth) {
      next("/login");
    } else {
      next();
    }
  }
});

export default router;
