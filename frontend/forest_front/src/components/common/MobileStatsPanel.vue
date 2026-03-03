<!-- MobileStatsPanel.vue - 替代桌面端的 ChartContainer -->
<template>
  <div class="mobile-stats-panel">
    <!-- 顶部统计卡片区 - 横向滚动 -->
    <div class="stats-scroll-container">
      <div class="stats-cards">
        <div 
          v-for="card in safeStatsCards" 
          :key="card.id"
          class="stat-card-mini"
          :style="{ borderLeftColor: card.color }"
        >
          <div class="stat-icon-mini" :style="{ backgroundColor: card.color + '20', color: card.color }">
            <el-icon :size="20">
              <component :is="getIcon(card.icon)" />
            </el-icon>
          </div>
          <div class="stat-info-mini">
            <div class="stat-value-mini" :style="{ color: card.color }">{{ card.value }}</div>
            <div class="stat-title-mini">{{ card.title }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区 - 标签页切换，每次只显示一个 -->
    <div class="charts-tabs">
      <div class="tab-header">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          class="tab-btn"
          :class="{ active: currentTab === tab.key }"
          @click="currentTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="tab-content">
        <!-- 树种分布 - 使用列表而非饼图，或简化饼图 -->
        <div v-show="currentTab === 'species'" class="chart-tab">
          <div class="species-list">
            <div 
              v-for="(item, index) in speciesStats" 
              :key="index"
              class="species-item"
            >
              <div class="species-bar-wrapper">
                <div class="species-label">
                  <span class="species-color" :style="{ backgroundColor: item.color }"></span>
                  <span class="species-name">{{ item.species }}</span>
                  <span class="species-count">{{ item.count }}个</span>
                </div>
                <div class="species-bar-bg">
                  <div 
                    class="species-bar-fill" 
                    :style="{ 
                      width: item.percentage + '%',
                      backgroundColor: item.color 
                    }"
                  ></div>
                </div>
                <div class="species-percent">{{ item.percentage }}%</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 蓄积分布 - 简化柱状图为列表 -->
        <div v-show="currentTab === 'volume'" class="chart-tab">
          <div class="volume-list">
            <div 
              v-for="(range, index) in volumeRanges" 
              :key="index"
              class="volume-item"
            >
              <div class="volume-range">{{ range.label }} m³/ha</div>
              <div class="volume-bar-wrapper">
                <div class="volume-bar-bg">
                  <div 
                    class="volume-bar-fill"
                    :style="{ width: range.percentage + '%' }"
                  ></div>
                </div>
                <span class="volume-count">{{ range.count }}个</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 生长趋势 - 保持折线图但优化显示 -->
        <div v-show="currentTab === 'trend'" class="chart-tab">
          <div class="trend-summary">
            <div class="trend-metric">
              <span class="metric-label">平均增长率</span>
              <span class="metric-value" :class="growthRate >= 0 ? 'up' : 'down'">
                {{ growthRate >= 0 ? '+' : '' }}{{ growthRate }}%
              </span>
            </div>
            <div class="trend-metric">
              <span class="metric-label">预测蓄积</span>
              <span class="metric-value">{{ predictedVolume }} m³</span>
            </div>
          </div>
          <div class="trend-chart-wrapper">
            <canvas ref="trendCanvas"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { 
  DataAnalysis, 
  MapLocation, 
  TrendCharts,
  Histogram 
} from '@element-plus/icons-vue'
import Chart from 'chart.js/auto'

const props = defineProps({
  stands: Array,
  speciesStats: Array
})

const currentTab = ref('species')
const trendCanvas = ref(null)
let trendChart = null

const tabs = [
  { key: 'species', label: '树种分布' },
  { key: 'volume', label: '蓄积分布' },
  { key: 'trend', label: '生长趋势' }
]

const iconMap = { DataAnalysis, MapLocation, TrendCharts, Histogram }
const getIcon = (name) => iconMap[name] || DataAnalysis

// 计算统计数据（简化版）
const safeStats = computed(() => {
  const totalStands = props.stands?.length || 0
  const totalArea = props.stands?.reduce((sum, s) => sum + (Number(s?.area) || 0), 0) || 0
  const totalVolume = props.stands?.reduce((sum, s) => sum + ((Number(s?.volumePerHa) || 0) * (Number(s?.area) || 0)), 0) || 0
  const avgVolume = totalArea > 0 ? totalVolume / totalArea : 0
  return { totalStands, totalArea, totalVolume, avgVolume }
})

