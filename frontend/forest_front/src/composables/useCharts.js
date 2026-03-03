import { ref, computed, onUnmounted, shallowRef, markRaw, nextTick, toRaw } from 'vue'
import Chart from 'chart.js/auto'

// ==================== 常量配置 ====================
export const SPECIES_COLORS = {
  '马尾松': '#2E7D32',
  '杉木': '#388E3C', 
  '樟树': '#00796B',
  '枫香': '#D32F2F',
  '木荷': '#F57C00',
  '毛竹': '#7B1FA2',
  '油茶': '#FBC02D',
  '紫叶李': '#8E24AA',
  '未知': '#757575'
}

export const VOLUME_RANGES = [
  { label: '<50', min: 0, max: 50 },
  { label: '50-100', min: 50, max: 100 },
  { label: '100-150', min: 100, max: 150 },
  { label: '150-200', min: 150, max: 200 },
  { label: '>200', min: 200, max: Infinity }
]

// ==================== 通用工具函数 ====================

/**
 * 防抖函数
 */
export function useDebounce(fn, delay = 100) {
  let timeout = null
  const debounced = (...args) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => fn(...args), delay)
  }
  debounced.cancel = () => clearTimeout(timeout)
  return debounced
}

/**
 * 安全地获取数值
 */
export const safeNumber = (val, defaultValue = 0) => {
  if (val === null || val === undefined) return defaultValue
  const num = Number(val)
  return isNaN(num) ? defaultValue : num
}

/**
 * 深拷贝数据（用于 Chart.js）
 */
export const deepClone = (data) => JSON.parse(JSON.stringify(toRaw(data) || []))

// ==================== 统计计算逻辑 ====================

export function calculateBaseStats(stands) {
  const totalStands = stands?.length || 0
  const totalArea = stands?.reduce((sum, s) => sum + safeNumber(s?.areaHa), 0) || 0
  const totalVolume = stands?.reduce((sum, s) => {
    const volumePerHa = safeNumber(s?.volumePerHa)
    const area = safeNumber(s?.areaHa)
    return sum + (volumePerHa * area)
  }, 0) || 0
  const avgVolume = totalArea > 0 ? totalVolume / totalArea : 0
  
  return { totalStands, totalArea, totalVolume, avgVolume }
}

export function generateStatsCards(stats, options = {}) {
  const { 
    areaDecimals = 2, volumeDecimals = 2, avgDecimals = 2,
    volumeThreshold = 10000,
    icons = { stands: 'DataAnalysis', area: 'MapLocation', volume: 'TrendCharts', avg: 'Histogram' },
    colors = { stands: '#2E7D32', area: '#388E3C', volume: '#D32F2F', avg: '#00796B' }
  } = options

  return [
    { id: 'stands', title: '小班总数', value: stats.totalStands.toString(), icon: icons.stands, color: colors.stands, subtitle: '林分小班' },
    { id: 'area', title: '总面积', value: stats.totalArea.toFixed(areaDecimals) + ' ha', icon: icons.area, color: colors.area, subtitle: '覆盖区域' },
    { id: 'volume', title: '总蓄积', value: stats.totalVolume > volumeThreshold ? (stats.totalVolume / volumeThreshold).toFixed(volumeDecimals) + ' 万m³' : stats.totalVolume.toFixed(volumeDecimals) + ' m³', icon: icons.volume, color: colors.volume, subtitle: '估算总量' },
    { id: 'avg', title: '平均蓄积', value: stats.avgVolume.toFixed(avgDecimals) + ' m³/ha', icon: icons.avg, color: colors.avg, subtitle: '单位面积' }
  ]
}

export function calculateGrowthData(avgVolume, years = 5) {
  if (avgVolume <= 0) return Array(years).fill(0)
  const factors = [0.5, 0.65, 0.8, 0.9, 1.0]
  return factors.slice(0, years).map(f => Math.round(avgVolume * f))
}

export function useForestStats(standsRef) {
  const safeStats = computed(() => calculateBaseStats(standsRef.value))
  const safeStatsCards = computed(() => generateStatsCards(safeStats.value))
  const growthData = computed(() => calculateGrowthData(safeStats.value.avgVolume))
  return { safeStats, safeStatsCards, growthData }
}

// ==================== 蓄积量区间计算 ====================

