<template>
  <div class="layer-control">
    <el-card shadow="hover" :body-style="{ padding: '12px' }">
      <template #header>
        <div class="layer-header">
          <span>🗂️ 图层控制</span>
          <el-button 
            type="primary" 
            link 
            size="small"
            @click="toggleExpandAll"
          >
            {{ expandAll ? '收起' : '展开' }}
          </el-button>
        </div>
      </template>
      
      <el-collapse v-model="activeNames" :accordion="false">
        <!-- 底图图层 -->
        <el-collapse-item name="base" title="🗺️ 底图">
          <div class="layer-list">
            <div 
              v-for="layer in baseLayers" 
              :key="layer.name"
              class="layer-item"
            >
              <el-radio 
                v-model="selectedBase" 
                :label="layer.name"
                @change="handleBaseChange(layer.name)"
              >
                {{ layer.label }}
              </el-radio>
            </div>
          </div>
        </el-collapse-item>
        
        <!-- 业务图层 -->
        <el-collapse-item name="business" title="🌲 业务图层">
          <div class="layer-list">
            <div 
              v-for="layer in businessLayers" 
              :key="layer.name"
              class="layer-item"
            >
              <div class="layer-row">
                <el-checkbox 
                  v-model="layer.visible"
                  @change="handleVisibilityChange(layer)"
                >
                  {{ layer.label }}
                </el-checkbox>
                <el-tag 
                  v-if="layer.name === 'heatmap'" 
                  size="small" 
                  type="warning"
                  effect="plain"
                >
                  实验
                </el-tag>
              </div>
              
              <!-- 透明度滑块 -->
              <div v-if="layer.visible" class="opacity-control">
                <span class="opacity-label">透明度</span>
                <el-slider 
                  v-model="layer.opacity"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  size="small"
                  @change="handleOpacityChange(layer)"
                />
                <span class="opacity-value">{{ Math.round(layer.opacity * 100) }}%</span>
              </div>
            </div>
          </div>
        </el-collapse-item>
        
        <!-- 图层信息 -->
        <el-collapse-item name="info" title="ℹ️ 图层信息">
          <div class="layer-info">
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="当前缩放">
                {{ currentZoom?.toFixed(1) || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="可见图层">
                {{ visibleLayerCount }} 个
              </el-descriptions-item>
              <el-descriptions-item label="坐标系">
                EPSG:3857
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <!-- 快捷操作 -->
      <div class="quick-actions">
        <el-button 
          type="primary" 
          size="small" 
          plain 
          @click="handleShowAll"
        >
          显示全部
        </el-button>
        <el-button 
          type="info" 
          size="small" 
          plain 
          @click="handleHideAll"
        >
          隐藏业务
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  layers: {
    type: Array,
    default: () => []
  },
  currentZoom: {
    type: Number,
    default: 12
  }
})

const emit = defineEmits(['toggle', 'opacity-change', 'base-change'])

// ==================== 状态管理 ====================
// 默认收起所有面板
const activeNames = ref([])
// 默认收起状态
const expandAll = ref(false)
const selectedBase = ref('base')

// ==================== 计算属性 ====================

// 底图图层
const baseLayers = computed(() => {
  return props.layers.filter(l => ['base', 'satellite'].includes(l.name))
})

// 业务图层
const businessLayers = computed(() => {
  return props.layers.filter(l => !['base', 'satellite'].includes(l.name))
})

// 可见图层数量
const visibleLayerCount = computed(() => {
  return props.layers.filter(l => l.visible).length
})

// ==================== 方法 ====================

// 展开/收起全部面板
const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
  if (expandAll.value) {
    activeNames.value = ['base', 'business', 'info']
  } else {
    activeNames.value = []
  }
}

// 底图切换（互斥）
const handleBaseChange = (layerName) => {
  // 隐藏其他底图
  baseLayers.value.forEach(layer => {
    if (layer.name !== layerName) {
      layer.visible = false
      emit('toggle', layer.name, false)
    }
  })
  
  // 显示选中的底图
  const selected = baseLayers.value.find(l => l.name === layerName)
  if (selected) {
    selected.visible = true
    emit('toggle', layerName, true)
    emit('base-change', layerName)
  }
}

// 可见性切换
const handleVisibilityChange = (layer) => {
  emit('toggle', layer.name, layer.visible)
}

// 透明度调整
const handleOpacityChange = (layer) => {
  emit('opacity-change', layer.name, layer.opacity)
}

// 显示全部
const handleShowAll = () => {
  businessLayers.value.forEach(layer => {
    layer.visible = true
    emit('toggle', layer.name, true)
  })
}

// 隐藏业务图层
const handleHideAll = () => {
  businessLayers.value.forEach(layer => {
    layer.visible = false
    emit('toggle', layer.name, false)
  })
}

// ==================== 监听 ====================

watch(() => props.layers, (newLayers) => {
  // 同步选中的底图状态
  const activeBase = newLayers.find(l => 
    ['base', 'satellite'].includes(l.name) && l.visible
  )
  if (activeBase) {
    selectedBase.value = activeBase.name
  }
}, { deep: true, immediate: true })

// 监听折叠面板状态变化，同步到 expandAll
watch(activeNames, (newActiveNames) => {
  expandAll.value = newActiveNames.length > 0
}, { immediate: true })
</script>

<style scoped>
.layer-control {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 260px;
  z-index: 100;
}

.layer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #2E7D32;
}

:deep(.el-card__header) {
  padding: 12px 16px;
}

:deep(.el-collapse-item__header) {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  padding-left: 8px;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 8px;
}

.layer-list {
  padding: 4px 8px;
}

.layer-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.layer-item:last-child {
  border-bottom: none;
}

.layer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.opacity-control {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding-left: 24px;
  gap: 8px;
}

.opacity-label {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
}

.opacity-value {
  font-size: 11px;
  color: #606266;
  min-width: 32px;
  text-align: right;
}

:deep(.el-slider) {
  flex: 1;
}

:deep(.el-slider__runway) {
  margin: 8px 0;
}

.layer-info {
  padding: 8px;
}

.quick-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

/* 响应式 */
@media (max-width: 768px) {
  .layer-control {
    width: calc(100% - 40px);
    max-width: 300px;
    right: 10px;
    top: 10px;
  }
  
  :deep(.el-card__header) {
    padding: 10px 12px;
  }
}
</style>