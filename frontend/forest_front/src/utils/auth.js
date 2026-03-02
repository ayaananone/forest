// /src/utils/auth.js - JWT工具函数（本地存储管理）

const TOKEN_KEY = 'forest_token'
const REFRESH_TOKEN_KEY = 'forest_refresh_token'
const USER_KEY = 'forest_user'
const TOKEN_EXPIRE_KEY = 'forest_token_expire'

// ==================== 基础操作 ====================

/**
 * 存储完整的认证信息
 */
export function setAuth(token, refreshToken, user) {
  localStorage.setItem(TOKEN_KEY, token)
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }
  // 计算并存储过期时间
  const payload = parseToken(token)
  if (payload && payload.exp) {
    localStorage.setItem(TOKEN_EXPIRE_KEY, String(payload.exp * 1000))
  }
}

/**
 * 获取Access Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 获取Refresh Token
 */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUser() {
  const user = localStorage.getItem(USER_KEY)
  try {
    return user ? JSON.parse(user) : null
  } catch {
    return null
  }
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(TOKEN_EXPIRE_KEY)
}

// ==================== 状态检查 ====================

/**
 * 检查是否已登录
 */
export function isAuthenticated() {
  return !!getToken()
}

/**
 * 解析JWT payload
 */
export function parseToken(token) {
  if (!token) return null
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (e) {
    console.error('解析Token失败:', e)
    return null
  }
}

/**
 * 检查Token是否即将过期
 */
export function isTokenExpiringSoon(token, thresholdMinutes = 5) {
  const payload = parseToken(token)
  if (!payload || !payload.exp) return true
  
  const expTime = payload.exp * 1000
  const now = Date.now()
  const threshold = thresholdMinutes * 60 * 1000
  
  return expTime - now < threshold
}

/**
 * 获取Token剩余时间（秒）
 */
export function getTokenRemainingTime(token) {
  const payload = parseToken(token)
  if (!payload || !payload.exp) return 0
  
  return Math.max(0, payload.exp - Math.floor(Date.now() / 1000))
}

// ==================== 关键：更新用户信息 ====================

/**
 * 更新用户信息（保留原有Token）
 */
export function updateUserInfo(user) {
  const currentToken = getToken()
  const currentRefresh = getRefreshToken()
  setAuth(currentToken, currentRefresh, user)
}