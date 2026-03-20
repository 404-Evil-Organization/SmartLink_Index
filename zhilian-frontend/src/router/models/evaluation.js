export default [
  {
    path: "evaluation/EvaluationAdd",
    name: "EvaluationAdd",
    component: () => import("@/views/credit/evaluation/EvaluationAdd.vue"),
    meta: { requiresAuth: true },
  },
];
