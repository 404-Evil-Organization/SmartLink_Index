import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";
import { useUserStore } from "@/stores/user";

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 从环境变量读取后端地址
  timeout: 10000, // 请求超时时间
});

// 请求拦截器：在发送请求之前自动加上token
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    const token = userStore.token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// 响应拦截器：处理返回的数据和错误
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 假设后端返回格式为 { code: 200, message: 'success', data: ... }
    if (res.code !== 200) {
      // 业务错误拦截：通过 config.silent 控制是否静默报错，支持 boolean 或业务状态码（数字）白名单数组（例如：[404, 500]）
      const silent = response.config?.silent;
      const isSilent =
        silent === true || (Array.isArray(silent) && silent.includes(res.code));

      if (res.code === 401) {
        // 处理业务层面的 token 过期或未认证
        const userStore = useUserStore();
        userStore.clearToken();
        router.push("/login");
        if (!isSilent) {
          ElMessage.error(res.message || "登录已过期，请重新登录");
        }
      } else if (res.code === 403) {
        if (!isSilent) {
          ElMessage.error(res.message || "没有权限访问");
        }
      } else {
        if (!isSilent) {
          ElMessage.error(res.message || "请求失败");
        }
      }

      // 返回带有 code 和 message 的错误对象，方便组件 catch 后判断处理
      const error = new Error(res.message || "Error");
      error.code = res.code;
      error.data = res.data;
      return Promise.reject(error);
    }
    return res.data; // 直接返回业务数据，使用时更方便
  },
  (error) => {
    const userStore = useUserStore();
    // silent 配置：可以为 true（全部静默），也可以是状态码/错误类型的数组（如 [404, 500, 'network']）
    const silent = error.config?.silent;
    const isSilent = (codeOrType) =>
      silent === true || (Array.isArray(silent) && silent.includes(codeOrType));

    // 处理HTTP错误状态码

    if (error.response) {
      const status = error.response.status;
      // 鉴权相关的副作用（如清 token 和跳转登录）必须无条件执行，不受 silent 控制
      if (status === 401) {
        userStore.clearToken();
        router.push("/login");
      }

      if (!isSilent(status)) {
        switch (status) {
          case 400:
            ElMessage.error("请求错误");
            break;
          case 401:
            ElMessage.error("登录已过期，请重新登录");
            break;
          case 403:
            ElMessage.error("没有权限访问");
            break;
          case 404:
            ElMessage.error("请求的资源不存在");
            break;
          default:
            ElMessage.error("服务器错误");
        }
      }
    } else {
      if (!isSilent("network")) ElMessage.error("网络连接失败");
    }
    return Promise.reject(error);
  },
);

export default request;
