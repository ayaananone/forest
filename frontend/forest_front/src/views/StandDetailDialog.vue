<template>
  <div class="map-wrapper">
    <div :id="targetId" class="map-container"></div>
    
    <LoadingMask 
      :visible="!isInitialized" 
      text="地图加载中..."
      :z-index="1000"
    />
    
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      class="map-error"
      closable
      @close="error = null"
    />
    
    <LayerControl 
      :layers="layerList"
      @toggle="handleLayerToggle"
      @opacity-change="handleOpacityChange"
    />
    
    <!-- 半径查询组件 -->
    <RadiusQuery
      ref="radiusQueryRef"
      v-model:active="radiusQueryActive"
      v-model:radius="radiusQueryRadius"
      @show-circle-change="handleShowCircleChange"
      @select-stand="handleRadiusSelectStand"
    />
    
    <!-- 弹窗组件 -->
    <MapPopup
      id="popup"
      :content="popupContent"
      :visible="popupVisible"
      @close="closePopup"
      @zoom-to="handleZoomTo"
      @show-detail="handleShowDetailClick"
    />
    
    <!-- 🔴 新增：小班详情弹窗 -->
    <StandDetailDialog
      v-model="detailDialogVisible"
      :stand-info="currentStandInfo"
    />
    
    <!-- 筛选面板 -->
    <div class="filter-panel">
      <el-card shadow="hover">
        <template #header>
          <span>🔍 筛选条件</span>
        </template>
        <el-form label-width="70px" size="small">
          <el-form-item label="树种">
            <el-select 
              id="filter-species"
              v-model="filters.species" 
              placeholder="全部树种"
              clearable
              style="width: 100%"
              @change="handleFilterChange"
            >
              <el-option
                v-for="species in availableSpecies"
                :key="species"
                :label="species"
                :value="species"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="起源">
            <el-select 
              id="filter-origin"
              v-model="filters.origin" 
              placeholder="全部起源"
              clearable
              style="width: 100%"
              @change="handleFilterChange"
            >
              <el-option label="人工林" value="人工" />
              <el-option label="天然林" value="天然" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="最小蓄积">
            <el-slider
              id="filter-volume"
              v-model="filters.minVolume"
              :max="500"
              :step="10"
              @change="handleFilterChange"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" size="small" @click="resetFilters">
              重置筛选
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <div class="map-info">
      <span>缩放: {{ currentZoom }}</span>
      <span v-if="mousePosition"> | 坐标: {{ mousePosition }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { toLonLat, fromLonLat } from 'ol/proj'
import LoadingMask from '@/components/common/LoadingMask.vue'
import LayerControl from '@/components/map/LayerControl.vue'
import MapPopup from '@/components/map/MapPopup.vue'
import RadiusQuery from '@/components/map/RadiusQuery.vue'
import StandDetailDialog from '@/components/map/StandDetailDialog.vue'  // 🔴 新增
import { useMap } from '@/composables/useMap'
import { fetchStands, formatVolume } from '@/api/forest'  // 🔴 修改导入

const props = defineProps({
  targetId: {
    type: String,
    default: 'map'
  },
  initialZoom: {
    type: Number,
    default: 12
  }
})

const emit = defineEmits([
  'stand-select', 
  'radius-query-result',
  'error'
])

// 引用
const radiusQueryRef = ref(null)

// 状态
const isInitialized = ref(false)
const error = ref(null)
const currentZoom = ref(props.initialZoom)
const mousePosition = ref('')
const radiusQueryActive = ref(false)
const radiusQueryRadius = ref(1000)

const filters = ref({
  species: '',
  origin: '',
  minVolume: 0
})

const availableSpecies = ref([
  '马尾松', '杉木', '樟树',
  '枫香', '木荷', '毛竹', '油茶', '未知'
])

const layerList = ref([
  { name: 'base', label: '街道地图', visible: true, opacity: 1 },
  { name: 'satellite', label: '卫星影像', visible: false, opacity: 1 },
  { name: 'stands', label: '林分分布', visible: true, opacity: 0.9 },
  { name: 'heatmap', label: '蓄积热力图', visible: false, opacity: 0.8 }
])

// 弹窗状态
const popupVisible = ref(false)
const popupContent = ref(null)

// 🔴 新增：详情弹窗状态
const detailDialogVisible = ref(false)
const currentStandInfo = ref({})

// 使用 useMap
const mapState = useMap(props.targetId, {
  onRadiusQueryResult: handleRadiusQueryResult,
  radiusQuery: {
    active: radiusQueryActive,
    radius: radiusQueryRadius
  }
})

const {
  map,
  view,
  initMap,
  toggleLayer,
  setLayerOpacity,
  highlightStand,
  clearHighlight,
  applyFilters,
  loadHeatmapFeatures,
  destroyMap,
  showPopup,
  closePopup: mapClosePopup
} = mapState

// 监听 useMap 中的弹窗状态
watch(() => mapState.popupVisible?.value, (newVal) => {
  popupVisible.value = newVal || false
})

watch(() => mapState.popupContent?.value, (newVal) => {
  popupContent.value = newVal
  
  // 🔴 处理半径查询结果
  if (newVal?.type === 'radius') {
    if (radiusQueryRef.value) {
      radiusQueryRef.value.setResult(newVal)
    }
    if (newVal.stands?.length > 0 && view.value) {
      fitViewToStands(newVal.stands)
    }
  }
})

onMounted(async () => {
  try {
    await initMap()
    isInitialized.value = true
    
    view.value?.on('change:resolution', () => {
      currentZoom.value = view.value.getZoom()
    })
    
    map.value?.on('pointermove', (evt) => {
      const coord = toLonLat(evt.coordinate)
      mousePosition.value = `${coord[0].toFixed(4)}, ${coord[1].toFixed(4)}`
    })
    
    await loadInitialData()
    
  } catch (err) {
    error.value = '地图初始化失败: ' + err.message
    emit('error', err)
  }
})

onUnmounted(() => {
  destroyMap()
})

const loadInitialData = async () => {
  try {
    const stands = await fetchStands()
    loadHeatmapFeatures(stands)
    ElMessage.success(`加载了 ${stands.length} 个林分数据`)
  } catch (err) {
    console.error('加载数据失败:', err)
    ElMessage.warning('林分数据加载失败')
  }
}

// 调整视图以显示所有林分
const fitViewToStands = (stands) => {
  if (!view.value || stands.length === 0) return
  
  const coords = stands.map(s => fromLonLat([s.centerLon, s.centerLat]))
  
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  
  coords.forEach(coord => {
    minX = Math.min(minX, coord[0])
    minY = Math.min(minY, coord[1])
    maxX = Math.max(maxX, coord[0])
    maxY = Math.max(maxY, coord[1])
  })
  
  const padding = 100
  const extent = [minX - padding, minY - padding, maxX + padding, maxY + padding]
  
  view.value.fit(extent, {
    padding: [100, 100, 100, 100],
    duration: 500,
    maxZoom: 16
  })
}

// ==================== 图层控制 ====================
const handleLayerToggle = (layerName, visible) => {
  toggleLayer(layerName, visible)
  
  const layer = layerList.value.find(l => l.name === layerName)
  if (layer) layer.visible = visible
  
  if (layerName === 'base' && visible) {
    toggleLayer('satellite', false)
    const satLayer = layerList.value.find(l => l.name === 'satellite')
    if (satLayer) satLayer.visible = false
  } else if (layerName === 'satellite' && visible) {
    toggleLayer('base', false)
    const baseLayer = layerList.value.find(l => l.name === 'base')
    if (baseLayer) baseLayer.visible = false
  }
}

const handleOpacityChange = (layerName, opacity) => {
  setLayerOpacity(layerName, opacity)
  const layer = layerList.value.find(l => l.name === layerName)
  if (layer) layer.opacity = opacity
}

// ==================== 筛选 ====================
const handleFilterChange = () => {
  doApplyFilters()
}

const doApplyFilters = () => {
  if (!applyFilters) {
    console.warn('applyFilters 方法不可用')
    return
  }
  
  applyFilters({
    species: filters.value.species,
    origin: filters.value.origin,
    minVolume: filters.value.minVolume
  })
}

const resetFilters = () => {
  filters.value = {
    species: '',
    origin: '',
    minVolume: 0
  }
  doApplyFilters()
  clearHighlight()
  ElMessage.success('筛选已重置')
}

// ==================== 弹窗处理 ====================
const closePopup = () => {
  popupVisible.value = false
  popupContent.value = null
  clearHighlight()
  if (mapClosePopup) {
    mapClosePopup()
  }
}

const handleZoomTo = (standId) => {
  console.log('居中显示林场:', standId)
  
  const markerLayer = mapState.getLayerByName('stands_markers')
  if (markerLayer && view.value) {
    const markers = markerLayer.getSource().getFeatures()
    const marker = markers.find(f => {
      const fid = f.get('id') || f.get('zone_id') || f.get('stand_id') || f.get('xiao_ban_code')
      return String(fid) === String(standId)
    })
    
    if (marker) {
      const geom = marker.getGeometry()
      const center = geom.getCoordinates()
      
      view.value.animate({
        center: center,
        duration: 500
      })
    }
  }
  
  closePopup()
}

// 🔴 新增：处理"查看详情"按钮点击
const handleShowDetailClick = (standId) => {
  console.log('查看详情按钮点击，ID:', standId)
  
  if (popupContent.value && popupContent.value.type === 'stand_detail') {
    // 保存当前小班信息
    currentStandInfo.value = {
      id: popupContent.value.id,
      name: popupContent.value.name,
      standNo: popupContent.value.standNo,
      species: popupContent.value.species,
      origin: popupContent.value.origin,
      area: popupContent.value.area,
      volumePerHa: popupContent.value.volumePerHa,
      totalVolume: popupContent.value.totalVolume,
      age: popupContent.value.age,
      density: popupContent.value.density
    }
    
    console.log('打开详情弹窗，小班信息:', currentStandInfo.value)
    
    // 显示详情弹窗
    detailDialogVisible.value = true
    
    // 关闭地图弹窗
    closePopup()
  }
}

// ==================== 半径查询相关 ====================
const handleShowCircleChange = (visible) => {
  const highlightLayer = mapState.getLayerByName('highlight')
  if (highlightLayer) {
    highlightLayer.setVisible(visible)
  }
}

const handleRadiusSelectStand = (stand) => {
  console.log('半径查询结果中选择林分:', stand)
  
  if (view.value && stand.centerLon && stand.centerLat) {
    view.value.animate({
      center: fromLonLat([stand.centerLon, stand.centerLat]),
      duration: 500
    })
  }
  
  const coordinate = fromLonLat([stand.centerLon, stand.centerLat])
  
  const data = {
    type: 'stand_detail',
    id: stand.id || stand.xiaoBanCode,
    name: stand.standName || stand.xiaoBanCode || '未命名林分',
    standNo: stand.xiaoBanCode || '-',
    species: stand.dominantSpecies || '未知',
    origin: stand.origin || '未知',
    area: stand.area || 0,
    volumePerHa: stand.volumePerHa || 0,
    totalVolume: (stand.volumePerHa || 0) * (stand.area || 0),
    age: stand.standAge || '-',
    density: stand.canopyDensity || '-'
  }
  
  showPopup(data, coordinate)
  
  highlightStandById(stand.id || stand.xiaoBanCode)
}

const highlightStandById = (standId) => {
  const markerLayer = mapState.getLayerByName('stands_markers')
  if (!markerLayer) return
  
  const markers = markerLayer.getSource().getFeatures()
  const marker = markers.find(f => {
    const fid = f.get('id') || f.get('zone_id') || f.get('stand_id') || f.get('xiao_ban_code')
    return String(fid) === String(standId)
  })
  
  if (marker) {
    highlightStand(marker)
  }
}

function handleRadiusQueryResult(stands, lon, lat, radius) {
  console.log('半径查询结果:', stands.length, '个林分')
  emit('radius-query-result', stands, lon, lat, radius)
  
  const totalVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0) * (s.area || 0), 0)
  ElMessage.success(`找到 ${stands.length} 个林分，总蓄积 ${formatVolume(totalVolume)}`)
}
</script>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
  background-color: #f0f2f5;
  display: block;
}

.map-error {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1001;
  width: 400px;
}

.filter-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 280px;
  z-index: 100;
}

.filter-panel :deep(.el-card__header) {
  padding: 12px 16px;
  font-weight: 600;
  color: #2E7D32;
}

.filter-panel :deep(.el-card__body) {
  padding: 16px;
}

.map-info {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

@media (max-width: 768px) {
  .filter-panel {
    width: calc(100% - 40px);
    max-width: 300px;
  }
  
  .map-info {
    display: none;
  }
}
</style>