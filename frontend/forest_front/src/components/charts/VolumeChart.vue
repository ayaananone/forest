<template>
  <div class="volume-chart">
    <div class="chart-header">
      <div class="stat-info">
        <span class="label">最多区间</span>
        <span class="value">{{ maxRange.label }}</span>
        <span class="count">{{ maxRange.count }}个小班</span>
      </div>
    </div>
    <div class="chart-wrapper">
      <canvas v-if="totalCount > 0" ref="chartCanvas"></canvas>
      <el-empty v-else description="暂无蓄积量数据" :image-size="80" class="empty-placeholder" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, toRaw } from 'vue'
import Chart from 'chart.js/auto'
import { ElEmpty } from 'element-plus'

const props = defineProps({
  data: {
    type: Array,
    default: () => [],
    validator: (val) => {
      return val.every(item => {
        if (item.volumePerHa) return typeof item.volumePerHa === 'number'
        return true
      })
    }
  }
})

const chartCanvas = ref(null)
const chartInstance = ref(null)

// 区间标签
const rangeLabels = ['<50', '50-100', '100-150', '150-200', '>200']

// 计算统计数据
const stats = computed(() => {
  const ranges = [0, 0, 0, 0, 0]
  
  const rawData = toRaw(props.data)
  if (!Array.isArray(rawData)) return ranges
  
  rawData.forEach(stand => {
    const volume = Number(stand?.volumePerHa) || 0
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

// 计算最大区间（支持多个最大值）
const maxRange = computed(() => {
  if (totalCount.value === 0) return { label: '-', count: 0 }
  
  const maxValue = Math.max(...stats.value)
  const maxIndices = stats.value.reduce((acc, val, idx) => {
    if (val === maxValue) acc.push(idx)
    return acc
  }, [])
  
  if (maxIndices.length > 1) {
    return {
      label: maxIndices.map(i => rangeLabels[i]).join('/'),
      count: maxValue
    }
  }
  
  return {
    label: rangeLabels[maxIndices[0]],
    count: maxValue
  }
})

// 初始化图表
const initChart = () => {
  if (!chartCanvas.value || totalCount.value === 0) return

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
        data: stats.value,
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
              const value = Number(context.parsed.y) || 0
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
            precision: 0,
            // 动态步长
            callback: function(value, index) {
              const max = this.chart.scales.y.max || 10
              const step = max > 20 ? Math.ceil(max / 10) : 1
              return index % step === 0 ? value : ''
            }
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
  if (!chartInstance.value) {
    initChart()
    return
  }
  
  chartInstance.value.data.datasets[0].data = stats.value
  chartInstance.value.update()
}

// 防抖处理
const debouncedUpdateChart = (() => {
  let timeout = null
  return () => {
    clearTimeout(timeout)
    timeout = setTimeout(updateChart, 300)
  }
})()

// 监听数据变化
watch(() => props.data, debouncedUpdateChart, { deep: true, immediate: true })

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.destroy()
    chartInstance.value = null
  }
  clearTimeout(debouncedUpdateChart.timeout)
})
</script>

<style scoped>
.volume-chart {
  position: relative;
  height: 250px;
}

.chart-header {
  margin-bottom: 12px;
}

.stat-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.stat-info .label {
  color: #666;
}

.stat-info .value {
  color: #2E7D32;
  font-weight: bold;
  font-size: 14px;
}

.stat-info .count {
  color: #999;
}

.chart-wrapper {
  height: 200px;
  position: relative;
}

.empty-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>