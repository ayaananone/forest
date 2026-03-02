// /src/composables/useAutoRefresh.js
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { isTokenExpiringSoon, getToken } from '@/utils/auth'

export function useAutoRefresh() {
  const userStore = useUserStore()
  let timer = null

  const startRefreshTimer = () => {
    // 每30秒检查一次Token状态
    timer = setInterval(() => {
      const token = getToken()
      if (!token) return
      
      // 如果Token将在5分钟内过期，刷新用户信息（会触发request拦截器中的刷新逻辑）
      if (isTokenExpiringSoon(token, 5)) {
        console.log('Token即将过期，准备刷新...')
        // 这里可以调用一个轻量API来触发刷新
        userStore.fetchUserInfo().catch(() => {
          // 如果刷新失败，会在request拦截器中处理
        })
      }
    }, 30000)
  }

  const stopRefreshTimer = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  onMounted(() => {
    if (userStore.isLoggedIn) {
      startRefreshTimer()
    }
  })

  onUnmounted(() => {
    stopRefreshTimer()
  })

  return {
    startRefreshTimer,
    stopRefreshTimer
  }
}