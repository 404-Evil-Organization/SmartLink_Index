export default [
  {
    path: "evaluation/add",
    name: "EvaluationAdd",
    component: () => import("@/views/credit/evaluation/EvaluationAdd.vue"),
    meta: { requiresAuth: true },
  },
];
