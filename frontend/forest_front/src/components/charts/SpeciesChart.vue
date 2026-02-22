<template>
  <el-card class="chart-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>🌲 树种分布</span>
        <el-tag size="small" type="success">{{ totalCount }}个小班</el-tag>
      </div>
    </template>
    
    <div class="chart-container">
      <canvas ref="chartCanvas" class="chart-canvas"></canvas>
    </div>
    
    <!-- 自定义图例 -->
    <div class="legend-container">
      <div v-if="legendData.length === 0" class="empty-text">
        暂无数据
      </div>
      <div 
        v-for="(item, index) in legendData" 
        :key="index"
        class="legend-item"
      >
        <span 
          class="legend-color" 
          :style="{ backgroundColor: item.color }"
        ></span>
        <span class="legend-name">{{ item.species }}</span>
        <span class="legend-value">
          {{ item.count }}个 ({{ item.percentage }}%)
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import Chart from 'chart.js/auto'
import { SPECIES_COLORS } from '@/config'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref(null)
const chartInstance = ref(null)
const legendData = ref([])

// 计算总数量
const totalCount = computed(() => {
  return props.data.reduce((sum, item) => sum + (item.count || 0), 0)
})

// 初始化图表
const initChart = () => {
  if (!chartCanvas.value) return

  const ctx = chartCanvas.value.getContext('2d')
  
  chartInstance.value = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: [],
      datasets: [{
        data: [],
        backgroundColor: Object.values(SPECIES_COLORS),
        borderWidth: 2,
        borderColor: '#fff',
        hoverOffset: 4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false // 使用自定义图例
        },
        tooltip: {
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          titleColor: '#333',
          bodyColor: '#666',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            label: (context) => {
              const label = context.label || ''
              const value = context.parsed || 0
              const total = context.dataset.data.reduce((a, b) => a + b, 0)
              const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
              return `${label}: ${value}个小班 (${percentage}%)`
            }
          }
        }
      },
      cutout: '60%',
      animation: {
        animateRotate: true,
        duration: 1000
      }
    }
  })
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance.value || !props.data) return

  const labels = props.data.map(s => s.species || '未知')
  const data = props.data.map(s => s.count || 0)
  const total = data.reduce((a, b) => a + b, 0)

  // 更新图表
  chartInstance.value.data.labels = labels
  chartInstance.value.data.datasets[0].data = data
  chartInstance.value.update()

  // 更新图例数据
  legendData.value = props.data.map((s, index) => {
    const color = SPECIES_COLORS[s.species] || 
                  Object.values(SPECIES_COLORS)[index % Object.values(SPECIES_COLORS).length]
    
    return {
      ...s,
      color: color,
      percentage: total > 0 ? ((s.count / total) * 100).toFixed(1) : 0
    }
  })
}

// 监听数据变化
watch(() => props.data, updateChart, { deep: true, immediate: true })

onMounted(() => {
  initChart()
  if (props.data && props.data.length > 0) {
    updateChart()
  }
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.destroy()
    chartInstance.value = null
  }
})
</script>

<style scoped>
.chart-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2E7D32;
}

.chart-container {
  position: relative;
  height: 200px;
  margin-bottom: 16px;
}

.chart-canvas {
  width: 100% !important;
  height: 100% !important;
}

.legend-container {
  max-height: 150px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.empty-text {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  font-size: 12px;
  border-bottom: 1px solid #ebeef5;
}

.legend-item:last-child {
  border-bottom: none;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  margin-right: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  color: #303133;
  font-weight: 500;
}

.legend-value {
  color: #606266;
  font-size: 11px;
}
</style>