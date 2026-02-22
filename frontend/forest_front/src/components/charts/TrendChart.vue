<template>
  <el-card class="chart-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>📈 生长趋势</span>
        <el-radio-group v-model="timeRange" size="small">
          <el-radio-button label="5年">5年</el-radio-button>
          <el-radio-button label="10年">10年</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    
    <div class="chart-container">
      <canvas ref="chartCanvas" class="chart-canvas"></canvas>
    </div>
    
    <!-- 关键指标 -->
    <div class="trend-metrics">
      <div class="metric-item">
        <div class="metric-label">平均增长率</div>
        <div class="metric-value" :style="{ color: growthRate >= 0 ? '#67C23A' : '#F56C6C' }">
          {{ growthRate >= 0 ? '+' : '' }}{{ growthRate }}%
        </div>
      </div>
      <div class="metric-item">
        <div class="metric-label">预测蓄积</div>
        <div class="metric-value" style="color: #409EFF">
          {{ predictedVolume }} m³
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import Chart from 'chart.js/auto'

const props = defineProps({
  data: {
    type: Array,
    default: () => [0, 0, 0, 0, 0]
  }
})

const chartCanvas = ref(null)
const chartInstance = ref(null)
const timeRange = ref('5年')

// 计算增长率
const growthRate = computed(() => {
  if (!props.data || props.data.length < 2) return 0
  const first = props.data[0] || 0
  const last = props.data[props.data.length - 1] || 0
  if (first === 0) return 0
  return (((last - first) / first) * 100).toFixed(1)
})

// 计算预测蓄积（下一年）
const predictedVolume = computed(() => {
  if (!props.data || props.data.length === 0) return 0
  const last = props.data[props.data.length - 1] || 0
  const growth = parseFloat(growthRate.value) / 100
  return Math.round(last * (1 + growth))
})

// 生成标签
const generateLabels = () => {
  const years = timeRange.value === '5年' ? 5 : 10
  return Array.from({ length: years }, (_, i) => `${i + 1}年`)
}

// 处理数据
const processData = () => {
  const years = timeRange.value === '5年' ? 5 : 10
  let data = [...props.data]
  
  // 如果数据不足，补充预测值
  while (data.length < years) {
    const last = data[data.length - 1] || 0
    const growth = parseFloat(growthRate.value) / 100
    data.push(Math.round(last * (1 + growth)))
  }
  
  // 如果数据过多，截取
  return data.slice(0, years)
}

// 初始化图表
const initChart = () => {
  if (!chartCanvas.value) return

  const ctx = chartCanvas.value.getContext('2d')
  
  // 创建渐变色
  const gradient = ctx.createLinearGradient(0, 0, 0, 200)
  gradient.addColorStop(0, 'rgba(56, 142, 60, 0.3)')
  gradient.addColorStop(1, 'rgba(56, 142, 60, 0.05)')

  chartInstance.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: generateLabels(),
      datasets: [{
        label: '平均蓄积增长',
        data: processData(),
        borderColor: '#388E3C',
        backgroundColor: gradient,
        fill: true,
        tension: 0.4,
        pointRadius: 5,
        pointBackgroundColor: '#388E3C',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointHoverRadius: 7,
        pointHoverBackgroundColor: '#2E7D32',
        pointHoverBorderColor: '#fff',
        pointHoverBorderWidth: 3
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
          displayColors: false,
          callbacks: {
            title: (items) => {
              return `第${items[0].label}`
            },
            label: (context) => {
              return `平均蓄积: ${context.parsed.y} m³/ha`
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: '时间',
            color: '#909399',
            font: { size: 11 }
          },
          grid: {
            display: false
          }
        },
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: '蓄积量 (m³/ha)',
            color: '#909399',
            font: { size: 11 }
          },
          ticks: {
            color: '#606266'
          },
          grid: {
            color: 'rgba(0,0,0,0.05)'
          }
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      },
      animation: {
        duration: 1000,
        easing: 'easeOutQuart'
      }
    }
  })
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return
  
  chartInstance.value.data.labels = generateLabels()
  chartInstance.value.data.datasets[0].data = processData()
  chartInstance.value.update()
}

// 监听变化
watch(() => props.data, updateChart, { deep: true })
watch(timeRange, updateChart)

onMounted(() => {
  initChart()
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

:deep(.el-radio-button__inner) {
  padding: 4px 12px;
  font-size: 12px;
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

.trend-metrics {
  display: flex;
  justify-content: space-around;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.metric-item {
  text-align: center;
  flex: 1;
}

.metric-item:first-child {
  border-right: 1px solid #e0e0e0;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s ease;
}
</style>