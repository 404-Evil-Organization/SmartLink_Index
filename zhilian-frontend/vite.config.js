import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { fileURLToPath, URL } from "node:url";
import { viteMockServe } from "vite-plugin-mock";

export default defineConfig({
  plugins: [
    vue(),
    viteMockServe({
      mockPath: "mock",
      localEnabled: process.env.VITE_MOCK_ENABLED === "true", // 开发环境启用
      enablePrefix: false,
      ignore: ["node_modules"]
    }),
  ],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080", // 后端地址
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
});