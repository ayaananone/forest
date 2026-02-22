<template>
  <el-config-provider :locale="zhCn">
    <div class="app-container">
      <el-header class="app-header" height="60px">
        <div class="header-left">
          <el-icon class="logo-icon"><View /></el-icon>
          <h1 class="app-title">智慧林场综合管理平台</h1>
        </div>
        <div class="header-right">
          <el-button type="primary" text @click="refreshAll">
            <el-icon><Refresh /></el-icon> 刷新数据
          </el-button>
          <el-divider direction="vertical" />
          <span class="user-info">
            <el-icon><User /></el-icon> 管理员
          </span>
        </div>
      </el-header>

      <div class="main-content">
        <div class="stats-panel">
          <ChartContainer ref="chartContainerRef" />
        </div>
        <div class="map-panel">
          <MapContainer 
            ref="mapContainerRef"
            @stand-select="handleStandSelect"
            @radius-query-result="handleRadiusResult"
            @error="handleMapError"
          />
        </div>
      </div>

      <el-footer class="app-footer" height="30px">
        <span>© 2024 智慧林场系统 | 数据更新时间: {{ updateTime }}</span>
        <span class="footer-right">Node: v18.12.0 | Vue: v3.3.0</span>
      </el-footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { View, Refresh, User } from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import ChartContainer from '@/components/charts/ChartContainer.vue'
import MapContainer from '@/components/map/MapContainer.vue'

const chartContainerRef = ref(null)
const mapContainerRef = ref(null)
const updateTime = ref(new Date().toLocaleString('zh-CN'))

const refreshAll = async () => {
  try {
    ElMessage.info('正在刷新数据...')
    await chartContainerRef.value?.refresh?.()
    updateTime.value = new Date().toLocaleString('zh-CN')
    ElMessage.success('数据刷新完成')
  } catch (error) {
    ElMessage.error('刷新失败: ' + error.message)
  }
}

const handleStandSelect = (standId) => {
  console.log('选中林分:', standId)
  ElNotification({
    title: '林分选中',
    message: `已选中林分编号: ${standId}`,
    type: 'success',
    duration: 2000
  })
}

const handleRadiusResult = (stands, lon, lat, radius) => {
  console.log(`半径${radius}m内找到${stands.length}个林分`)
}

const handleMapError = (error) => {
  console.error('地图错误:', error)
}

onMounted(() => {
  console.log('🌲 智慧林场系统启动完成')
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
</style>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.app-header {
  background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  z-index: 1000;
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
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #E8F5E9;
}

/* 关键：使用 flex 布局确保子元素有尺寸 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0; /* 重要：防止 flex 子项溢出 */
}

.stats-panel {
  width: 340px;
  flex-shrink: 0;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  padding: 16px;
}

.map-panel {
  flex: 1;
  position: relative;
  min-width: 0; /* 重要：防止 flex 子项溢出 */
  min-height: 0;
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

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .stats-panel {
    width: 100%;
    height: 40%;
  }
  
  .map-panel {
    height: 60%;
  }
}
</style>