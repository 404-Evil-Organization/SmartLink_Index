import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import { fileURLToPath, URL } from "node:url";
import { viteMockServe } from "vite-plugin-mock";

export default defineConfig(({ mode }) => {
  // 加载环境变量，mode 为当前模式（development/production）
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [
      vue(),
      viteMockServe({
        mockPath: "mock",                       // mock 文件存放目录
        enable: env.VITE_MOCK_ENABLED === "true", // 根据环境变量开启 mock
      }),
    ],
    server: {
      proxy: {
        // 如果需要代理，可以在这里配置，例如：
        // '/api': {
        //   target: 'http://localhost:8080',
        //   changeOrigin: true,
        // }
      },
    },
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
  };
});