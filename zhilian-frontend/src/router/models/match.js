export default [
  {
    path: "match/market",
    name: "Market",
    component: () => import("@/views/match/Market.vue"),
    meta: {
      roles: ["service", "admin"],
    },
  },
  {
    path: "match/my",
    name: "MyDemands",
    component: () => import("@/views/match/MyDemands.vue"),
    meta: {
      roles: ["admin", "manufacture"],
    },
  },
];
