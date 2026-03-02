<template>
  <el-config-provider :locale="zhCn">
    <div class="app-wrapper">
      <!-- 顶部导航栏（登录后显示） -->
      <el-header v-if="userStore.isLoggedIn && $route.path !== '/login'" class="app-header" height="60px">
        <div class="header-left">
          <el-icon class="logo-icon"><View /></el-icon>
          <h1 class="app-title">智慧林场综合管理平台</h1>
        </div>

        <!-- 中间区域 - 系统状态 -->
        <div class="header-center">
          <div class="system-status">
            <el-icon class="status-icon"><Monitor /></el-icon>
            <span class="status-text">系统运行中</span>
            <span class="status-dot"></span>
          </div>
        </div>

        <div class="header-right">
          <el-button type="primary" text @click="refreshAll">
            <el-icon><Refresh /></el-icon> 刷新数据
          </el-button>

          <el-divider direction="vertical" />

          <!-- 优化后的用户菜单 -->
          <el-dropdown @command="handleCommand" trigger="click" popper-class="user-dropdown">
            <div class="user-menu-trigger">
              <div class="avatar-wrapper">
                <el-avatar 
                  :size="36" 
                  class="user-avatar"
                >
                  {{ userStore.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="online-status"></div>
              </div>
              <div class="user-info">
                <span class="username">{{ userStore.username }}</span>
                <span class="user-role">{{ currentRoleDisplay }}</span>
              </div>
              <el-icon class="dropdown-icon"><Arrow-Down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <!-- 用户信息卡片 -->
                <div class="dropdown-user-card">
                  <div class="dropdown-avatar">
                    <el-avatar :size="48" class="user-avatar-large">
                      {{ userStore.username?.charAt(0)?.toUpperCase() }}
                    </el-avatar>
                    <div class="online-status-large"></div>
                  </div>
                  <div class="dropdown-user-info">
                    <div class="dropdown-username">{{ userStore.username }}</div>
                    <div class="dropdown-email">{{ userStore.email || 'user@forest.com' }}</div>
                    <el-tag 
                      :type="currentRoleTagType" 
                      size="small" 
                      effect="light"
                      class="role-tag"
                    >
                      {{ currentRoleDisplay }}
                    </el-tag>
                  </div>
                </div>

                <el-divider class="dropdown-divider" />

                <!-- 菜单项 -->
                <el-dropdown-item command="profile" class="dropdown-item">
                  <div class="menu-item-content">
                    <div class="menu-icon-wrapper blue">
                      <el-icon><User /></el-icon>
                    </div>
                    <div class="menu-text">
                      <span class="menu-title">个人中心</span>
                      <span class="menu-desc">查看和编辑个人信息</span>
                    </div>
                  </div>
                </el-dropdown-item>

                <el-dropdown-item command="admin" v-if="userStore.isAdmin" class="dropdown-item">
                  <div class="menu-item-content">
                    <div class="menu-icon-wrapper purple">
                      <el-icon><Setting /></el-icon>
                    </div>
                    <div class="menu-text">
                      <span class="menu-title">系统管理</span>
                      <span class="menu-desc">用户和权限管理</span>
                    </div>
                  </div>
                </el-dropdown-item>

                <el-divider class="dropdown-divider" />

                <el-dropdown-item command="logout" class="dropdown-item logout-item">
                  <div class="menu-item-content">
                    <div class="menu-icon-wrapper red">
                      <el-icon><SwitchButton /></el-icon>
                    </div>
                    <div class="menu-text">
                      <span class="menu-title">退出登录</span>
                      <span class="menu-desc">安全退出当前账户</span>
                    </div>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 路由出口 -->
      <div class="main-container" :class="{ 'has-header': userStore.isLoggedIn && $route.path !== '/login' }">
        <router-view />
      </div>

      <!-- 底部页脚（登录后显示） -->
      <el-footer v-if="userStore.isLoggedIn && $route.path !== '/login'" class="app-footer" height="30px">
        <span>© 2026 智慧林场系统 | 数据更新时间: {{ updateTime }}</span>
        <span class="footer-right">
          用户: {{ userStore.username }} | Node: v18.12.0 | Vue: v3.3.0
        </span>
      </el-footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  View, 
  Refresh, 
  User, 
  ArrowDown,
  SwitchButton, 
  Setting,
  Monitor
} from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const updateTime = ref(new Date().toLocaleString('zh-CN'))

// 计算当前角色显示
const currentRoleDisplay = computed(() => {
  // 优先使用 store 的 isAdmin getter
  if (userStore.isAdmin) {
    return '超级管理员'
  }
  
  // 检查 roles 数组
  const roles = userStore.roles || []
  if (roles.includes('ROLE_ADMIN') || roles.includes('ADMIN')) {
    return '超级管理员'
  }
  if (roles.includes('ROLE_OPERATOR') || roles.includes('OPERATOR')) {
    return '操作员'
  }
  
  // 检查 userRole
  const role = userStore.userRole
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return '超级管理员'
  if (role === 'ROLE_OPERATOR' || role === 'OPERATOR') return '操作员'
  
  return '普通用户'
})

// 计算角色标签类型
const currentRoleTagType = computed(() => {
  if (userStore.isAdmin) return 'danger'
  
  const roles = userStore.roles || []
  if (roles.includes('ROLE_ADMIN') || roles.includes('ADMIN')) return 'danger'
  
  const role = userStore.userRole
  if (role === 'ROLE_ADMIN' || role === 'ADMIN') return 'danger'
  if (role === 'ROLE_OPERATOR' || role === 'OPERATOR') return 'warning'
  return 'success'
})

const refreshAll = () => {
  updateTime.value = new Date().toLocaleString('zh-CN')
  ElMessage.success('数据刷新完成')
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      await userStore.userLogout()
      window.location.href = '/login'
    } catch {}
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUserInfo?.()
  }
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { height: 100%; width: 100%; overflow: hidden; }
</style>

