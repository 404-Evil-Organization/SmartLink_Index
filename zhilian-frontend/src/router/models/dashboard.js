export default [
  {
    path: "dashboard/index",
    name: "DashboardHome",
    component: () => import("@/views/dashboard/index.vue"),
    meta: {
      roles: ["park", "admin"],
    },
  },
  {
    path: "dashboard/region",
    name: "DashboardRegionIndex",
    component: () => import("@/views/dashboard/RegionIndex.vue"),
    meta: {
      roles: ["park", "admin"],
    },
  },
];
