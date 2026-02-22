<template>
  <el-card 
    class="stats-card" 
    :style="cardStyle"
    shadow="hover"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="stats-content">
      <div class="stats-icon" :style="iconStyle">
        <el-icon :size="32">
          <component :is="getIcon(icon)" />
        </el-icon>
      </div>
      <div class="stats-info">
        <div class="stats-title">{{ title }}</div>
        <div 
          class="stats-value" 
          :class="{ 'number-animate': isAnimating }"
          :style="valueStyle"
        >
          {{ displayValue }}
        </div>
        <div v-if="subtitle" class="stats-subtitle">{{ subtitle }}</div>
      </div>
    </div>
    
    <div class="stats-bar" :style="barStyle"></div>
  </el-card>
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
  borderLeft: `4px solid ${props.color}`,
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

const handleMouseEnter = () => { isAnimating.value = true }
const handleMouseLeave = () => { isAnimating.value = false }
</script>

<style scoped>
.stats-card {
  margin-bottom: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
}
.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1) !important;
}
.stats-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}
.stats-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  transition: all 0.3s ease;
}
.stats-card:hover .stats-icon {
  transform: scale(1.1);
}
.stats-info {
  flex: 1;
}
.stats-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}
.stats-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
  transition: transform 0.3s ease;
}
.stats-subtitle {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
.number-animate {
  animation: numberPulse 0.3s ease;
}
@keyframes numberPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
@media (max-width: 768px) {
  .stats-content {
    flex-direction: column;
    text-align: center;
  }
  .stats-icon {
    margin-right: 0;
    margin-bottom: 12px;
  }
  .stats-value {
    font-size: 20px;
  }
}
</style>