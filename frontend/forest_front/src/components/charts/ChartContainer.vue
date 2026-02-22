<template>
  <div class="chart-container-wrapper">
    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="12" v-for="card in safeStatsCards" :key="card.id">
        <StatsCard
          :title="card.title"
          :value="card.value"
          :icon="card.icon"
          :color="card.color"
          :subtitle="card.subtitle"
        />
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <div class="charts-area">
      <el-row :gutter="16">
        <el-col :span="24">
          <SpeciesChart :data="speciesStats" />
        </el-col>
      </el-row>
      
      <el-row :gutter="16">
        <el-col :xs="24" :sm="24" :md="12">
          <VolumeChart :data="stands" />
        </el-col>
        <el-col :xs="24" :sm="24" :md="12">
          <TrendChart :data="growthData" />
        </el-col>
      </el-row>
    </div>
    
    <!-- 数据加载状态 -->
    <LoadingMask 
      :visible="loading" 
      text="统计图表加载中..."
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
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
const growthData = ref([45, 58, 72, 88, 102])
const loading = ref(false)

// ==================== 计算属性 - 添加空值保护 ====================

const safeStats = computed(() => {
  const totalStands = stands.value?.length || 0
  const totalArea = stands.value?.reduce((sum, s) => sum + (s?.area || 0), 0) || 0
  const totalVolume = stands.value?.reduce((sum, s) => sum + ((s?.volumePerHa || 0) * (s?.area || 0)), 0) || 0
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

// ==================== 方法 ====================

const loadData = async () => {
  loading.value = true
  try {
    const [standsData, statsData] = await Promise.all([
      fetchStands(),
      fetchSpeciesStatistics()
    ])
    
    stands.value = standsData || []
    speciesStats.value = statsData || []
    
    // 计算生长趋势
    const avgVolume = safeStats.value.avgVolume
    if (avgVolume > 0) {
      growthData.value = [
        Math.round(avgVolume * 0.5),
        Math.round(avgVolume * 0.65),
        Math.round(avgVolume * 0.8),
        Math.round(avgVolume * 0.9),
        Math.round(avgVolume)
      ]
    }
    
    ElMessage.success('统计图表加载完成')
  } catch (error) {
    console.error('加载统计图表失败:', error)
    ElMessage.error('数据加载失败')
    // 使用空数据
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

// ==================== 导出方法 ====================

defineExpose({
  refresh: loadData,
  getStats: () => safeStats.value,
  getStands: () => stands.value
})
</script>

<style scoped>
.chart-container-wrapper {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
  position: relative;
}

.stats-row {
  margin-bottom: 16px;
}

.charts-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-container-wrapper::-webkit-scrollbar {
  width: 6px;
}

.chart-container-wrapper::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 3px;
}

.chart-container-wrapper::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .chart-container-wrapper {
    padding: 8px;
  }
  
  .stats-row {
    margin-bottom: 8px;
  }
  
  :deep(.el-col) {
    margin-bottom: 8px;
  }
}
</style>