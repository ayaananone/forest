<template>
  <div class="trend-chart">
    <div class="chart-header">
      <span class="title">📈 生长趋势</span>
      <div class="radio-group">
        <button 
          :class="{ active: timeRange === '5年' }" 
          @click="timeRange = '5年'"
        >5年</button>
        <button 
          :class="{ active: timeRange === '10年' }" 
          @click="timeRange = '10年'"
        >10年</button>
      </div>
    </div>
    
    <div class="chart-container">
      <canvas ref="chartCanvas" class="chart-canvas"></canvas>
    </div>
    
    <!-- 关键指标 -->
    <div class="trend-metrics">
      <div class="metric-item">
        <div class="metric-label">平均增长率</div>
        <div class="metric-value" :style="{ color: growthRateNum >= 0 ? '#67C23A' : '#F56C6C' }">
          {{ growthRateNum >= 0 ? '+' : '' }}{{ growthRate }}%
        </div>
      </div>
      <div class="metric-item">
        <div class="metric-label">预测蓄积</div>
        <div class="metric-value" style="color: #409EFF">
          {{ predictedVolume }} m³
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, toRaw, nextTick, shallowRef, markRaw } from 'vue'
import Chart from 'chart.js/auto'

const props = defineProps({
  data: {
    type: Array,
    default: () => [0, 0, 0, 0, 0]
  }
})

const chartCanvas = ref(null)
const chartInstance = shallowRef(null)
const timeRange = ref('5年')
let updateTimeout = null

// ==================== 计算属性====================
// 获取实际数据长度
const actualDataLength = computed(() => {
  const rawData = props.data || []
  let length = rawData.length
  while (length > 0 && Number(rawData[length - 1]) === 0) {
    length--
  }
  return length
})

// 年均复合增长率（CAGR）
const growthRate = computed(() => {
  const n = actualDataLength.value
  if (n < 2) return '0.0'
  
  const first = Number(props.data[0]) || 0
  const last = Number(props.data[n - 1]) || 0
  
  if (first <= 0 || last <= 0) return '0.0'
  
  // CAGR = (最后/第一)^(1/(年数-1)) - 1
  const years = n - 1
  const cagr = (Math.pow(last / first, 1 / years) - 1) * 100
  
  return cagr.toFixed(1)
})

// 数字类型的增长率（用于样式判断）
const growthRateNum = computed(() => {
  return parseFloat(growthRate.value) || 0
})

// 预测蓄积（基于CAGR计算下一年）
const predictedVolume = computed(() => {
  const n = actualDataLength.value
  if (n === 0) return 0
  
  const last = Number(props.data[n - 1]) || 0
  if (last <= 0) return 0
  
  // 如果只有一年数据，假设增长率为0
  if (n < 2) return Math.round(last)
  
  const cagr = growthRateNum.value / 100
  const nextYear = last * (1 + cagr)
  
  return Math.max(0, Math.round(nextYear))
})

// ==================== 图表方法 ====================

const generateLabels = () => {
  const years = timeRange.value === '5年' ? 5 : 10
  return Array.from({ length: years }, (_, i) => `${i + 1}年`)
}

const processData = () => {
  const rawData = JSON.parse(JSON.stringify(toRaw(props.data) || []))
  const years = timeRange.value === '5年' ? 5 : 10
  
  if (rawData.length === 0) {
    return Array.from({ length: years }, () => 0)
  }
  
  // 获取有效数据（去除末尾0）
  let validData = rawData.map(item => Math.max(0, Number(item)))
  while (validData.length > 0 && validData[validData.length - 1] === 0) {
    validData.pop()
  }
  
  const n = validData.length
  if (n === 0) {
    return Array.from({ length: years }, () => 0)
  }
  
  // 填充数据到指定年份
  let result = [...validData]
  
  // 如果数据不足，用CAGR预测填充
  if (n >= 2 && result.length < years) {
    const first = result[0]
    const last = result[n - 1]
    const cagr = Math.pow(last / first, 1 / (n - 1)) - 1
    
    while (result.length < years) {
      const next = result[result.length - 1] * (1 + cagr)
      result.push(Math.round(next))
    }
  } else if (n === 1 && result.length < years) {
    // 只有一年数据，保持持平
    while (result.length < years) {
      result.push(result[0])
    }
  }
  
  return result.slice(0, years)
}

