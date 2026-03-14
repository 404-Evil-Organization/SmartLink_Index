// src/router/models/dashboard.js
export default [
  {
    path: '', // 空路径表示在父路径 '/' 下默认显示该子路由
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '仪表盘', icon: 'Odometer' }
  }
  // 可继续添加其他仪表盘相关路由
]