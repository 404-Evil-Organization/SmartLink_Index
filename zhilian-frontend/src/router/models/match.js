export default [
  {
    path: "match/my",
    name: "MyDemands",
    component: () => import("@/views/match/MyDemands.vue"),
    meta: {
      roles: ["admin", "manufacture"],
    },
  },
];
