// src/router/models/manage.js
export default [
  {
    path: '/manage/service-provider',
    name: 'List',
    component: () => import('@/views/service/List.vue')
  }
  // 后续可继续添加其他管理页面路由
]