import { defineStore } from "pinia";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: localStorage.getItem("token") || "", // 从本地存储读取token
    userInfo: {}, // 用户信息
  }),
  actions: {
    // 设置token（登录成功后调用）
    setToken(token) {
      this.token = token;
      localStorage.setItem("token", token);
    },
    // 清除token（退出登录时调用）
    clearToken() {
      this.token = "";
      this.userInfo = {}; // 同时清空用户信息，避免登出后残留上一个用户的数据
      localStorage.removeItem("token");
    },
    // 设置用户信息
    setUserInfo(info) {
      this.userInfo = info;
    },
  },
});
