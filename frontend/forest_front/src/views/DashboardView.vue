<template>
  <div class="dashboard">
    <!-- 顶部统计栏 -->
    <div class="stats-bar">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="card in topStats" :key="card.id">
          <div class="stat-item" :style="{ borderColor: card.color }">
            <div class="stat-icon" :style="{ backgroundColor: card.color + '15', color: card.color }">
              <el-icon :size="24">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
              <div class="stat-label">{{ card.title }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧图表面板 -->
      <div class="left-panel" :class="{ collapsed: leftCollapsed }">
        <div class="panel-toggle" @click="leftCollapsed = !leftCollapsed">
          <el-icon>
            <Arrow-Left v-if="!leftCollapsed" />
            <Arrow-Right v-else />
          </el-icon>
        </div>
        
        <div class="panel-content" v-show="!leftCollapsed">
          <SpeciesChart :data="speciesStats" />
          <VolumeChart :data="stands" />
          <TrendChart :data="growthData" />
        </div>
      </div>

      <!-- 中间地图 -->
      <div class="map-wrapper">
        <MapContainer 
          ref="mapRef"
          @stand-select="handleStandSelect"
          @radius-query-result="handleRadiusResult"
        />
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel" :class="{ collapsed: rightCollapsed }">
        <div class="panel-toggle" @click="rightCollapsed = !rightCollapsed">
          <el-icon>
            <Arrow-Right v-if="!rightCollapsed" />
            <Arrow-Left v-else />
          </el-icon>
        </div>
        
        <div class="panel-content" v-show="!rightCollapsed">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <span>📋 林分列表</span>
              <el-button type="primary" link size="small" @click="refreshData">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </template>
            
            <el-input
              v-model="searchKeyword"
              placeholder="搜索林分名称"
              size="small"
              clearable
              :prefix-icon="Search"
            />
            
            <el-table
              :data="filteredStands"
              size="small"
              height="300"
              @row-click="handleRowClick"
            >
              <el-table-column prop="standName" label="名称" show-overflow-tooltip />
              <el-table-column prop="dominantSpecies" label="树种" width="80" />
              <el-table-column prop="area" label="面积" width="80">
                <template #default="{ row }">
                  {{ row.area?.toFixed(1) }}ha
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card class="info-card" shadow="hover">
            <template #header>
              <span>⚡ 快捷操作</span>
            </template>
            <div class="quick-actions">
              <el-button type="primary" plain @click="locateCurrent">
                <el-icon><Aim /></el-icon> 定位当前
              </el-button>
              <el-button type="success" plain @click="exportData">
                <el-icon><Download /></el-icon> 导出数据
              </el-button>
              <el-button type="warning" plain @click="showHeatmap">
                <el-icon><View /></el-icon> 热力图层
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 底部时间轴 -->
    <div class="timeline-bar">
      <el-slider
        v-model="currentYear"
        :min="2019"
        :max="2026"
        :step="1"
        show-stops
        :marks="{ 2019: '2019', 2020: '2020', 2021: '2021', 2022: '2022', 2023: '2023', 2024: '2024', 2025: '2025', 2026: '2026' }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  ArrowRight, 
  Refresh, 
  Search, 
  Aim, 
  Download, 
  View,
  FirstAidKit,
  Location,
  DataLine,
  Histogram
} from '@element-plus/icons-vue'

import SpeciesChart from '@/components/charts/SpeciesChart.vue'
import VolumeChart from '@/components/charts/VolumeChart.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import MapContainer from '@/components/map/MapContainer.vue'
import { fetchStands, fetchSpeciesStatistics } from '@/api/forest'
import { calculateStats } from '@/utils/calculations'
import { formatNumber, formatArea, formatVolume } from '@/utils/formatters'

// ==================== 状态管理 ====================

const mapRef = ref(null)
const leftCollapsed = ref(false)
const rightCollapsed = ref(false)
const searchKeyword = ref('')
const currentYear = ref(2024)
const loading = ref(false)

const stands = ref([])
const speciesStats = ref([])
const growthData = ref([45, 58, 72, 88, 102])

// ==================== 计算属性 ====================

const stats = computed(() => calculateStats(stands.value))

const topStats = computed(() => [
  {
    id: 'stands',
    title: '小班总数',
    value: formatNumber(stats.value.totalStands),
    icon: FirstAidKit,
    color: '#2E7D32'
  },
  {
    id: 'area',
    title: '总面积',
    value: formatArea(stats.value.totalArea),
    icon: Location,
    color: '#388E3C'
  },
  {
    id: 'volume',
    title: '总蓄积',
    value: formatVolume(stats.value.totalVolume),
    icon: Histogram,
    color: '#D32F2F'
  },
  {
    id: 'avg',
    title: '平均蓄积',
    value: stats.value.avgVolume.toFixed(1) + ' m³/ha',
    icon: DataLine,
    color: '#00796B'
  }
])

