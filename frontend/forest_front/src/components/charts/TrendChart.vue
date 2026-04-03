<!-- TrendChart.vue -->
<template>
  <div class="trend-chart">
    <div class="chart-header">
      <span class="title">📈 生长趋势</span>
      <div class="radio-group">
        <button :class="{ active: timeRange === '5年' }" @click="timeRange = '5年'">5年</button>
        <button :class="{ active: timeRange === '10年' }" @click="timeRange = '10年'">10年</button>
      </div>
    </div>

    <div class="chart-container">
      <canvas ref="chartCanvas" class="chart-canvas"></canvas>
    </div>

    <!-- 关键指标 -->
    <div class="trend-metrics">
      <div class="metric-item">
        <div class="metric-label">平均增长率</div>
        <div class="metric-value" :class="trendMetrics.isPositive ? 'up' : 'down'">
          {{ trendMetrics.isPositive ? '+' : '' }}{{ trendMetrics.growthRate }}%
        </div>
      </div>
      <div class="metric-item">
        <div class="metric-label">预测蓄积</div>
        <div class="metric-value" style="color: #409EFF">
          {{ trendMetrics.predictedVolume }} m³
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, toRef } from 'vue'
import { 
  useTrendChart, 
  useTrendStats,
  calculateGrowthData 
} from '@/composables/useCharts'

const props = defineProps({
  data: { type: Array, default: () => [0, 0, 0, 0, 0] }
})

const chartCanvas = ref(null)
const timeRange = ref('5年')

// ==================== 使用重构后的 Hooks ====================
const { 
  chartInstance, 
  initChart, 
  debouncedUpdate 
} = useTrendChart()

const { trendMetrics } = useTrendStats(toRef(props, 'data'))

// ==================== 生命周期与监听 ====================
onMounted(() => {
  initChart(chartCanvas.value, { years: 10 }) // 预初始化支持10年
  debouncedUpdate(props.data, timeRange.value)
})

watch(() => props.data, (newVal) => {
  debouncedUpdate(newVal, timeRange.value)
}, { deep: true })

watch(timeRange, (newVal) => {
  debouncedUpdate(props.data, newVal)
})

onUnmounted(() => {
  // 清理逻辑已在 Hook 中处理
})
</script>

<style scoped>
/* 样式保持不变 */
.trend-chart { height: 100%; display: flex; flex-direction: column; }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; flex-shrink: 0; }
.title { font-size: 16px; font-weight: 600; color: #2E7D32; }
.radio-group { display: flex; border-radius: 6px; overflow: hidden; border: 1px solid #dcdfe6; }
.radio-group button { padding: 4px 12px; border: none; background: #fff; cursor: pointer; font-size: 12px; transition: all 0.3s; color: #606266; }
.radio-group button.active { background: #2E7D32; color: #fff; }
.chart-container { flex: 1; position: relative; min-height: 0; margin-bottom: 12px; }
.chart-canvas { width: 100% !important; height: 100% !important; }
.trend-metrics { display: flex; justify-content: space-around; padding: 10px; background-color: #f5f7fa; border-radius: 8px; flex-shrink: 0; }
.metric-item { text-align: center; flex: 1; }
.metric-item:first-child { border-right: 1px solid #e0e0e0; }
.metric-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.metric-value { font-size: 20px; font-weight: bold; transition: all 0.3s ease; }
.metric-value.up { color: #67C23A; }
.metric-value.down { color: #F56C6C; }
@media (max-width: 1200px) and (min-width: 769px) {
  .trend-chart {
    height: 100%; /* 填满父容器350px */
    min-height: 100%;
    max-height: 100%;
    display: flex;
    flex-direction: column;
    padding: 12px;
  }

  .chart-header {
    margin-bottom: 10px;
    flex-shrink: 0;
  }

  .chart-container {
    flex: 1; /* 占据剩余空间 */
    position: relative;
    min-height: 0; /* 关键：允许收缩 */
    height: auto;
    margin-bottom: 12px;
  }

  .chart-canvas {
    width: 100% !important;
    height: 100% !important;
  }

  .trend-metrics {
    flex-shrink: 0;
    display: flex;
    justify-content: space-around;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 8px;
    height: auto;
    min-height: 70px;
  }

  .metric-item {
    text-align: center;
    flex: 1;
  }

  .metric-item:first-child {
    border-right: 1px solid #e0e0e0;
  }
}

/* ==================== 移动端 (<768px) ==================== */
@media (max-width: 768px) {
  .trend-chart {
    padding: 8px;
    height: 380px;
    min-height: 380px;
    max-height: 380px;
    display: flex;
    flex-direction: column;
  }

  .chart-header {
    margin-bottom: 10px;
    flex-wrap: wrap;
    gap: 8px;
    flex-shrink: 0;
  }

  .title {
    font-size: 15px;
  }

  .radio-group {
    transform: scale(0.9);
    transform-origin: right center;
  }

  .chart-container {
    flex: 0 0 200px;
    height: 200px;
    min-height: 200px;
    max-height: 200px;
    position: relative;
    margin-bottom: 10px;
  }

  .chart-canvas {
    width: 100% !important;
    height: 100% !important;
  }

  .trend-metrics {
    flex-shrink: 0;
    flex-direction: row;
    gap: 12px;
    padding: 12px;
    height: 80px;
    min-height: 80px;
    max-height: 80px;
  }

  .metric-item {
    flex: 1;
    padding: 8px;
    background: rgba(46, 125, 50, 0.05);
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .metric-item:first-child {
    border-right: none;
  }

  .metric-label {
    font-size: 11px;
    margin-bottom: 4px;
  }

  .metric-value {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .trend-chart {
    height: 340px;
    min-height: 340px;
    max-height: 340px;
  }

  .chart-container {
    flex: 0 0 170px;
    height: 170px;
    min-height: 170px;
    max-height: 170px;
  }

  .trend-metrics {
    padding: 10px;
    gap: 8px;
    height: 70px;
    min-height: 70px;
    max-height: 70px;
  }

  .metric-value {
    font-size: 16px;
  }
  
  .radio-group button {
    padding: 3px 10px;
    font-size: 11px;
  }
}
</style>