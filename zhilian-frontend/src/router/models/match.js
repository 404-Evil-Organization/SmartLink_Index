export default [
  {
    path: "match/market",
    name: "Market",
    component: () => import("@/views/match/Market.vue"),
    meta: {
      roles: ["service", "admin"],
    },
  },
];
