// /src/api/auth.js - 认证相关API
import request from './request'

/**
 * 用户登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 */
export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

/**
 * 用户注册
 * @param {object} data - 注册信息
 */
export function register(data) {
  return request.post('/auth/register', data)
}

/**
 * 用户登出
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return request.get('/auth/me')
}

/**
 * 刷新Token
 * @param {string} refreshToken - 刷新令牌
 */
export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', null, {
    headers: {
      'Authorization': `Bearer ${refreshToken}`
    }
  })
}

/**
 * 修改密码
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 */
export function changePassword(oldPassword, newPassword) {
  return request.post('/auth/change-password', {
    oldPassword,
    newPassword
  })
}