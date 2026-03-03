<template>
  <div class="species-chart">
    <div class="chart-header">
      <span class="title">🌲 树种分布</span>
      <el-tag size="small" type="success">{{ totalCount }}个小班</el-tag>
    </div>

    <div class="chart-body">
      <div class="chart-wrapper">
        <canvas ref="chartCanvas"></canvas>
      </div>

      <!-- 图例 -->
      <div class="legend-wrapper">
        <div 
          v-for="(item, index) in legendData" 
          :key="index"
          class="legend-item"
        >
          <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
          <span class="legend-name">{{ item.species }}</span>
          <span class="legend-value">{{ item.count }}个 ({{ item.percentage }}%)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useSpeciesChart, processSpeciesStats, safeNumber } from '@/composables/useCharts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref(null)

// ==================== 使用重构后的 Hook ====================
const { 
  chartInstance, 
  legendData, 
  initChart, 
  debouncedUpdate 
} = useSpeciesChart()

// ==================== 计算属性 ====================
const totalCount = computed(() => {
  return props.data.reduce((sum, item) => sum + safeNumber(item?.count), 0)
})

// ==================== 生命周期 ====================
onMounted(() => {
  initChart(chartCanvas.value)
  debouncedUpdate(props.data)
})

onUnmounted(() => {
  // 清理逻辑已在 useSpeciesChart 中处理
})

// ==================== 监听数据变化 ====================
watch(() => props.data, (newVal) => {
  debouncedUpdate(newVal)
}, { deep: true })
</script>

<style scoped>
/* 样式保持不变 */
.species-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #2E7D32;
}

.chart-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.chart-wrapper {
  flex: 1;
  position: relative;
  min-width: 0;
  min-height: 0;
  max-width: 60%;
}

canvas {
  width: 100% !important;
  height: 100% !important;
  max-height: 100%;
}

.legend-wrapper {
  width: 40%;
  max-width: 150px;
  overflow-y: auto;
  padding-left: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.legend-item:last-child {
  border-bottom: none;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 8px;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.legend-value {
  color: #606266;
  font-size: 11px;
  flex-shrink: 0;
  margin-left: 4px;
}

@media (max-width: 400px) {
  .chart-body {
    flex-direction: column;
  }

  .chart-wrapper {
    max-width: 100%;
    height: 60%;
  }

  .legend-wrapper {
    width: 100%;
    max-width: none;
    height: 40%;
  }
}
</style>