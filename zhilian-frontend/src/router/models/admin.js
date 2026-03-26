export default [
  {
    path: "admin/tag",
    name: "Tag",
    component: () => import("@/views/admin/TagList.vue"),
  },
  {
    path: "admin/enterprise-audit",
    name: "EnterpriseAudit",
    component: () => import("@/views/admin/EnterpriseAudit.vue"),
  },
  {
    path: "admin/user",
    name: "UserManage",
    component: () => import("@/views/admin/UserManage.vue"),
  },
  {
    path: "admin/demand",
    name: "DemandAudit",
    component: () => import("@/views/admin/DemandAudit.vue"),
  },
];
