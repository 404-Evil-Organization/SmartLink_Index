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
    path: "admin/abroad-case",
    name: "AbroadCaseManage",
    component: () => import("@/views/admin/AbroadCaseManage.vue"),
  },
  {
    path: "admin/demand",
    name: "DemandAudit",
    component: () => import("@/views/admin/DemandAudit.vue"),
  },
  {
    path: "admin/region-index",
    name: "RegionIndexManage",
    component: () => import("@/views/admin/RegionIndexManage.vue"),
  },
];
