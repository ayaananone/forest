<template>
  <div class="radius-query">
    <el-card 
      shadow="hover" 
      :class="{ 'active-card': active }"
      :body-style="{ padding: '12px' }"
    >
      <div class="radius-header">
        <el-button
          :type="active ? 'danger' : 'primary'"
          size="small"
          @click="toggleActive"
        >
          <el-icon v-if="!active"><Search /></el-icon>
          <el-icon v-else><Close /></el-icon>
          {{ active ? '退出查询' : '半径查询' }}
        </el-button>
        
        <el-tooltip content="点击地图查询指定半径范围内的林分" placement="top">
          <el-icon class="help-icon"><Question-Filled /></el-icon>
        </el-tooltip>
      </div>
      
      <el-collapse-transition>
        <div v-show="active" class="radius-controls">
          <el-divider />
          
          <div class="control-item">
            <label>查询半径</label>
            <div class="radius-slider">
              <el-slider
                v-model="localRadius"
                :min="100"
                :max="5000"
                :step="100"
                show-stops
                :marks="{ 1000: '1km', 3000: '3km', 5000: '5km' }"
              />
              <el-input-number
                v-model="localRadius"
                :min="100"
                :max="5000"
                :step="100"
                size="small"
                controls-position="right"
              >
                <template #append>m</template>
              </el-input-number>
            </div>
          </div>
          
          <div class="control-item">
            <label>显示范围</label>
            <el-switch
              v-model="showCircle"
              active-text="显示"
              inactive-text="隐藏"
              @change="handleShowCircleChange"
            />
          </div>
          
          <div class="radius-tips">
            <el-alert
              title="点击地图任意位置开始查询"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
          
          <div class="radius-actions">
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="handleQuickQuery(500)"
            >
              500m
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="handleQuickQuery(1000)"
            >
              1km
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="handleQuickQuery(2000)"
            >
              2km
            </el-button>
          </div>
        </div>
      </el-collapse-transition>
    </el-card>
    
    <!-- 查询结果摘要（悬浮显示） -->
    <el-collapse-transition>
      <div v-if="lastResult" class="result-summary">
        <el-card shadow="hover" :body-style="{ padding: '10px' }">
          <div class="summary-header">
            <span>上次查询结果</span>
            <el-button type="danger" link size="small" @click="clearResult">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="summary-content">
            <div class="summary-item">
              <span class="label">半径</span>
              <span class="value">{{ lastResult.radius }}m</span>
            </div>
            <div class="summary-item">
              <span class="label">找到</span>
              <span class="value highlight">{{ lastResult.count }} 个</span>
            </div>
            <div class="summary-item">
              <span class="label">总蓄积</span>
              <span class="value">{{ formatVolume(lastResult.totalVolume) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">总面积</span>
              <span class="value">{{ lastResult.totalArea.toFixed(2) }} ha</span>
            </div>
          </div>
          
          <!-- 结果列表 -->
          <div v-if="lastResult.stands && lastResult.stands.length > 0" class="result-list">
            <el-divider />
            <div 
              v-for="stand in lastResult.stands.slice(0, 5)" 
              :key="stand.id"
              class="result-stand-item"
              @click="selectStand(stand)"
            >
              <div class="stand-info">
                <span class="stand-name">{{ stand.standName || stand.xiaoBanCode || '未命名' ||stand.id }}</span>
                <span class="stand-species">{{ stand.dominantSpecies || '未知' }}</span>
              </div>
              <div class="stand-data">
                <span class="stand-distance">{{ stand.distance }}m</span>
                <span class="stand-volume">{{ stand.volumePerHa || 0 }} m³/ha</span>
              </div>
            </div>
            <div v-if="lastResult.stands.length > 5" class="more-stands">
              还有 {{ lastResult.stands.length - 5 }} 个林分...
            </div>
          </div>
          
          <!-- 导出按钮 -->
          <div class="export-action">
            <el-button type="success" size="small" plain @click="exportResult">
              <el-icon><Download /></el-icon> 导出结果
            </el-button>
          </div>
        </el-card>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Close, QuestionFilled, Delete, Download } from '@element-plus/icons-vue'
import { formatVolume } from '@/utils/formatters'
import { exportRadiusQueryResult } from '@/utils/export'

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  radius: {
    type: Number,
    default: 1000
  }
})

