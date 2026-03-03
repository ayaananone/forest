<!-- VolumeChart.vue -->
<template>
  <div class="volume-chart">
    <div class="chart-header">
      <div class="stat-info">
        <span class="label">最多区间</span>
        <span class="value">{{ maxRange?.label || '-' }}</span>
        <span class="count">{{ maxRange?.count || 0 }}个小班</span>
      </div>
    </div>
    <div class="chart-wrapper">
      <canvas v-show="totalCount > 0" ref="chartCanvas"></canvas>
      <el-empty v-show="totalCount === 0" description="暂无蓄积量数据" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { ElEmpty } from 'element-plus'
import { useVolumeChart } from '@/composables/useCharts'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartCanvas = ref(null)
const isInitialized = ref(false)

const { 
  stats,
  initChart, 
  updateChart,
  getMaxRange 
} = useVolumeChart()

const totalCount = computed(() => {
  return stats.value?.reduce((sum, item) => sum + (item?.count || 0), 0) || 0
})

const maxRange = computed(() => {
  const range = getMaxRange()
  return range || { label: '-', count: 0 }
})

const refreshChart = async () => {
  if (!isInitialized.value || !chartCanvas.value) return
  if (!props.data || props.data.length === 0) return
  
  await nextTick()
  updateChart(props.data)
}

onMounted(async () => {
  if (chartCanvas.value) {
    initChart(chartCanvas.value, {
      responsive: true,
      maintainAspectRatio: false
    })
    isInitialized.value = true
    refreshChart()
  }
})

watch(() => props.data, () => {
  refreshChart()
}, { deep: true })

onUnmounted(() => {
  // 清理逻辑已在 Hook 中处理
})
</script>

<style scoped>
.volume-chart {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 280px;
  padding: 12px;
  box-sizing: border-box;
}

.chart-header {
  flex-shrink: 0;
  margin-bottom: 16px;
  padding: 0 4px;
}

.stat-info {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
  line-height: 1.4;
}

.stat-info .label {
  color: #666;
  font-weight: 500;
}

.stat-info .value {
  color: #2E7D32;
  font-weight: 700;
  font-size: 15px;
}

.stat-info .count {
  color: #999;
  font-size: 12px;
}

.chart-wrapper {
  flex: 1;
  position: relative;
  min-height: 200px;
  width: 100%;
}

.chart-wrapper canvas {
  width: 100% !important;
  height: 100% !important;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .volume-chart {
    min-height: 240px;
    padding: 8px;
  }
  
  .chart-header {
    margin-bottom: 12px;
  }
  
  .stat-info {
    font-size: 12px;
    gap: 4px;
  }
  
  .stat-info .value {
    font-size: 14px;
  }
  
  .stat-info .count {
    font-size: 11px;
  }
  
  .chart-wrapper {
    min-height: 160px;
  }
}

/* 小屏手机适配 */
@media screen and (max-width: 480px) {
  .volume-chart {
    min-height: 220px;
    padding: 6px;
  }
  
  .chart-wrapper {
    min-height: 140px;
  }
  
  .stat-info .label,
  .stat-info .count {
    display: none;
  }
  
  .stat-info .value {
    font-size: 13px;
  }
}

/* 大屏优化 */
@media screen and (min-width: 1400px) {
  .volume-chart {
    min-height: 320px;
  }
  
  .chart-wrapper {
    min-height: 240px;
  }
}
</style>