<style scoped>
.app-wrapper {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-header {
  background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.logo-icon {
  font-size: 32px;
  color: #81C784;
}

.app-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
  white-space: nowrap;
}

.header-center {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  font-size: 13px;
  color: #fff;
  white-space: nowrap;
}

.status-icon {
  font-size: 14px;
  color: #81C784;
}

.status-text {
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #4CAF50;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7); }
  70% { opacity: 0.8; box-shadow: 0 0 0 6px rgba(76, 175, 80, 0); }
  100% { opacity: 1; box-shadow: 0 0 0 0 rgba(76, 175, 80, 0); }
}

.header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  flex: 1;
}

.user-menu-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.user-menu-trigger:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.4);
  background: linear-gradient(135deg, #388E3C 0%, #66BB6A 100%);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.online-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #4CAF50;
  border: 2px solid #2E7D32;
  border-radius: 50%;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.3;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.user-role {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  transition: transform 0.3s;
}

.user-menu-trigger:hover .dropdown-icon {
  transform: rotate(180deg);
}

:deep(.user-dropdown) {
  padding-top: 8px;
}

:deep(.user-dropdown-menu) {
  padding: 0;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, 0.05);
  min-width: 280px;
  overflow: hidden;
}

.dropdown-user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8faf8 0%, #e8f5e9 100%);
}

.dropdown-avatar {
  position: relative;
}

.user-avatar-large {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #388E3C 0%, #66BB6A 100%);
  color: #fff;
  font-weight: 600;
  font-size: 20px;
}

.online-status-large {
  position: absolute;
  bottom: 3px;
  right: 3px;
  width: 12px;
  height: 12px;
  background: #4CAF50;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dropdown-username {
  font-size: 16px;
  font-weight: 600;
  color: #1b5e20;
}

.dropdown-email {
  font-size: 12px;
  color: #666;
}

.role-tag {
  margin-top: 4px;
  width: fit-content;
}

:deep(.dropdown-divider) {
  margin: 0;
  border-color: #e8f5e9;
}

:deep(.dropdown-item) {
  padding: 0;
  margin: 4px 8px;
  border-radius: 10px;
  transition: all 0.2s;
}

:deep(.dropdown-item:hover) {
  background: #f1f8e9;
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.menu-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  transition: transform 0.2s;
}

:deep(.dropdown-item:hover .menu-icon-wrapper) {
  transform: scale(1.1);
}

.menu-icon-wrapper.blue {
  background: linear-gradient(135deg, #42A5F5 0%, #1976D2 100%);
}

.menu-icon-wrapper.purple {
  background: linear-gradient(135deg, #AB47BC 0%, #7B1FA2 100%);
}

.menu-icon-wrapper.red {
  background: linear-gradient(135deg, #EF5350 0%, #D32F2F 100%);
}

.menu-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.menu-desc {
  font-size: 11px;
  color: #999;
}

:deep(.logout-item:hover) {
  background: #ffebee;
}

:deep(.logout-item:hover .menu-title) {
  color: #d32f2f;
}

.main-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.app-footer {
  background-color: #1B5E20;
  color: #C8E6C9;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.footer-right {
  color: #81C784;
}
</style>