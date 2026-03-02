<!-- /src/components/DashboardLayout.vue -->
<template>
  <Layout>
    <div class="dashboard-wrapper">
      <!-- 统计信息展开按钮 -->
      <div 
        class="sidebar-toggle"
        :class="{ collapsed: sidebarCollapsed, expanded: !sidebarCollapsed }"
        @click="toggleSidebar"
      >
        <span class="toggle-text">{{ sidebarCollapsed ? '展开统计' : '收起统计' }}</span>
        <el-icon :size="12">
          <Arrow-Right v-if="sidebarCollapsed" />
          <Arrow-Left v-else />
        </el-icon>
      </div>

      <!-- 统计面板 -->
      <div class="stats-panel" :class="{ collapsed: sidebarCollapsed, expanded: !sidebarCollapsed }">
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
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import { ElNotification } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import Layout from './Layout.vue'
import ChartContainer from '@/components/charts/ChartContainer.vue'
import MapContainer from '@/components/map/MapContainer.vue'

const sidebarCollapsed = ref(true)
const chartContainerRef = ref(null)
const mapContainerRef = ref(null)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleStandSelect = (standId) => {
  ElNotification({
    title: '林分选中',
    message: `已选中林分编号: ${standId}`,
    type: 'success',
    duration: 2000
  })
}

const handleRadiusResult = (stands, lon, lat, radius) => {
  ElNotification({
    title: '半径查询完成',
    message: `找到 ${stands.length} 个林分`,
    type: 'success',
    duration: 3000
  })
}

const handleMapError = (error) => {
  console.error('地图错误:', error)
}
</script>

<style scoped>
.dashboard-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
  height: 100%;
}

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
  transition: all 0.3s ease;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  letter-spacing: 2px;
}

.sidebar-toggle:hover {
  background: #1B5E20;
  padding-left: 12px;
}

/* 展开状态时，按钮移动到面板右侧 */
.sidebar-toggle.expanded {
  left: auto;
  right: 10px;
  border-radius: 6px 0 0 6px;
  box-shadow: -2px 0 8px rgba(0,0,0,0.2);
}

.toggle-text {
  font-size: 13px;
  font-weight: 500;
}

.stats-panel {
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 30px;
  width: 400px;
  background: #f5f7fa;
  z-index: 200;
  transform: translateX(0);
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
}

/* 收起状态 */
.stats-panel.collapsed {
  transform: translateX(-100%);
  width: 400px;
}

/* 展开状态 - 全屏宽度 */
.stats-panel.expanded {
  transform: translateX(0);
  width: 100%;
  z-index: 300;
}

.panel-inner {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}

.map-panel {
  flex: 1;
  position: relative;
  min-width: 0;
  min-height: 0;
  transition: all 0.3s ease;
  height: 100%;
  margin-left: 400px;
}

.map-panel.expanded {
  margin-left: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .stats-panel {
    width: 100%;
    top: 60px;
    bottom: 0;
  }
  
  .stats-panel.collapsed {
    width: 100%;
    transform: translateX(-100%);
  }
  
  .stats-panel.expanded {
    width: 100%;
  }
  
  .sidebar-toggle.expanded {
    left: auto;
    right: 10px;
  }
  
  .map-panel {
    margin-left: 0;
  }
}
</style>