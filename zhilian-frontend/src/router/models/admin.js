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
  path: "admin/log",
  name: "LogList",
  component: () => import("@/views/admin/LogList.vue"),
  },
];
