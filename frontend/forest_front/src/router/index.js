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
    component: () => import('@/views/HomeView.vue'), // 地图展示
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
  const userStore = useUserStore()
  
  // 公开页面
  if (to.meta.public) {
    if (to.path === '/login' && userStore.isLoggedIn) {
      return next('/')  // 改为 return
    }
    return next()  // 改为 return
  }
  
  // 需要登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return next({ path: '/login', query: { redirect: to.fullPath } })  // 改为 return
  }
  
  // 需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.error('需要管理员权限')
    return next('/')  // 改为 return
  }
  
  return next()  // 改为 return
})

export default router