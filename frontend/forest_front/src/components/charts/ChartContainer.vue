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

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-box species-box">
        <div class="chart-content">
          <SpeciesChart :data="speciesStats" />
        </div>
      </div>

      <div class="chart-box volume-box">
        <div class="chart-content">
          <VolumeChart :data="stands" />
        </div>
      </div>

      <div class="chart-box trend-box">
        <div class="chart-content">
          <TrendChart :data="growthData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatsCard from '@/components/common/StatsCard.vue'
import SpeciesChart from '@/components/charts/SpeciesChart.vue'
import VolumeChart from '@/components/charts/VolumeChart.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import LoadingMask from '@/components/common/LoadingMask.vue'
import { fetchStands, fetchSpeciesStatistics } from '@/api/forest'

// ==================== 使用重构后的 useCharts ====================
import { 
  useForestStats,           // 统计计算
  useForestDataLoader,      // 数据加载
  calculateGrowthData       // 趋势数据计算
} from '@/composables/useCharts'

// ==================== 数据加载逻辑 ====================
const { 
  stands, 
  speciesStats, 
  loading, 
  loadData,
  refresh 
} = useForestDataLoader({
  fetchStands,
  fetchSpeciesStatistics,
  onSuccess: () => ElMessage.success('统计图表加载完成'),
  onError: () => ElMessage.error('部分数据加载失败，请刷新重试'),
  showMessage: ElMessage
})

// ==================== 统计计算（自动响应式）====================
const { safeStatsCards, growthData } = useForestStats(stands)

// ==================== 生命周期 ====================
onMounted(loadData)

// ==================== 导出 ====================
defineExpose({
  refresh,
  getStats: () => stands.value,  // 可通过 useForestStats 获取
  getStands: () => stands.value
})
</script>

<style scoped>
/* 样式保持不变 */
.chart-container {
  padding: 20px;
  position: relative;
  min-width: 320px;
  height: 100%;
  box-sizing: border-box;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  height: calc(100% - 100px);
  min-height: 400px;
}

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
    width: 100%;
    padding: 15px;
    box-sizing: border-box;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>