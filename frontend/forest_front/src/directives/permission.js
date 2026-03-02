// /src/directives/permission.js
import { useUserStore } from '@/stores/user'

export function setupPermission(app) {
  // v-permission 指令 - 检查是否有特定权限
  app.directive('permission', {
    mounted(el, binding) {
      const userStore = useUserStore()
      const requiredPermission = binding.value
      
      if (!userStore.hasPermission(requiredPermission)) {
        el.remove()
      }
    }
  })
  
  // v-role 指令 - 检查角色
  app.directive('role', {
    mounted(el, binding) {
      const userStore = useUserStore()
      const requiredRole = binding.value
      
      if (userStore.userRole !== requiredRole && userStore.userRole !== 'admin') {
        el.remove()
      }
    }
  })
}