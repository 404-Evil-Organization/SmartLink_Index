export default [
  {
    path: "dashboard/index",
    name: "data",
    component: () => import("@/views/dashboard/index.vue"),
  },
  {
    path: "dashboard/region",
    name: "region",
    component: () => import("@/views/dashboard/RegionIndex.vue"),
  },
];
