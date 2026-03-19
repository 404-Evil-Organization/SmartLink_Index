// src/router/models/manage.js
export default [
  {
    path: 'service/list',
    name: 'serviceList',
    component: () => import('@/views/service/List.vue')
  }
  // 后续可继续添加其他管理页面路由
]