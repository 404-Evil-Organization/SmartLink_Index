export default [
    {
    path: "diagnosis/report",
    name: "DiagnosisReport",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
  {
    path: "diagnosis/report/:id",      // 支持路径参数（如 /diagnosis/report/5009）
    name: "DiagnosisReportById",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: { roles: ["manufacture", "admin"] },
  },
  
];
