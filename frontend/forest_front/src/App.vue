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
        <!-- 统计信息展开按钮 -->
        <div 
          class="sidebar-toggle"
          :class="{ collapsed: sidebarCollapsed }"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <span class="toggle-text">统计信息</span>
          <el-icon :size="12">
            <Arrow-Right v-if="sidebarCollapsed" />
            <Arrow-Left v-else />
          </el-icon>
        </div>

        <!-- 统计面板 - 默认折叠，展开后全屏 -->
        <div class="stats-panel" :class="{ collapsed: sidebarCollapsed }">
          <div class="panel-inner">
            <ChartContainer ref="chartContainerRef" />
          </div>
        </div>
        
        <!-- 地图面板 -->
        <div class="map-panel" :class="{ expanded: sidebarCollapsed }">
          <MapContainer 
            ref="mapContainerRef"
            @stand-select="handleStandSelect"
            @radius-query-result="handleRadiusResult"
            @error="handleMapError"
          />
        </div>
      </div>

      <el-footer class="app-footer" height="30px">
        <span>© 2026 智慧林场系统 | 数据更新时间: {{ updateTime }}</span>
        <span class="footer-right">Node: v18.12.0 | Vue: v3.3.0</span>
      </el-footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { View, Refresh, User, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import ChartContainer from '@/components/charts/ChartContainer.vue'
import MapContainer from '@/components/map/MapContainer.vue'

const chartContainerRef = ref(null)
const mapContainerRef = ref(null)
const updateTime = ref(new Date().toLocaleString('zh-CN'))
// 🔴 默认折叠
const sidebarCollapsed = ref(true)

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

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 统计信息展开按钮 */
.sidebar-toggle {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 16px 8px;
  background: #2E7D32;
  color: #fff;
  cursor: pointer;
  border-radius: 0 6px 6px 0;
  box-shadow: 2px 0 8px rgba(0,0,0,0.2);
  transition: all 0.3s;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 2px;
}

.sidebar-toggle:hover {
  background: #1B5E20;
  padding-left: 12px;
}

.sidebar-toggle.collapsed {
  left: 0;
}

.toggle-text {
  font-size: 13px;
  font-weight: 500;
}

/* 统计面板 - 默认折叠，展开后全屏 */
.stats-panel {
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 30px;
  width: 100%;
  background: #f5f7fa;
  z-index: 200;
  transform: translateX(0);
  transition: transform 0.3s ease;
  overflow: hidden;
}

.stats-panel.collapsed {
  transform: translateX(-100%);
}

.panel-inner {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}

/* 地图面板 */
.map-panel {
  flex: 1;
  position: relative;
  min-width: 0;
  min-height: 0;
  transition: all 0.3s ease;
  height: 100%;
}

.map-panel.expanded {
  margin-left: 0;
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

/* 响应式 */
@media (max-width: 768px) {
  .sidebar-toggle {
    padding: 12px 6px;
    font-size: 12px;
  }
  
  .toggle-text {
    font-size: 12px;
  }
}
</style>