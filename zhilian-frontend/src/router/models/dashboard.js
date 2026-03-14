export default [
  {
    path: "", // 空路径表示父路径 '/' 时默认显示该子路由
    name: "Dashboard",
    component: () => import("@/views/dashboard/index.vue"),
  },
];
