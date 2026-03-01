<template>
  <el-config-provider :locale="zhCn">
    <div class="app-wrapper">
      <!-- 顶部导航栏（登录后显示） -->
      <el-header v-if="userStore.isLoggedIn && $route.path !== '/login'" class="app-header" height="60px">
        <div class="header-left">
          <el-icon class="logo-icon"><View /></el-icon>
          <h1 class="app-title">智慧林场综合管理平台</h1>
        </div>
        
        <div class="header-center">
          <el-tag type="success" effect="light" size="small">
            <el-icon><User /></el-icon>
            {{ userStore.username }}
          </el-tag>
          <el-tag 
            v-if="userStore.isAdmin" 
            type="danger" 
            effect="light" 
            size="small"
            style="margin-left: 8px;"
          >
            管理员
          </el-tag>
        </div>
        
        <div class="header-right">
          <el-button type="primary" text @click="refreshAll">
            <el-icon><Refresh /></el-icon> 刷新数据
          </el-button>
          
          <el-divider direction="vertical" />
          
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-menu-trigger">
              <el-avatar 
                :size="32" 
                class="user-avatar"
              >
                {{ userStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <el-icon class="dropdown-icon"><Arrow-Down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="admin" v-if="userStore.isAdmin">
                  <el-icon><Setting /></el-icon> 系统管理
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  View, 
  Refresh, 
  User, 
  ArrowDown,
  SwitchButton, 
  Setting 
} from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const updateTime = ref(new Date().toLocaleString('zh-CN'))

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
}

.logo-icon {
  font-size: 32px;
  color: #81C784;
}

.app-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-menu-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.user-menu-trigger:hover {
  background-color: rgba(255,255,255,0.1);
}

.user-avatar {
  border: 2px solid rgba(255,255,255,0.3);
  background: #388E3C;
  color: #fff;
  font-weight: bold;
}

.dropdown-icon {
  color: rgba(255,255,255,0.8);
  font-size: 12px;
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