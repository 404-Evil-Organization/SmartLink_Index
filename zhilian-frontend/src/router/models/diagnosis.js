export default [
  {
    path: "diagnosis/questionnaire",
    name: "Questionnaire",
    component: () => import("@/views/diagnosis/Questionnaire.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
  {
    path: "diagnosis/report",
    name: "DiagnosisReport",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
  {
    path: "diagnosis/:id(\\d+)",
    name: "DiagnosisReportID",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
