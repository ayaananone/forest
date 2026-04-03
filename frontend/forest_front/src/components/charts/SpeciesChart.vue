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

@media (max-width: 1200px) and (min-width: 769px) {
  .species-chart {
    height: 100%; /* 填满父容器 */
    display: flex;
    flex-direction: column;
  }

  .chart-header {
    margin-bottom: 10px;
    flex-shrink: 0;
  }

  .chart-body {
    flex: 1;
    display: flex;
    gap: 16px;
    min-height: 0; /* 关键：允许收缩 */
    max-height: calc(100% - 50px); /* 减去头部高度 */
  }

  .chart-wrapper {
    flex: 1;
    position: relative;
    min-width: 0;
    max-width: 60%;
    height: 100%; /* 填满可用空间 */
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
    height: 100%; /* 填满高度 */
  }
}

/* ==================== 移动端 (<768px) ==================== */
@media (max-width: 768px) {
  .species-chart {
    padding: 8px;
    height: auto; /* 不限制高度 */
  }

  .chart-header {
    margin-bottom: 10px;
  }

  .title {
    font-size: 15px;
  }

  .chart-body {
    flex-direction: column;
    gap: 12px;
    max-height: 400px; /* 限制最大高度 */
    height: auto;
  }

  .chart-wrapper {
    max-width: 100%;
    aspect-ratio: 1 / 1;
    height: auto;
    max-height: 280px;
    min-height: auto;
    margin: 0 auto;
  }

  canvas {
    width: 100% !important;
    height: 100% !important;
    object-fit: contain;
  }

  .legend-wrapper {
    width: 100%;
    max-width: none;
    display: flex;
    flex-wrap: nowrap;
    gap: 12px;
    overflow-x: auto;
    padding: 8px 0;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    max-height: 80px;
    height: auto;
  }
  
  .legend-wrapper::-webkit-scrollbar {
    display: none;
  }

  .legend-item {
    flex: 0 0 auto;
    padding: 6px 10px;
    background: #f5f7fa;
    border-radius: 16px;
    border-bottom: none;
    white-space: nowrap;
  }

  .legend-color {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-right: 6px;
  }

  .legend-name {
    font-size: 12px;
  }

  .legend-value {
    font-size: 11px;
    color: #909399;
    margin-left: 4px;
  }
}

@media (max-width: 480px) {
  .chart-body {
    max-height: 350px;
  }
  
  .chart-wrapper {
    max-height: 220px;
  }
  
  .legend-wrapper {
    max-height: 70px;
  }
  
  .legend-item {
    padding: 4px 8px;
    font-size: 11px;
  }
}
</style>