export function calculateVolumeRanges(stands, ranges = VOLUME_RANGES) {
  const counts = ranges.map(() => 0)
  
  if (!Array.isArray(stands) || stands.length === 0) {
    return ranges.map(r => ({ ...r, count: 0, percentage: 0, barPercentage: 0 }))
  }
  
  const validStands = stands.filter(stand => {
    const volume = safeNumber(stand?.volumePerHa, -1)
    return volume > 0
  })
  
  if (validStands.length === 0) {
    return ranges.map(r => ({ ...r, count: 0, percentage: 0, barPercentage: 0 }))
  }
  
  validStands.forEach(stand => {
    const volume = safeNumber(stand.volumePerHa)
    for (let i = 0; i < ranges.length; i++) {
      if (volume >= ranges[i].min && volume < ranges[i].max) {
        counts[i]++
        break
      }
    }
  })

  const maxCount = Math.max(...counts, 1)
  const total = counts.reduce((a, b) => a + b, 0)

  return ranges.map((r, i) => ({
    ...r,
    count: counts[i],
    percentage: total > 0 ? Math.round((counts[i] / total) * 100) : 0,
    barPercentage: maxCount > 0 ? (counts[i] / maxCount) * 100 : 0
  }))
}

export function getMaxRange(ranges) {
  if (!ranges || ranges.length === 0) return { label: '-', count: 0 }
  const maxCount = Math.max(...ranges.map(r => r.count))
  if (maxCount === 0) return { label: '-', count: 0 }
  const maxRanges = ranges.filter(r => r.count === maxCount)
  return { label: maxRanges.map(r => r.label).join('/'), count: maxCount }
}

// ==================== 树种数据处理 ====================

export function processSpeciesStats(speciesStats, colors = SPECIES_COLORS) {
  if (!Array.isArray(speciesStats) || speciesStats.length === 0) {
    return []
  }
  
  const total = speciesStats.reduce((sum, s) => sum + safeNumber(s?.count || s?.standCount), 0)
  const colorValues = Object.values(colors)

  const result = speciesStats.map((s, index) => {
    const count = safeNumber(s?.count || s?.standCount)
    const speciesName = s?.species || '未知'
    const color = colors[speciesName] || colorValues[index % colorValues.length]
    const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : '0.0'
    
    return { 
      species: speciesName, 
      count, 
      color, 
      percentage,
      totalArea: safeNumber(s?.totalArea),
      totalVolume: safeNumber(s?.totalVolume),
      avgVolumePerHa: safeNumber(s?.avgVolumePerHa)
    }
  }).sort((a, b) => b.count - a.count)
  
  return result
}

// ==================== 生长趋势计算 ====================

export function calculateCAGR(data) {
  const validData = data.map(v => safeNumber(v)).filter(v => v > 0)
  const n = validData.length
  if (n < 2) return 0
  const first = validData[0]
  const last = validData[n - 1]
  if (first <= 0) return 0
  return parseFloat(((Math.pow(last / first, 1 / (n - 1)) - 1) * 100).toFixed(1))
}

export function predictNextVolume(data, growthRate = null) {
  const validData = data.map(v => safeNumber(v)).filter(v => v > 0)
  const n = validData.length
  if (n === 0) return 0
  const last = validData[n - 1]
  if (last <= 0) return 0
  if (n < 2) return Math.round(last)
  const rate = growthRate !== null ? growthRate : calculateCAGR(data)
  return Math.max(0, Math.round(last * (1 + rate / 100)))
}

export function fillTrendData(data, years = 5) {
  const validData = deepClone(data).map(v => Math.max(0, safeNumber(v)))
  while (validData.length > 0 && validData[validData.length - 1] === 0) validData.pop()
  
  const n = validData.length
  if (n === 0) return Array(years).fill(0)
  
  let result = [...validData]
  if (n >= 2 && result.length < years) {
    const cagr = Math.pow(result[n - 1] / result[0], 1 / (n - 1)) - 1
    while (result.length < years) result.push(Math.round(result[result.length - 1] * (1 + cagr)))
  } else if (n === 1 && result.length < years) {
    while (result.length < years) result.push(result[0])
  }
  return result.slice(0, years)
}

export function useTrendStats(dataRef) {
  const growthRate = computed(() => calculateCAGR(dataRef.value))
  const predictedVolume = computed(() => predictNextVolume(dataRef.value))
  const trendMetrics = computed(() => ({ growthRate: growthRate.value, predictedVolume: predictedVolume.value, isPositive: growthRate.value >= 0 }))
  return { growthRate, predictedVolume, trendMetrics }
}

