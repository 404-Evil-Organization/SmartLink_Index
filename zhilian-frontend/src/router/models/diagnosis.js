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
    path: "diagnosis/:id",
    name: "DiagnosisReport",
    component: () => import("@/views/diagnosis/Report.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
