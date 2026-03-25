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
  path: "admin/abroad-case",
  name: "AbroadCaseManage",
  component: () => import("@/views/admin/AbroadCaseManage.vue"),
  meta: { roles: ["admin"] }
  },
];
