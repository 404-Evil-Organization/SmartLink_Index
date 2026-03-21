export default [
  {
    path: "enterprise",
    name: "Enterprise",
    component: () => import("@/views/enterprise/index.vue"),
    meta: {
      roles: ["manufacture", "service", "admin"],
    },
  },
];
