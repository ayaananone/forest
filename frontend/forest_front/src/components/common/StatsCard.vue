<template>
  <div 
    class="stats-card" 
    :style="cardStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="icon-wrapper" :style="iconStyle">
      <el-icon :size="24">
        <component :is="getIcon(icon)" />
      </el-icon>
    </div>
    <div class="content">
      <div class="title">{{ title }}</div>
      <div class="value" :class="{ animating: isAnimating }" :style="valueStyle">
        {{ displayValue }}
      </div>
      <div class="subtitle">{{ subtitle }}</div>
    </div>
    <!-- 底部装饰条 -->
    <div class="bottom-bar" :style="barStyle"></div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { 
  DataAnalysis, 
  MapLocation, 
  TrendCharts,
  Histogram,
  OfficeBuilding,
  Money,
  PieChart,
  View
} from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [String, Number], default: '-' },
  icon: { type: String, default: 'DataAnalysis' },
  color: { type: String, default: '#2E7D32' },
  subtitle: { type: String, default: '' },
  animate: { type: Boolean, default: true }
})

const isAnimating = ref(false)

// 确保值是字符串
const displayValue = computed(() => {
  if (props.value === undefined || props.value === null) return '-'
  return String(props.value)
})

const iconMap = {
  DataAnalysis,
  MapLocation,
  TrendCharts,
  Histogram,
  OfficeBuilding,
  Money,
  PieChart,
  View
}

const getIcon = (name) => iconMap[name] || DataAnalysis

const cardStyle = computed(() => ({
  borderLeftColor: props.color,
  position: 'relative',
  overflow: 'hidden'
}))

const iconStyle = computed(() => ({
  backgroundColor: `${props.color}15`,
  color: props.color
}))

const valueStyle = computed(() => ({
  color: props.color
}))

const barStyle = computed(() => ({
  position: 'absolute',
  bottom: 0,
  left: 0,
  right: 0,
  height: '3px',
  backgroundColor: props.color,
  opacity: 0.3
}))

watch(() => props.value, () => {
  if (!props.animate) return
  
  isAnimating.value = true
  setTimeout(() => {
    isAnimating.value = false
  }, 300)
})

const handleMouseEnter = () => { 
  if (props.animate) isAnimating.value = true 
}

const handleMouseLeave = () => { 
  if (props.animate) isAnimating.value = false 
}
</script>

<style scoped>
.stats-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  min-width: 0;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3列 */
  gap: 16px;
  margin-bottom: 20px;
}

.title {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.value {
  font-size: 22px;
  font-weight: bold;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: transform 0.3s;
}

.value.animating {
  transform: scale(1.05);
}

.subtitle {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bottom-bar {
  pointer-events: none;
}
</style>