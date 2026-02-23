<template>
  <div class="chart-container">
    <LoadingMask :visible="loading" text="加载统计数据中..." />
    
    <!-- 统计卡片 -->
    <div class="stats-row">
      <StatsCard 
        v-for="card in safeStatsCards" 
        :key="card.id"
        v-bind="card"
      />
    </div>
    
    <!-- 图表区域 - 三列布局 -->
    <div class="charts-grid">
      <!-- 树种分布 -->
      <div class="chart-box species-box">
        <div class="chart-content">
          <SpeciesChart :data="speciesStats" />
        </div>
      </div>
      
      <!-- 蓄积量分布 -->
      <div class="chart-box volume-box">
        <div class="chart-content">
          <VolumeChart :data="stands" />
        </div>
      </div>
      
      <!-- 生长趋势 -->
      <div class="chart-box trend-box">
        <div class="chart-content">
          <TrendChart :data="growthData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed} from 'vue'
import { ElMessage } from 'element-plus'
import StatsCard from '@/components/common/StatsCard.vue'
import SpeciesChart from '@/components/charts/SpeciesChart.vue'
import VolumeChart from '@/components/charts/VolumeChart.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import LoadingMask from '@/components/common/LoadingMask.vue'
import { fetchStands, fetchSpeciesStatistics } from '@/api/forest'

// ==================== 状态管理 ====================

const stands = ref([])
const speciesStats = ref([])
const loading = ref(false)

// ==================== 计算属性 ====================

const safeStats = computed(() => {
  const totalStands = stands.value?.length || 0
  const totalArea = stands.value?.reduce((sum, s) => sum + (Number(s?.area) || 0), 0) || 0
  const totalVolume = stands.value?.reduce((sum, s) => sum + ((Number(s?.volumePerHa) || 0) * (Number(s?.area) || 0)), 0) || 0
  const avgVolume = totalArea > 0 ? totalVolume / totalArea : 0
  
  return {
    totalStands,
    totalArea,
    totalVolume,
    avgVolume
  }
})

const safeStatsCards = computed(() => [
  {
    id: 'stands',
    title: '小班总数',
    value: safeStats.value.totalStands.toString(),
    icon: 'DataAnalysis',
    color: '#2E7D32',
    subtitle: '林分小班'
  },
  {
    id: 'area',
    title: '总面积',
    value: safeStats.value.totalArea.toFixed(2) + ' ha',
    icon: 'MapLocation',
    color: '#388E3C',
    subtitle: '覆盖区域'
  },
  {
    id: 'volume',
    title: '总蓄积',
    value: safeStats.value.totalVolume > 10000 
      ? (safeStats.value.totalVolume / 10000).toFixed(2) + ' 万m³'
      : safeStats.value.totalVolume.toFixed(2) + ' m³',
    icon: 'TrendCharts',
    color: '#D32F2F',
    subtitle: '估算总量'
  },
  {
    id: 'avg',
    title: '平均蓄积',
    value: safeStats.value.avgVolume.toFixed(2) + ' m³/ha',
    icon: 'Histogram',
    color: '#00796B',
    subtitle: '单位面积'
  }
])

// 计算生长趋势数据（响应式）
const growthData = computed(() => {
  const avgVolume = safeStats.value.avgVolume
  if (avgVolume <= 0) return [0, 0, 0, 0, 0]
  
  return [
    Math.round(avgVolume * 0.5),
    Math.round(avgVolume * 0.65),
    Math.round(avgVolume * 0.8),
    Math.round(avgVolume * 0.9),
    Math.round(avgVolume)
  ]
})

// ==================== 方法 ====================

const loadData = async () => {
  loading.value = true
  
  try {
    // 容错处理：一个接口失败不影响另一个
    const [standsRes, statsRes] = await Promise.allSettled([
      fetchStands(),
      fetchSpeciesStatistics()
    ])
    
    // 单独处理每个接口结果
    stands.value = standsRes.status === 'fulfilled' ? (standsRes.value || []) : []
    speciesStats.value = statsRes.status === 'fulfilled' ? (statsRes.value || []) : []
    
    console.log('数据加载完成:', {
      stands: stands.value.length,
      species: speciesStats.value.length
    })
    
    ElMessage.success('统计图表加载完成')
  } catch (error) {
    console.error('加载统计图表失败:', error)
    ElMessage.error('部分数据加载失败，请刷新重试')
    stands.value = []
    speciesStats.value = []
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
})

// ==================== 导出 ====================

defineExpose({
  refresh: loadData,
  getStats: () => safeStats.value,
  getStands: () => stands.value
})
</script>

<style scoped>
.chart-container {
  padding: 20px;
  position: relative;
  min-width: 320px;
  height: 100%;
  box-sizing: border-box;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

/* 图表网格 - 三列等高布局 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  height: calc(100% - 100px); /* 减去统计卡片高度和间距 */
  min-height: 400px;
}

/* 图表盒子 */
.chart-box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chart-content {
  flex: 1;
  position: relative;
  min-height: 0;
}

/* 响应式 - 小屏幕堆叠 */
@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(3, 1fr);
    height: auto;
    min-height: 1200px;
  }
  
  .chart-box {
    min-height: 350px;
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .chart-container {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>