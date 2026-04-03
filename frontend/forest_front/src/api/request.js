// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  getToken, 
  getRefreshToken, 
  setAuth, 
  clearAuth, 
  isTokenExpiringSoon,
  parseToken,
  getTokenRemainingTime
} from '@/utils/auth'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',  // 与Spring Boot后端保持一致
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 允许携带Cookie（如果需要）
})

// ==================== Token刷新机制 ====================

let isRefreshing = false
let refreshSubscribers = []

// 订阅Token刷新
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者新Token
function onTokenRefreshed(newToken) {
  refreshSubscribers.forEach(callback => callback(newToken))
  refreshSubscribers = []
}

// 刷新Token的请求（使用独立axios实例避免拦截器循环）
async function doRefreshToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }

  // 使用原生axios避免拦截器
  const response = await axios.post('/api/auth/refresh', null, {
    headers: {
      'Authorization': `Bearer ${refreshToken}`
    },
    withCredentials: true
  })

  const { token, refreshToken: newRefreshToken, ...userData } = response.data.data || response.data
  
  // 更新存储
  setAuth(token, newRefreshToken || refreshToken, userData)
  
  // 更新Pinia状态
  const userStore = useUserStore()
  if (userStore) {
    userStore.token = token
    userStore.userInfo = userData
  }
  
  return token
}

// ==================== 请求拦截器 ====================

request.interceptors.request.use(
  async config => {
    const token = getToken()
    
    // 没有Token直接放行（公开接口）
    if (!token) {
      return config
    }

    // 登录和刷新接口不需要处理
    if (config.url?.includes('/auth/login') || config.url?.includes('/auth/refresh')) {
      return config
    }

    // 检查Token是否即将过期
    if (isTokenExpiringSoon(token, 5)) {
      // 如果正在刷新，加入等待队列
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh(newToken => {
            config.headers.Authorization = `Bearer ${newToken}`
            resolve(config)
          })
        })
      }

      // 执行刷新
      isRefreshing = true
      try {
        const newToken = await doRefreshToken()
        onTokenRefreshed(newToken)
        config.headers.Authorization = `Bearer ${newToken}`
      } catch (error) {
        // 刷新失败，清除认证并跳转登录
        clearAuth()
        window.location.href = '/login?expired=1'
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    } else {
      // Token正常，直接使用
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================

request.interceptors.response.use(
  // 成功响应
  response => {
    // Spring Boot后端通常返回 { code, message, data }
    const res = response.data
    
    // 如果后端返回自定义错误码
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    
    return res.data !== undefined ? res.data : res  // 统一返回data内容
  },
  
  // 错误处理
  async error => {
    const { response, config, message } = error
    
    // 处理网络错误
    if (!response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }

    const status = response.status
    const errorData = response.data
    const errorMsg = errorData?.message || errorData?.error || message || '请求失败'

    // 401 - 未认证或Token过期
    if (status === 401) {
      // 避免重复刷新
      if (config._retry || config.url?.includes('/auth/refresh')) {
        clearAuth()
        window.location.href = '/login?expired=1'
        return Promise.reject(error)
      }

      config._retry = true

      // 正在刷新中，加入队列
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh(newToken => {
            config.headers.Authorization = `Bearer ${newToken}`
            resolve(request(config))
          })
        })
      }

      // 尝试刷新Token
      isRefreshing = true
      try {
        const newToken = await doRefreshToken()
        onTokenRefreshed(newToken)
        config.headers.Authorization = `Bearer ${newToken}`
        return request(config)  // 重试原请求
      } catch (refreshError) {
        ElMessage.error('登录已过期，请重新登录')
        clearAuth()
        window.location.href = '/login?expired=1'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 403 - 权限不足
    if (status === 403) {
      ElMessage.error('权限不足，无法访问该资源')
      // 如果是管理员接口，提示更明确
      if (config.url?.includes('/admin')) {
        ElMessage.warning('需要管理员权限才能访问')
      }
      return Promise.reject(error)
    }

    // 404 - 资源不存在
    if (status === 404) {
      ElMessage.error('请求的资源不存在')
      return Promise.reject(error)
    }

    // 500+ 服务器错误
    if (status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
      console.error('Server Error:', errorData)
      return Promise.reject(error)
    }

    // 其他错误显示后端消息
    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default request