// ==================== 图表 Hooks（修复版）====================

export function useSpeciesChart() {
  const chartInstance = shallowRef(null)
  const legendData = ref([])
  const pendingData = ref(null) // 用于存储等待更新的数据
  let updateTimeout = null

  const initChart = (canvas, options = {}) => {
    if (!canvas) {
      console.warn('initChart: canvas 为空')
      return false
    }
    
    // 如果已有实例，先销毁
    if (chartInstance.value) { 
      chartInstance.value.destroy()
      chartInstance.value = null 
    }
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      console.warn('initChart: 无法获取 2d context')
      return false
    }

    try {
      const chart = new Chart(ctx, {
        type: 'doughnut',
        data: { 
          labels: [], 
          datasets: [{ 
            data: [], 
            backgroundColor: [], 
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
              backgroundColor: 'rgba(255,255,255,0.9)', 
              titleColor: '#333', 
              bodyColor: '#666', 
              borderColor: '#e0e0e0', 
              borderWidth: 1, 
              padding: 12, 
              callbacks: { 
                label: (context) => { 
                  const label = context.label || '未知'
                  const value = safeNumber(context.parsed)
                  const total = context.dataset.data.reduce((a,b) => a+b,0)
                  const pct = total > 0 ? ((value/total)*100).toFixed(1) : 0
                  return `${label}: ${value}个小班 (${pct}%)` 
                } 
              } 
            } 
          },
          cutout: options.cutout || '55%',
          radius: options.radius || '90%',
          animation: { animateRotate: true, duration: 800 }
        }
      })
      
      chartInstance.value = markRaw(chart)
      
      // 如果有待更新的数据，立即更新
      if (pendingData.value) {
        console.log('initChart: 应用待更新数据')
        updateChart(pendingData.value)
        pendingData.value = null
      }
      
      return true
    } catch (e) { 
      console.error('Chart init failed:', e)
      return false
    }
  }

  const updateChart = async (speciesStats) => {
    // 如果图表未初始化，保存数据等待初始化
    if (!chartInstance.value) {
      console.log('updateChart: 图表未初始化，保存数据等待初始化')
      pendingData.value = speciesStats
      return false
    }
    
    await nextTick()
    
    const processed = processSpeciesStats(speciesStats)
    legendData.value = processed
    
    if (processed.length === 0) {
      console.warn('updateChart: 处理后数据为空')
      return false
    }
    
    chartInstance.value.data.labels = processed.map(s => s.species)
    chartInstance.value.data.datasets[0].data = processed.map(s => s.count)
    chartInstance.value.data.datasets[0].backgroundColor = processed.map(s => s.color)
    chartInstance.value.update('none')
    
    console.log('树种图表已更新:', processed.length, '个树种')
    return true
  }

  const debouncedUpdate = (data) => { 
    clearTimeout(updateTimeout)
    updateTimeout = setTimeout(() => updateChart(data), 100) 
  }

  onUnmounted(() => { 
    clearTimeout(updateTimeout)
    if (chartInstance.value) { 
      chartInstance.value.destroy()
      chartInstance.value = null 
    } 
  })

  return { chartInstance, legendData, initChart, updateChart, debouncedUpdate }
}

