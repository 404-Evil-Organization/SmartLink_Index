export default [
    {
    path: "diagnosis/report",
    name: "DiagnosisReport",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  }, 
];
