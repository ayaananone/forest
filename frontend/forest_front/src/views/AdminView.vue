<!-- src/views/AdminView.vue -->
<template>
  <div class="admin-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统管理</span>
          <el-button type="primary" text @click="$router.push('/')">
            返回首页
          </el-button>
        </div>
      </template>
      
      <el-tabs>
        <el-tab-pane label="用户管理">
          <p>用户管理功能（需要管理员权限）</p>
          <el-table :data="users" style="width: 100%">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="role" label="角色" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="系统设置">
          <el-form label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="settings.systemName" />
            </el-form-item>
            <el-form-item label="数据备份">
              <el-button type="primary">立即备份</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const users = ref([
  { username: 'admin', role: '管理员', status: '正常' },
  { username: 'operator1', role: '操作员', status: '正常' }
])

const settings = ref({
  systemName: '智慧林场管理系统'
})
</script>

<style scoped>
.admin-container {
  padding: 40px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>