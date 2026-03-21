// src/router/models/manage.js
export default [
  {
    path: 'service/list',
    name: 'serviceList',
    component: () => import('@/views/service/List.vue')
  },
  {
    path: 'credit/service/detail/:id',
    name: 'serviceDetail',
    component: () => import('@/views/credit/service/Detail.vue'),
    meta: { requiresAuth: true }
  }
  // 后续可继续添加其他管理页面路由
]