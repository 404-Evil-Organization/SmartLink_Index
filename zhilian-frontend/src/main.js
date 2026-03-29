import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(createPinia())    // 注册状态管理
app.use(router)           // 注册路由
app.use(ElementPlus)      // 注册UI库

app.mount('#app')