const safeStatsCards = computed(() => [
  {
    id: 'stands',
    title: '小班总数',
    value: safeStats.value.totalStands.toString(),
    icon: 'DataAnalysis',
    color: '#2E7D32'
  },
  {
    id: 'area',
    title: '总面积',
    value: safeStats.value.totalArea.toFixed(1) + ' ha',
    icon: 'MapLocation',
    color: '#388E3C'
  },
  {
    id: 'volume',
    title: '总蓄积',
    value: safeStats.value.totalVolume > 10000 
      ? (safeStats.value.totalVolume / 10000).toFixed(1) + ' 万m³'
      : safeStats.value.totalVolume.toFixed(0) + ' m³',
    icon: 'TrendCharts',
    color: '#D32F2F'
  },
  {
    id: 'avg',
    title: '平均蓄积',
    value: safeStats.value.avgVolume.toFixed(1) + ' m³/ha',
    icon: 'Histogram',
    color: '#00796B'
  }
])

// 处理后的树种数据（带颜色）
const SPECIES_COLORS = {
  '马尾松': '#2E7D32',
  '杉木': '#388E3C', 
  '樟树': '#00796B',
  '枫香': '#D32F2F',
  '木荷': '#F57C00',
  '毛竹': '#7B1FA2',
  '油茶': '#FBC02D',
  '未知': '#757575'
}

const processedSpeciesStats = computed(() => {
  const total = props.speciesStats?.reduce((sum, s) => sum + (Number(s.count) || 0), 0) || 0
  return (props.speciesStats || []).map((s, index) => {
    const count = Number(s.count) || 0
    const color = SPECIES_COLORS[s.species] || Object.values(SPECIES_COLORS)[index % 8]
    return {
      species: s.species || '未知',
      count,
      color,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0
    }
  }).sort((a, b) => b.count - a.count)
})

// 蓄积区间数据
const volumeRanges = computed(() => {
  const ranges = [
    { label: '<50', min: 0, max: 50, count: 0 },
    { label: '50-100', min: 50, max: 100, count: 0 },
    { label: '100-150', min: 100, max: 150, count: 0 },
    { label: '150-200', min: 150, max: 200, count: 0 },
    { label: '>200', min: 200, max: Infinity, count: 0 }
  ]

  props.stands?.forEach(stand => {
    const volume = Number(stand?.volumePerHa) || 0
    const range = ranges.find(r => volume >= r.min && volume < r.max)
    if (range) range.count++
  })

  const maxCount = Math.max(...ranges.map(r => r.count), 1)
  return ranges.map(r => ({
    ...r,
    percentage: (r.count / maxCount) * 100
  }))
})

const growthRate = computed(() => {
  // 简化计算
  return 5.2
})

const predictedVolume = computed(() => {
  return Math.round(safeStats.value.avgVolume * 1.05)
})

// 初始化趋势图（简化版）
const initTrendChart = () => {
  if (!trendCanvas.value) return

  if (trendChart) {
    trendChart.destroy()
  }

  const ctx = trendCanvas.value.getContext('2d')
  trendChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: ['1年', '2年', '3年', '4年', '5年'],
      datasets: [{
        data: [45, 58, 72, 88, 102],
        borderColor: '#2E7D32',
        backgroundColor: 'rgba(46, 125, 50, 0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { display: false } },
        y: { beginAtZero: true }
      }
    }
  })
}

watch(currentTab, (val) => {
  if (val === 'trend') {
    setTimeout(initTrendChart, 100)
  }
})

onMounted(() => {
  if (currentTab.value === 'trend') {
    initTrendChart()
  }
})
</script>

<style scoped>
.mobile-stats-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 统计卡片横向滚动 */
.stats-scroll-container {
  padding: 12px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.stats-cards {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 4px 0;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.stats-cards::-webkit-scrollbar {
  display: none;
}

.stat-card {
  flex: 0 0 auto;
  width: 140px;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  border-left: 3px solid;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

/* 图表区域 */
.charts-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 12px;
}

.tab-btn {
  flex: 1;
  padding: 14px 8px;
  border: none;
  background: none;
  font-size: 14px;
  color: #666;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.tab-btn.active {
  color: #2E7D32;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 3px;
  background: #2E7D32;
  border-radius: 2px;
}

.tab-panels {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.panel {
  min-height: 100%;
}

/* 进度条样式 */
.bar-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.bar-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.count {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.range-label {
  font-size: 13px;
  color: #666;
}

.bar-track {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.bar-fill.gradient {
  background: linear-gradient(90deg, #2E7D32, #4CAF50);
}

.bar-percent {
  position: absolute;
  right: 0;
  top: -18px;
  font-size: 11px;
  color: #999;
}

/* 趋势指标 */
.trend-metrics {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.metric-box {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.metric-value.up {
  color: #4CAF50;
}

.metric-value.down {
  color: #f44336;
}

.chart-wrapper {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  height: 220px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.empty-state {
  padding: 40px 0;
}

canvas {
  width: 100% !important;
  height: 100% !important;
}
</style>