export default [
  {
    path: "diagnosis/questionnaire",
    name: "Questionnaire",
    component: () => import("@/views/diagnosis/Questionnaire.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
