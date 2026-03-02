// /src/stores/user.js
import { defineStore } from 'pinia'
import { 
  getToken,
  getRefreshToken,
  getUser, 
  setAuth, 
  clearAuth, 
  isAuthenticated,
  getTokenRemainingTime
} from '@/utils/auth'
import { login, logout, getCurrentUser } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUser(),
    loading: false
  }),

  getters: {
    isLoggedIn: (state) => {
      return !!state.token && isAuthenticated() && getTokenRemainingTime(state.token) > 0
    },
    username: (state) => state.userInfo?.username || '',
    // 修复：添加 roles 数组支持
    roles: (state) => state.userInfo?.roles || [],
    userRole: (state) => state.userInfo?.role || state.userInfo?.roles?.[0] || '',
    // 修复：同时检查 role 字符串和 roles 数组
    isAdmin: (state) => {
      const role = state.userInfo?.role || ''
      const roles = state.userInfo?.roles || []
      
      // 检查 role 字符串
      if (role === 'ROLE_ADMIN' || role === 'ADMIN' || role === 'admin') {
        return true
      }
      
      // 检查 roles 数组
      if (roles.length > 0) {
        return roles.includes('ROLE_ADMIN') || 
               roles.includes('ADMIN') || 
               roles.includes('admin')
      }
      
      return false
    }
  },

  actions: {
    async userLogin(credentials) {
      this.loading = true
      try {
        const res = await login(credentials.username, credentials.password)
        
        console.log('登录响应:', res)
        
        const data = res.data || res
        const accessToken = data.token || data.accessToken
        
        if (!accessToken) {
          console.error('未获取到Token，响应数据:', data)
          ElMessage.error('登录失败：未获取到Token')
          return false
        }

        // 修复：确保存储 roles 数组
        const userData = {
          ...data,
          username: credentials.username,
          // 如果有 roles 数组，同时设置 role 为第一个角色（兼容旧代码）
          role: data.role || (data.roles && data.roles[0]) || '',
          roles: data.roles || [data.role].filter(Boolean) || []
        }

        setAuth(accessToken, data.refreshToken, userData)

        this.token = accessToken
        this.userInfo = getUser()
        
        console.log('登录成功，用户信息:', this.userInfo)
        console.log('isAdmin:', this.isAdmin)
        console.log('roles:', this.roles)
        
        ElMessage.success(`欢迎回来，${this.username || credentials.username}！`)
        return true
        
      } catch (err) {
        console.error('登录错误:', err)
        ElMessage.error(err.response?.data?.message || '登录失败')
        return false
      } finally {
        this.loading = false
      }
    },

    async fetchUserInfo() {
      if (!this.token) return false
      
      try {
        const res = await getCurrentUser()
        const data = res.data || res
        
        // 修复：合并用户信息时保留 roles
        this.userInfo = { 
          ...this.userInfo, 
          ...data,
          roles: data.roles || this.userInfo?.roles || []
        }
        
        const currentToken = getToken()
        const currentRefresh = getRefreshToken()
        setAuth(currentToken, currentRefresh, this.userInfo)
        
        return true
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return false
      }
    },

    async userLogout() {
      console.log('开始执行退出登录...')
      
      try {
        await logout()
        console.log('后端登出接口调用成功')
      } catch (err) {
        console.log('后端登出接口调用失败（不影响）:', err.message)
      }
      
      clearAuth()
      console.log('localStorage 已清除')
      
      this.token = null
      this.userInfo = null
      this.loading = false
      
      console.log('Pinia 状态已重置:', { 
        token: this.token, 
        userInfo: this.userInfo,
        isLoggedIn: this.isLoggedIn 
      })
      
      ElMessage.success('已安全退出')
      
      window.location.href = '/login'
    }
  }
})