const filteredStands = computed(() => {
  if (!searchKeyword.value) return stands.value.slice(0, 20)
  const keyword = searchKeyword.value.toLowerCase()
  return stands.value.filter(s => 
    s.standName?.toLowerCase().includes(keyword) ||
    s.dominantSpecies?.includes(keyword)
  ).slice(0, 20)
})

// ==================== 方法 ====================

const loadData = async () => {
  loading.value = true
  try {
    const [standsData, statsData] = await Promise.all([
      fetchStands(),
      fetchSpeciesStatistics()
    ])
    stands.value = standsData
    speciesStats.value = statsData
    
    // 更新生长趋势
    const avgVolume = stats.value.avgVolume
    if (avgVolume > 0) {
      growthData.value = [
        Math.round(avgVolume * 0.5),
        Math.round(avgVolume * 0.65),
        Math.round(avgVolume * 0.8),
        Math.round(avgVolume * 0.9),
        Math.round(avgVolume)
      ]
    }
    
    ElMessage.success('数据加载完成')
  } catch (error) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const handleStandSelect = (standId) => {
  console.log('选中林分:', standId)
}

const handleRadiusResult = (stands, lon, lat, radius) => {
  console.log(`半径${radius}m内找到${stands.length}个林分`)
}

const handleRowClick = (row) => {
  mapRef.value?.highlightStand?.(row.id)
}

const locateCurrent = () => {
  // 获取当前位置
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords
        mapRef.value?.zoomToCoordinate?.(longitude, latitude)
        ElMessage.success('已定位到当前位置')
      },
      () => {
        ElMessage.warning('无法获取当前位置')
      }
    )
  }
}

const exportData = () => {
  // 导出数据逻辑
  ElMessage.info('导出功能开发中...')
}

const showHeatmap = () => {
  mapRef.value?.toggleLayer?.('heatmap', true)
  ElMessage.success('已显示热力图层')
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
}

/* 顶部统计栏 */
.stats-bar {
  background: #fff;
  padding: 12px 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  z-index: 100;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

/* 左侧面板 */
.left-panel {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  position: relative;
  transition: width 0.3s;
}

.left-panel.collapsed {
  width: 32px;
}

/* 右侧面板 */
.right-panel {
  width: 280px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  position: relative;
  transition: width 0.3s;
}

.right-panel.collapsed {
  width: 32px;
}

/* 面板切换按钮 */
.panel-toggle {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 60px;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
}

.left-panel .panel-toggle {
  right: -16px;
  border-radius: 0 4px 4px 0;
}

.right-panel .panel-toggle {
  left: -16px;
  border-radius: 4px 0 0 4px;
}

.panel-toggle:hover {
  background: #c0c4cc;
}

/* 面板内容 */
.panel-content {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.left-panel .panel-content {
  padding-right: 24px;
}

.right-panel .panel-content {
  padding-left: 24px;
}

/* 地图区域 */
.map-wrapper {
  flex: 1;
  position: relative;
  min-height: 400px;
}

/* 信息卡片 */
.info-card {
  margin-bottom: 16px;
}

.info-card :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-weight: 500;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-actions .el-button {
  justify-content: flex-start;
}

/* 底部时间轴 */
.timeline-bar {
  background: #fff;
  padding: 12px 48px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式 */
@media (max-width: 1200px) {
  .left-panel {
    width: 280px;
  }
  .right-panel {
    width: 240px;
  }
}

@media (max-width: 992px) {
  .left-panel, .right-panel {
    position: absolute;
    z-index: 200;
    height: 100%;
    box-shadow: 2px 0 8px rgba(0,0,0,0.15);
  }
  
  .left-panel {
    left: 0;
  }
  
  .left-panel.collapsed {
    transform: translateX(-100%);
    width: 0;
  }
  
  .right-panel {
    right: 0;
  }
  
  .right-panel.collapsed {
    transform: translateX(100%);
    width: 0;
  }
  
  .panel-toggle {
    display: none;
  }
}

@media (max-width: 768px) {
  .stats-bar {
    padding: 8px 12px;
  }
  
  .stat-item {
    padding: 8px 12px;
  }
  
  .stat-icon {
    width: 36px;
    height: 36px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .timeline-bar {
    padding: 8px 24px;
  }
}
</style>