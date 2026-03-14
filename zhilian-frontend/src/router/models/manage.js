// src/router/modules/manage.js
export default [
  {
    path: '/manage/service-provider',
    name: 'ServiceProviderList',
    component: () => import('@/views/manage/ServiceProviderList.vue')
  }
  // 后续可继续添加其他管理页面路由
]