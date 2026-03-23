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

];
