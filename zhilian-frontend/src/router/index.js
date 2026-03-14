import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";

import dashboardRoutes from "./models/dashboard";
import serviceProviderRoutes from "./models/manage";

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
    children: [...dashboardRoutes, ...serviceProviderRoutes],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, _from) => {
  const userStore = useUserStore();
  const token = userStore.token;

  if (token) {
    if (to.path === "/login") {
      return "/";
    } else {
      if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
        try {
          await userStore.fetchUserInfo();
          return true;
        } catch (error) {
          if (error.response?.status === 401) {
            // 401 统一交由 axios 响应拦截器负责跳转至登录页并弹出提示，这里仅中止当前导航以避免重复导航/重复提示
            return false;
          } else {
            ElMessage.error("部分用户信息加载失败，请刷新重试");
            return true;
          }
        }
      } else {
        return true;
      }
    }
  } else {
    if (to.meta.requiresAuth) {
      return "/login";
    } else {
      return true;
    }
  }
});

export default router;