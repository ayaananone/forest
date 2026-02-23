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
          <span class="legend-name">{{ item.species || '未知' }}</span>
          <span class="legend-value">{{ item.count }}个 ({{ item.percentage }}%)</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, toRaw, nextTick, shallowRef, markRaw } from 'vue'
import Chart from 'chart.js/auto'
import { SPECIES_COLORS } from '@/config'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref(null)
const chartInstance = shallowRef(null)
const legendData = ref([])

const totalCount = computed(() => {
  return props.data.reduce((sum, item) => sum + (Number(item.count) || 0), 0)
})

const initChart = () => {
  if (!chartCanvas.value) return

  if (chartInstance.value) {
    chartInstance.value.destroy()
    chartInstance.value = null
  }

  const ctx = chartCanvas.value.getContext('2d')
  if (!ctx) return

  const rawData = JSON.parse(JSON.stringify(toRaw(props.data) || []))
  const labels = rawData.map(s => s.species || '未知')
  const data = rawData.map(s => Number(s.count) || 0)
  const colors = rawData.map((s, index) => {
    return SPECIES_COLORS[s?.species] || Object.values(SPECIES_COLORS)[index % Object.values(SPECIES_COLORS).length]
  })

  updateLegend(rawData)

  try {
    const chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: colors,
          borderWidth: 2,
          borderColor: '#fff',
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: 'rgba(255, 255, 255, 0.9)',
            titleColor: '#333',
            bodyColor: '#666',
            borderColor: '#e0e0e0',
            borderWidth: 1,
            padding: 12,
            callbacks: {
              label: (context) => {
                const label = context.label || '未知'
                const value = Number(context.parsed) || 0
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
                return `${label}: ${value}个小班 (${percentage}%)`
              }
            }
          }
        },
        cutout: '55%', /* 稍小的内圆 */
        radius: '90%', /* 控制饼图大小，不要100% */
        animation: { animateRotate: true, duration: 800 }
      }
    })
    
    chartInstance.value = markRaw(chart)
  } catch (error) {
    console.error('Chart initialization failed:', error)
  }
}

const updateLegend = (rawData) => {
  const total = rawData.reduce((sum, item) => sum + (Number(item.count) || 0), 0)
  legendData.value = rawData.map((s, index) => {
    const color = SPECIES_COLORS[s?.species] || Object.values(SPECIES_COLORS)[index % Object.values(SPECIES_COLORS).length]
    return {
      species: s.species || '未知',
      count: Number(s.count) || 0,
      color: color,
      percentage: total > 0 ? ((Number(s.count) || 0) / total * 100).toFixed(1) : 0
    }
  })
}

const updateChart = async () => {
  await nextTick()
  if (!chartCanvas.value) return
  
  if (chartInstance.value) {
    const rawData = JSON.parse(JSON.stringify(toRaw(props.data) || []))
    const labels = rawData.map(s => s.species || '未知')
    const data = rawData.map(s => Number(s.count) || 0)
    const colors = rawData.map((s, index) => {
      return SPECIES_COLORS[s?.species] || Object.values(SPECIES_COLORS)[index % Object.values(SPECIES_COLORS).length]
    })
    
    updateLegend(rawData)
    chartInstance.value.data.labels = labels
    chartInstance.value.data.datasets[0].data = data
    chartInstance.value.data.datasets[0].backgroundColor = colors
    chartInstance.value.update('none')
  } else {
    initChart()
  }
}

let timeout = null
const debouncedUpdate = () => {
  clearTimeout(timeout)
  timeout = setTimeout(updateChart, 100)
}

watch(() => props.data, debouncedUpdate, { deep: true })

onMounted(() => {
  nextTick(initChart)
})

onUnmounted(() => {
  clearTimeout(timeout)
  if (chartInstance.value) {
    chartInstance.value.destroy()
    chartInstance.value = null
  }
})
</script>

<style scoped>
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
  max-width: 60%; /* 限制饼图最大宽度 */
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

/* 小屏幕下图例放下面 */
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