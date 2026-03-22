export default [
  {
    path: "cooperation/my",
    name: "MyCooperation",
    component: () => import("@/views/credit/cooperation/MyCooperation.vue"),
    meta: {
      roles: ["manufacture", "service", "admin"],
    },
  },
];
