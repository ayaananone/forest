// /src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 防止 next 被多次调用的安全包装器
  let called = false
  const safeNext = (...args) => {
    if (!called) {
      called = true
      next(...args)
    }
  }

  try {
    const userStore = useUserStore()
    
    // 公开页面
    if (to.meta.public) {
      if (to.path === '/login' && userStore.isLoggedIn) {
        return safeNext('/')
      }
      return safeNext()
    }
    
    // 需要登录
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      return safeNext({ path: '/login', query: { redirect: to.fullPath } })
    }
    
    // 需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      ElMessage.error('需要管理员权限')
      return safeNext('/')
    }
    
    return safeNext()
    
  } catch (error) {
    console.error('导航守卫错误:', error)
    return safeNext('/login')
  }
})

export default router