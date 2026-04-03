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

@media (max-width: 1200px) and (min-width: 769px) {
  .chart-container {
    padding: 16px;
    height: auto; /* 关键：移除固定高度 */
    min-height: auto;
    overflow-y: auto; /* 允许滚动 */
  }

  .stats-row {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }

  .charts-grid {
    /* 关键：改为单列布局，固定每个图表高度 */
    grid-template-columns: 1fr;
    grid-template-rows: none; /* 移除重复行定义 */
    gap: 16px;
    height: auto; /* 关键：不限制总高度 */
    min-height: auto;
  }

  .chart-box {
    /* 关键：每个图表固定高度，不拉伸 */
    height: 350px;
    min-height: 350px;
    max-height: 350px;
    overflow: hidden;
  }

  .chart-content {
    height: 100%;
    min-height: auto;
  }
}

/* ==================== 移动端 (<768px) ==================== */
@media (max-width: 768px) {
  .chart-container {
    padding: 12px;
    min-width: auto;
    height: auto;
    min-height: auto;
  }

  .stats-row {
    display: flex;
    flex-wrap: nowrap;
    gap: 10px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    padding-bottom: 8px;
    margin-bottom: 16px;
    height: auto;
  }
  
  .stats-row::-webkit-scrollbar {
    display: none;
  }

  .stats-row :deep(.stats-card) {
    flex: 0 0 auto;
    width: 140px;
    min-width: 140px;
    padding: 12px;
  }

  .charts-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
    height: auto;
    min-height: auto;
  }

  .chart-box {
    min-height: auto;
    height: auto;
    padding: 12px;
    border-radius: 10px;
    overflow: hidden;
  }
  
  .chart-content {
    min-height: auto;
    height: auto;
  }
}

@media (max-width: 480px) {
  .chart-container {
    padding: 8px;
  }

  .chart-box {
    padding: 10px;
    border-radius: 8px;
    box-shadow: 0 1px 6px rgba(0,0,0,0.06);
  }
  
  .stats-row :deep(.stats-card) {
    width: 120px;
    min-width: 120px;
    padding: 10px;
  }
}
</style>