export function useVolumeChart() {
  const chartInstance = shallowRef(null)
  const stats = ref([])
  const pendingData = ref(null) // 用于存储等待更新的数据
  let updateTimeout = null

  const initChart = (canvas, options = {}) => {
    if (!canvas) {
      console.warn('initChart: canvas 为空')
      return false
    }
    
    if (chartInstance.value) { 
      chartInstance.value.destroy()
      chartInstance.value = null 
    }
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      console.warn('initChart: 无法获取 2d context')
      return false
    }
    
    const gradient = ctx.createLinearGradient(0, 0, 0, 200)
    gradient.addColorStop(0, 'rgba(46,125,50,0.8)')
    gradient.addColorStop(1, 'rgba(46,125,50,0.3)')

    try {
      const chart = new Chart(ctx, {
        type: 'bar',
        data: { 
          labels: VOLUME_RANGES.map(r => r.label), 
          datasets: [{ 
            label: '小班数量', 
            data: [0,0,0,0,0], 
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
            legend: { display: false }, 
            tooltip: { 
              backgroundColor: 'rgba(255,255,255,0.9)', 
              titleColor: '#333', 
              bodyColor: '#666', 
              borderColor: '#e0e0e0', 
              borderWidth: 1, 
              padding: 12, 
              callbacks: { 
                title: (items) => `蓄积量: ${items[0].label} m³/ha`, 
                label: (context) => { 
                  const value = safeNumber(context.parsed.y)
                  const total = context.dataset.data.reduce((a,b)=>a+b,0)
                  const pct = total > 0 ? ((value/total)*100).toFixed(1) : 0
                  return `小班数量: ${value} (${pct}%)` 
                } 
              } 
            } 
          }, 
          scales: { 
            x: { 
              title: { display: true, text: '蓄积量区间 (m³/ha)', color: '#909399', font: { size: 11 } }, 
              grid: { display: false } 
            }, 
            y: { 
              beginAtZero: true, 
              title: { display: true, text: '小班数量', color: '#909399', font: { size: 11 } } 
            } 
          }, 
          animation: { duration: 800, easing: 'easeOutQuart' }
        }
      })
      
      chartInstance.value = markRaw(chart)
      
      // 如果有待更新的数据，立即更新
      if (pendingData.value) {
        console.log('initChart: 应用待更新数据')
        updateChart(pendingData.value)
        pendingData.value = null
      }
      
      return true
    } catch (e) { 
      console.error('Chart init failed:', e)
      return false
    }
  }

  const updateChart = (stands) => {
    // 如果图表未初始化，保存数据等待初始化
    if (!chartInstance.value) {
      console.log('updateChart: 图表未初始化，保存数据等待初始化')
      pendingData.value = stands
      return false
    }
    
    stats.value = calculateVolumeRanges(stands)
    const data = stats.value.map(r => r.count)
    
    chartInstance.value.data.datasets[0].data = data
    chartInstance.value.update()
    
    console.log('蓄积量图表已更新:', stats.value.map(r => `${r.label}:${r.count}`).join(', '))
    return true
  }
  
  const debouncedUpdate = (data) => { 
    clearTimeout(updateTimeout)
    updateTimeout = setTimeout(() => updateChart(data), 300) 
  }
  
  const getMaxRange = () => { 
    return stats.value.length > 0 ? { 
      label: stats.value.reduce((max, r) => r.count > max.count ? r : max, stats.value[0]).label, 
      count: Math.max(...stats.value.map(r => r.count)) 
    } : { label: '-', count: 0 } 
  }

  onUnmounted(() => { 
    clearTimeout(updateTimeout)
    if (chartInstance.value) { 
      chartInstance.value.destroy()
      chartInstance.value = null 
    } 
  })

  return { chartInstance, stats, initChart, updateChart, debouncedUpdate, getMaxRange }
}

