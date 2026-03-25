export default [
  {
    path: "abroad/services",
    name: "ServiceList",
    component: () => import("@/views/abroad/ServiceList.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
  {
    path: "abroad/cases",
    name: "Cases",
    component: () => import("@/views/abroad/Cases.vue"),
    meta: {
      roles: ["manufacture", "admin"],
    },
  },
];
