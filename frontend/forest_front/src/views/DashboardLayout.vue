
<template>
  <Layout>
    <div class="dashboard-wrapper">
      <!-- 桌面端布局 -->
      <template v-if="!isMobile">
        <button 
          class="sidebar-toggle desktop" 
          :class="{ 'is-collapsed': sidebarCollapsed }"
          @click="toggleSidebar"
        >
          <el-icon :size="16">
            <Arrow-Right v-if="sidebarCollapsed" />
            <Arrow-Left v-else />
          </el-icon>
        </button>

        <aside class="stats-panel desktop" :class="{ collapsed: sidebarCollapsed }">
          <div class="panel-inner">
            <ChartContainer ref="chartContainerRef" />
          </div>
        </aside>
        
        <main class="map-panel" :class="{ expanded: sidebarCollapsed }">
          <MapContainer 
            ref="mapContainerRef"
            @stand-select="handleStandSelect"
            @radius-query-result="handleRadiusResult"
            @error="handleMapError"
          />
        </main>
      </template>
      
      <!-- 移动端布局 -->
      <template v-else>
        <!-- 浮动统计按钮 -->
        <button class="stats-fab" @click="showMobileStats = true">
          <el-icon :size="24"><Data-Line /></el-icon>
          <span>统计</span>
        </button>
        
        <!-- 移动端统计抽屉 -->
        <el-drawer
          v-model="showMobileStats"
          title="数据统计"
          size="92%"
          direction="btt"
          :with-header="true"
          destroy-on-close
          class="mobile-stats-drawer"
        >
          <MobileStatsPanel 
            :stands="stands" 
            :species-stats="speciesStats"
          />
        </el-drawer>
        
        <!-- 地图全屏 -->
        <main class="map-panel mobile-full">
          <MapContainer 
            ref="mapContainerRef"
            @stand-select="handleStandSelect"
            @radius-query-result="handleRadiusResult"
            @error="handleMapError"
          />
        </main>
      </template>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { ArrowLeft, ArrowRight, DataLine } from '@element-plus/icons-vue'
import ChartContainer from '@/components/charts/ChartContainer.vue'
import MobileStatsPanel from '@/components/charts/MobileStatsPanel.vue'
import MapContainer from '@/components/map/MapContainer.vue'
import { fetchStands, fetchSpeciesStatistics } from '@/api/forest'

const sidebarCollapsed = ref(true)
const showMobileStats = ref(false)
const isMobile = ref(false)
const stands = ref([])
const speciesStats = ref([])

// 检测设备类型
const checkDevice = () => {
  isMobile.value = window.innerWidth <= 768
}

let resizeTimer = null
const handleResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(checkDevice, 100)
}

onMounted(() => {
  checkDevice()
  window.addEventListener('resize', handleResize)
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  clearTimeout(resizeTimer)
})

// 加载数据
const loadData = async () => {
  try {
    const [standsRes, statsRes] = await Promise.all([
      fetchStands(),
      fetchSpeciesStatistics()
    ])
    stands.value = standsRes || []
    speciesStats.value = statsRes || []
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

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

/* 桌面端样式 */
.sidebar-toggle.desktop {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1001;
  width: 32px;
  height: 64px;
  background: #2E7D32;
  color: #fff;
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 0 8px rgba(0,0,0,0.2);
  transition: all 0.3s;
}

.sidebar-toggle.desktop:hover {
  background: #1B5E20;
  width: 36px;
}

.sidebar-toggle.desktop.is-collapsed {
  left: 0;
}

.stats-panel.desktop {
  width: 400px;
  background: #f5f7fa;
  transform: translateX(0);
  transition: transform 0.3s ease, width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
  z-index: 100;
}

.stats-panel.desktop.collapsed {
  transform: translateX(-100%);
  width: 0;
}

.panel-inner {
  width: 400px;
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
  transition: margin-left 0.3s;
}

.map-panel.expanded {
  margin-left: 0;
}

/* 移动端样式 */
.stats-fab {
  position: fixed;
  right: 20px;
  bottom: 100px;
  z-index: 1001;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #2E7D32;
  color: #fff;
  border: none;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  font-size: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stats-fab:active {
  transform: scale(0.95);
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.mobile-full {
  width: 100%;
  height: 100%;
}

:deep(.mobile-stats-drawer .el-drawer__body) {
  padding: 0;
  background: #f5f7fa;
}

:deep(.mobile-stats-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 600;
}
</style>