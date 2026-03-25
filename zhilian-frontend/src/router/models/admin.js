export default [
  {
    path: "admin/tag",
    name: "Tag",
    component: () => import("@/views/admin/TagList.vue"),
  },
  {
    path: "admin/user",
    name: "UserManage",
    component: () => import("@/views/admin/UserManage.vue"),
  },
  {
  path: "admin/country-guide",
  name: "CountryGuideManage",
  component: () => import("@/views/admin/CountryGuideManage.vue"),
  },
];
