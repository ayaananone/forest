<!-- src/views/ProfileView.vue -->
<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" text @click="$router.push('/')">
            返回首页
          </el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userStore.userRole }}</el-descriptions-item>
        <el-descriptions-item label="登录时间">{{ loginTime }}</el-descriptions-item>
        <el-descriptions-item label="Token有效期">{{ tokenExpire }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="actions">
        <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
        <el-button @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTokenRemainingTime } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const loginTime = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

const tokenExpire = computed(() => {
  const remaining = getTokenRemainingTime(userStore.token)
  if (remaining <= 0) return '已过期'
  return `${Math.floor(remaining / 60)} 分钟`
})

const handleChangePassword = () => {
  ElMessage.info('功能开发中...')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    await userStore.userLogout()
    router.push('/login')
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.profile-container {
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>