const initChart = () => {
  if (!chartCanvas.value) {
    console.warn('Canvas not found')
    return
  }
  
  // 销毁旧实例
  if (chartInstance.value) {
    chartInstance.value.stop()
    chartInstance.value.destroy()
    chartInstance.value = null
  }

  const ctx = chartCanvas.value.getContext('2d')
  if (!ctx) {
    console.warn('Cannot get 2d context')
    return
  }
  
  const gradient = ctx.createLinearGradient(0, 0, 0, 200)
  gradient.addColorStop(0, 'rgba(56, 142, 60, 0.3)')
  gradient.addColorStop(1, 'rgba(56, 142, 60, 0.05)')

  const chartData = {
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
  }

  try {
    const chart = new Chart(ctx, {
      type: 'line',
      data: chartData,
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
            displayColors: false,
            callbacks: {
              title: (items) => `第${items[0].label}`,
              label: (context) => {
                const value = Number(context.parsed.y) || 0
                return `平均蓄积: ${value} m³/ha`
              }
            }
          }
        },
        scales: {
          x: {
            title: { display: true, text: '时间', color: '#909399', font: { size: 11 } },
            grid: { display: false }
          },
          y: {
            beginAtZero: true,
            title: { display: true, text: '蓄积量 (m³/ha)', color: '#909399', font: { size: 11 } },
            ticks: { color: '#606266' },
            grid: { color: 'rgba(0,0,0,0.05)' }
          }
        },
        interaction: { intersect: false, mode: 'index' },
        animation: { duration: 600, easing: 'easeOutQuart' }
      }
    })
    
    chartInstance.value = markRaw(chart)
    console.log('Chart initialized successfully')
  } catch (error) {
    console.error('Chart initialization failed:', error)
  }
}

const updateChart = async () => {
  if (updateTimeout) clearTimeout(updateTimeout)
  
  await nextTick()
  
  if (!chartCanvas.value) {
    console.warn('Canvas not available during update')
    return
  }
  
  if (chartInstance.value) {
    try {
      const newData = processData()
      const newLabels = generateLabels()
      
      chartInstance.value.data.labels = newLabels
      chartInstance.value.data.datasets[0].data = newData
      chartInstance.value.update('none')
      console.log('Chart updated with data:', newData)
    } catch (error) {
      console.error('Chart update failed:', error)
      // 如果更新失败，重新初始化
      initChart()
    }
  } else {
    initChart()
  }
}

const debouncedUpdateChart = () => {
  if (updateTimeout) clearTimeout(updateTimeout)
  updateTimeout = setTimeout(updateChart, 100)
}

// 监听变化
watch(() => props.data, (newVal) => {
  console.log('Data changed:', newVal)
  debouncedUpdateChart()
}, { deep: true })

watch(timeRange, (newVal) => {
  console.log('Time range changed:', newVal)
  debouncedUpdateChart()
})

onMounted(() => {
  console.log('Component mounted, initializing chart...')
  nextTick(() => {
    initChart()
  })
})

onUnmounted(() => {
  if (updateTimeout) clearTimeout(updateTimeout)
  if (chartInstance.value) {
    chartInstance.value.stop()
    chartInstance.value.destroy()
    chartInstance.value = null
  }
})
</script>

<style scoped>
.trend-chart {
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

.radio-group {
  display: flex;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.radio-group button {
  padding: 4px 12px;
  border: none;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
  color: #606266;
}

.radio-group button.active {
  background: #2E7D32;
  color: #fff;
}

.chart-container {
  flex: 1;
  position: relative;
  min-height: 0;
  margin-bottom: 12px;
}

.chart-canvas {
  width: 100% !important;
  height: 100% !important;
}

.trend-metrics {
  display: flex;
  justify-content: space-around;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 8px;
  flex-shrink: 0;
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
  font-size: 20px;
  font-weight: bold;
  transition: all 0.3s ease;
}
</style>