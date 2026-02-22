<template>
  <el-card class="chart-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>📊 蓄积量分布</span>
        <el-tooltip content="按每公顷蓄积量统计小班数量" placement="top">
          <el-icon class="info-icon"><Info-Filled /></el-icon>
        </el-tooltip>
      </div>
    </template>
    
    <div class="chart-container">
      <canvas ref="chartCanvas" class="chart-canvas"></canvas>
    </div>
    
    <!-- 统计摘要 -->
    <div class="stats-summary">
      <el-row :gutter="10">
        <el-col :span="12">
          <div class="summary-item">
            <div class="summary-label">最多区间</div>
            <div class="summary-value" :style="{ color: '#2E7D32' }">
              {{ maxRange.label || '-' }}
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="summary-item">
            <div class="summary-label">小班总数</div>
            <div class="summary-value" :style="{ color: '#388E3C' }">
              {{ totalCount }}
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import Chart from 'chart.js/auto'
import { InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartCanvas = ref(null)
const chartInstance = ref(null)

// 区间标签
const rangeLabels = ['<50', '50-100', '100-150', '150-200', '>200']

// 计算统计数据
const stats = computed(() => {
  const ranges = [0, 0, 0, 0, 0] // <50, 50-100, 100-150, 150-200, >200
  
  props.data.forEach(stand => {
    const volume = stand.volumePerHa || 0
    if (volume < 50) ranges[0]++
    else if (volume < 100) ranges[1]++
    else if (volume < 150) ranges[2]++
    else if (volume < 200) ranges[3]++
    else ranges[4]++
  })
  
  return ranges
})

// 计算总数
const totalCount = computed(() => {
  return stats.value.reduce((a, b) => a + b, 0)
})

// 计算最大区间
const maxRange = computed(() => {
  const maxIndex = stats.value.indexOf(Math.max(...stats.value))
  return {
    label: rangeLabels[maxIndex],
    count: stats.value[maxIndex]
  }
})

// 初始化图表
const initChart = () => {
  if (!chartCanvas.value) return

  const ctx = chartCanvas.value.getContext('2d')
  
  // 创建渐变色
  const gradient = ctx.createLinearGradient(0, 0, 0, 200)
  gradient.addColorStop(0, 'rgba(46, 125, 50, 0.8)')
  gradient.addColorStop(1, 'rgba(46, 125, 50, 0.3)')

  chartInstance.value = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: rangeLabels,
      datasets: [{
        label: '小班数量',
        data: [0, 0, 0, 0, 0],
        backgroundColor: gradient,
        borderColor: '#2E7D32',
        borderWidth: 1,
        borderRadius: 4,
        borderSkipped: false
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { 
          display: false 
        },
        tooltip: {
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          titleColor: '#333',
          bodyColor: '#666',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            title: (items) => {
              const item = items[0]
              return `蓄积量: ${item.label} m³/ha`
            },
            label: (context) => {
              const value = context.parsed.y
              const total = context.dataset.data.reduce((a, b) => a + b, 0)
              const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
              return `小班数量: ${value} (${percentage}%)`
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: '蓄积量区间 (m³/ha)',
            color: '#909399',
            font: { size: 11 }
          },
          grid: {
            display: false
          }
        },
        y: {
          beginAtZero: true,
          ticks: { 
            stepSize: 1,
            precision: 0
          },
          title: {
            display: true,
            text: '小班数量',
            color: '#909399',
            font: { size: 11 }
          }
        }
      },
      animation: {
        duration: 800,
        easing: 'easeOutQuart'
      }
    }
  })
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return
  
  chartInstance.value.data.datasets[0].data = stats.value
  chartInstance.value.update()
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

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 16px;
}

.info-icon:hover {
  color: #2E7D32;
}

.chart-container {
  position: relative;
  height: 220px;
  margin-bottom: 16px;
}

.chart-canvas {
  width: 100% !important;
  height: 100% !important;
}

.stats-summary {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  text-align: center;
  padding: 8px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.summary-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 空数据状态 */
:deep(.el-card__body) {
  position: relative;
}

.chart-container:empty::after {
  content: '暂无数据';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #909399;
  font-size: 14px;
}
</style>