const emit = defineEmits([
  'update:active', 
  'update:radius', 
  'query', 
  'show-circle-change',
  'select-stand'
])

const localRadius = ref(props.radius)
const showCircle = ref(true)
const lastResult = ref(null)

// ==================== 计算属性 ====================

const isActive = computed({
  get: () => props.active,
  set: (val) => emit('update:active', val)
})

// ==================== 监听 ====================

watch(() => props.radius, (val) => {
  localRadius.value = val
})

watch(localRadius, (val) => {
  emit('update:radius', val)
})

// ==================== 方法 ====================

const toggleActive = () => {
  isActive.value = !isActive.value
  if (!isActive.value) {
    lastResult.value = null
  }
}

const handleShowCircleChange = (val) => {
  emit('show-circle-change', val)
}

const handleQuickQuery = (radius) => {
  localRadius.value = radius
  emit('update:radius', radius)
}

const selectStand = (stand) => {
  emit('select-stand', stand)
}

const clearResult = () => {
  lastResult.value = null
}

const exportResult = () => {
  if (lastResult.value && lastResult.value.stands) {
    exportRadiusQueryResult(lastResult.value.stands)
  }
}

// ==================== 外部调用的方法 ====================

const setResult = (result) => {
  // 计算每个林分的距离
  const standsWithDistance = result.stands?.map(stand => ({
    ...stand,
    distance: Math.round(calculateDistance(
      result.lon, 
      result.lat, 
      stand.centerLon, 
      stand.centerLat
    ))
  })) || []

  // 按距离排序
  standsWithDistance.sort((a, b) => a.distance - b.distance)

  lastResult.value = {
    radius: result.radius,
    count: result.stands?.length || 0,
    totalVolume: result.totalVolume || 0,
    totalArea: result.stands?.reduce((sum, s) => sum + (s.area || 0), 0) || 0,
    stands: standsWithDistance,
    centerLon: result.lon,
    centerLat: result.lat,
    timestamp: Date.now()
  }
}

// 计算两点间距离（米）
const calculateDistance = (lon1, lat1, lon2, lat2) => {
  const R = 6371000 // 地球半径（米）
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
  return R * c
}

// ==================== 导出方法 ====================

defineExpose({
  setResult,
  clearResult
})
</script>

<style scoped>
.radius-query {
  position: absolute;
  top: 20px;
  right: 300px;
  width: 280px;
  z-index: 100;
}

.active-card {
  border: 2px solid #2E7D32;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3) !important;
}

.radius-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.help-icon {
  color: #909399;
  cursor: help;
  font-size: 16px;
}

.help-icon:hover {
  color: #2E7D32;
}

.radius-controls {
  margin-top: 8px;
}

.control-item {
  margin-bottom: 16px;
}

.control-item label {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.radius-slider {
  display: flex;
  align-items: center;
  gap: 12px;
}

.radius-slider :deep(.el-slider) {
  flex: 1;
}

.radius-slider :deep(.el-input-number) {
  width: 100px;
}

:deep(.el-slider__marks-text) {
  font-size: 10px;
  color: #909399;
}

.radius-tips {
  margin-bottom: 12px;
}

.radius-actions {
  display: flex;
  justify-content: space-between;
}

.radius-actions .el-button {
  flex: 1;
}

/* 结果摘要 */
.result-summary {
  margin-top: 12px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.summary-item .label {
  color: #606266;
}

.summary-item .value {
  color: #303133;
  font-weight: 500;
}

.summary-item .value.highlight {
  color: #F56C6C;
  font-size: 14px;
}

/* 结果列表 */
.result-list {
  margin-top: 8px;
}

.result-stand-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.result-stand-item:hover {
  background: #e8f5e9;
  transform: translateX(4px);
}

.stand-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stand-name {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.stand-species {
  color: #909399;
  font-size: 11px;
}

.stand-data {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.stand-distance {
  color: #2E7D32;
  font-size: 12px;
  font-weight: 500;
}

.stand-volume {
  color: #606266;
  font-size: 11px;
}

.more-stands {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 8px;
}

.export-action {
  margin-top: 12px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .radius-query {
    right: 10px;
    top: auto;
    bottom: 20px;
    width: calc(100% - 20px);
    max-width: 300px;
  }
  
  .radius-slider {
    flex-direction: column;
    align-items: stretch;
  }
  
  .radius-slider :deep(.el-input-number) {
    width: 100%;
  }
}
</style>