// /src/main.js - 应用入口
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 权限指令
import { setupPermission } from '@/directives/permission'

/*import * as Cesium from 'cesium'

Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2NWZjYzgzNi1kZDkwLTRhZmEtYWM1NS0zMzc2N2Y2YWVjZDEiLCJpZCI6NDEzMjI4LCJpYXQiOjE3NzUyMDEwMDd9.bieYe4Lmk0kYT07iZUwzuPT7kJ5bAnuSWB9bNwMlrB8'
*/
const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err, info)
  // 可以发送到错误监控服务
}

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  zIndex: 3000,
  size: 'default',
  button: {
    autoInsertSpace: true
  }
})

// 注册权限指令
setupPermission(app)

// 挂载应用
app.mount('#app')

console.log('🌲 智慧林场系统启动完成 | JWT认证已启用')