export function useTrendChart() {
  const chartInstance = shallowRef(null)
  const pendingData = ref(null)
  const pendingTimeRange = ref('5年')
  let updateTimeout = null

  const initChart = (canvas, options = {}) => {
    if (!canvas) {
      console.warn('initChart: canvas 为空')
      return false
    }
    
    if (chartInstance.value) { 
      chartInstance.value.stop()
      chartInstance.value.destroy()
      chartInstance.value = null 
    }
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      console.warn('initChart: 无法获取 2d context')
      return false
    }
    
    const gradient = ctx.createLinearGradient(0, 0, 0, 200)
    gradient.addColorStop(0, 'rgba(56,142,60,0.3)')
    gradient.addColorStop(1, 'rgba(56,142,60,0.05)')
    const years = options.years || 5
    const labels = Array.from({ length: years }, (_, i) => `${i + 1}年`)

    try {
      const chart = new Chart(ctx, {
        type: 'line',
        data: { 
          labels, 
          datasets: [{ 
            label: '平均蓄积增长', 
            data: Array(years).fill(0), 
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
            legend: { display: false }, 
            tooltip: { 
              backgroundColor: 'rgba(255,255,255,0.9)', 
              titleColor: '#333', 
              bodyColor: '#666', 
              borderColor: '#e0e0e0', 
              borderWidth: 1, 
              padding: 12, 
              displayColors: false, 
              callbacks: { 
                title: (items) => `第${items[0].label}`, 
                label: (context) => `平均蓄积: ${safeNumber(context.parsed.y)} m³/ha` 
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
      
      // 如果有待更新的数据，立即更新
      if (pendingData.value) {
        console.log('initChart: 应用待更新数据')
        updateChart(pendingData.value, pendingTimeRange.value)
        pendingData.value = null
        pendingTimeRange.value = '5年'
      }
      
      return true
    } catch (e) { 
      console.error('Chart init failed:', e)
      return false
    }
  }

  const updateChart = async (data, timeRange = '5年') => {
    // 如果图表未初始化，保存数据等待初始化
    if (!chartInstance.value) {
      console.log('updateChart: 图表未初始化，保存数据等待初始化')
      pendingData.value = data
      pendingTimeRange.value = timeRange
      return false
    }
    
    await nextTick()
    const years = timeRange === '5年' ? 5 : 10
    const filledData = fillTrendData(data, years)
    const labels = Array.from({ length: years }, (_, i) => `${i + 1}年`)
    chartInstance.value.data.labels = labels
    chartInstance.value.data.datasets[0].data = filledData
    chartInstance.value.update('none')
    return true
  }

  const debouncedUpdate = (data, timeRange) => { 
    clearTimeout(updateTimeout)
    updateTimeout = setTimeout(() => updateChart(data, timeRange), 100) 
  }

  onUnmounted(() => { 
    clearTimeout(updateTimeout)
    if (chartInstance.value) { 
      chartInstance.value.stop()
      chartInstance.value.destroy()
      chartInstance.value = null 
    } 
  })

  return { chartInstance, initChart, updateChart, debouncedUpdate }
}

export function useAllCharts() {
  const species = useSpeciesChart()
  const volume = useVolumeChart()
  const trend = useTrendChart()

  const initAll = (speciesCanvas, volumeCanvas, trendCanvas, options = {}) => {
    console.log('initAll 被调用')
    const r1 = species.initChart(speciesCanvas, options.species)
    const r2 = volume.initChart(volumeCanvas, options.volume)
    const r3 = trend.initChart(trendCanvas, options.trend)
    console.log('图表初始化结果:', { species: r1, volume: r2, trend: r3 })
    return { species: r1, volume: r2, trend: r3 }
  }

  const updateAll = (stats, stands, growthData, timeRange = '5年') => {
    console.log('updateAll 被调用:', {
      statsCount: stats?.length,
      standsCount: stands?.length,
      growthDataLength: growthData?.length
    })
    species.updateChart(stats)
    volume.updateChart(stands)
    trend.updateChart(growthData, timeRange)
  }

  const destroyAll = () => {
    species.chartInstance.value?.destroy()
    volume.chartInstance.value?.destroy()
    trend.chartInstance.value?.destroy()
  }

  return { species, volume, trend, initAll, updateAll, destroyAll }
}

// ==================== 数据加载逻辑 ====================

export function useForestDataLoader(options = {}) {
  const stands = ref([])
  const speciesStats = ref([])
  const loading = ref(false)
  const error = ref(null)

  const { fetchStands, fetchSpeciesStatistics, onSuccess, onError, showMessage } = options

  const loadData = async () => {
    if (!fetchStands) { 
      console.warn('fetchStands not provided')
      return 
    }
    
    loading.value = true
    error.value = null

    try {
      console.log('开始加载数据...')
      const promises = [fetchStands()]
      if (fetchSpeciesStatistics) promises.push(fetchSpeciesStatistics())
      
      const results = await Promise.allSettled(promises)

      stands.value = results[0].status === 'fulfilled' ? (results[0].value || []) : []
      if (results[1]) {
        speciesStats.value = results[1].status === 'fulfilled' ? (results[1].value || []) : []
      }

      console.log('数据加载完成:', { 
        stands: stands.value.length, 
        speciesStats: speciesStats.value.length 
      })
      
      if (onSuccess) onSuccess({ stands: stands.value, speciesStats: speciesStats.value })
      if (showMessage) showMessage.success('统计图表加载完成')
    } catch (err) {
      console.error('加载失败:', err)
      error.value = err
      stands.value = []
      speciesStats.value = []
      if (onError) onError(err)
      if (showMessage) showMessage.error('部分数据加载失败，请刷新重试')
    } finally {
      loading.value = false
    }
  }

  const refresh = () => loadData()

  return { stands, speciesStats, loading, error, loadData, refresh }
}

// 默认导出
export default {
  SPECIES_COLORS, VOLUME_RANGES,
  useDebounce, safeNumber, deepClone,
  calculateBaseStats, generateStatsCards, calculateGrowthData, useForestStats,
  calculateVolumeRanges, getMaxRange,
  processSpeciesStats,
  calculateCAGR, predictNextVolume, fillTrendData, useTrendStats,
  useSpeciesChart, useVolumeChart, useTrendChart, useAllCharts,
  useForestDataLoader
}