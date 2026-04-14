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
        <el-button type="primary" @click="showPwdDialog = true">修改密码</el-button>
        <el-button @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px" destroy-on-close>
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPwdChange">确定修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTokenRemainingTime } from '@/utils/auth'
import request from '@/api/request' 

const router = useRouter()
const userStore = useUserStore()

// 控制弹窗显示
const showPwdDialog = ref(false)
// 密码表单数据
const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const loginTime = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

const tokenExpire = computed(() => {
  const remaining = getTokenRemainingTime(userStore.token)
  if (remaining <= 0) return '已过期'
  return `${Math.floor(remaining / 60)} 分钟`
})

// 提交修改密码
const submitPwdChange = async () => {
  // 简单的非空校验
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    return ElMessage.warning('请输入旧密码和新密码')
  }
  if (pwdForm.oldPassword === pwdForm.newPassword) {
    return ElMessage.warning('新密码不能与旧密码相同')
  }

  try {
    await request.post('/auth/change-password', null, { params: pwdForm })
    
    ElMessage.success('密码修改成功，即将重新登录')
    showPwdDialog.value = false
    
    // 密码改完后，强制退出登录回到登录页
    setTimeout(async () => {
      await userStore.userLogout()
      router.push('/login')
    }, 1000)
    
  } catch (error) {
    // 错误拦截器通常会处理错误提示，这里做个兜底
    ElMessage.error(error.message || '修改失败')
  }
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
