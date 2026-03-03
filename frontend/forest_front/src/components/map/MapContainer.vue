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
    
    <!-- 编辑模式提示 -->
    <div v-if="editMode" class="edit-mode-banner">
      <el-alert
        title="编辑模式：点击地图选择位置"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <el-button 
            type="primary" 
            size="small" 
            @click="exitEditMode"
            style="margin-left: 16px;"
          >
            退出编辑
          </el-button>
        </template>
      </el-alert>
    </div>
    
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
      @edit="handleEditFromPopup"
    />
    
    <!-- 筛选面板 -->
    <div class="filter-panel">
      <el-card shadow="hover">
        <template #header>
          <div class="panel-header">
            <span>🔍 筛选条件</span>
            <el-button 
              type="primary" 
              link 
              size="small"
              @click="openCreateDrawer"
            >
              <el-icon><Plus /></el-icon>
              新增林分
            </el-button>
          </div>
        </template>
        <el-form label-width="70px" size="small">
          <!-- 树种 -->
          <el-form-item label="树种">
            <el-select 
              v-model="filters.species" 
              placeholder="全部树种"
              clearable
              style="width: 100%"
              @change="handleFilterChange"
              @clear="handleClearSpecies"
            >
              <el-option
                v-for="species in availableSpecies"
                :key="species"
                :label="species"
                :value="species"
              />
            </el-select>
          </el-form-item>
          
          <!-- 起源 -->
          <el-form-item label="起源">
            <el-select 
              v-model="filters.origin" 
              placeholder="全部起源"
              clearable
              style="width: 100%"
              @change="handleFilterChange"
              @clear="handleClearOrigin"
            >
              <el-option label="人工林" value="人工" />
              <el-option label="天然林" value="天然" />
            </el-select>
          </el-form-item>
          
          <!-- 最小蓄积  -->
          <div class="filter-item">
            <span class="filter-label">最小蓄积</span>
            <el-slider
              v-model="filters.minVolume"
              :max="500"
              :step="10"
              @change="handleFilterChange"
            />
          </div>
          
          <!-- 按钮 -->
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
    
    <!-- 编辑抽屉 -->
    <StandEditDrawer
      v-model="drawerVisible"
      :edit-data="currentEditStand"
      :species-options="availableSpecies"
      @submit="handleSaveStand"
      @delete="handleDeleteStand"
      @location-change="handleLocationChange"
      @start-map-selection="startMapSelection"
      ref="editDrawerRef"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'  // 添加路由
import { ElMessage } from 'element-plus'
import { toLonLat, fromLonLat } from 'ol/proj'
import { Feature } from 'ol'
import { Point } from 'ol/geom'
import { Vector as VectorLayer } from 'ol/layer'
import { Vector as VectorSource } from 'ol/source'
import { Style, Circle as CircleStyle, Fill, Stroke } from 'ol/style'
import { Plus } from '@element-plus/icons-vue'
import LoadingMask from '@/components/common/LoadingMask.vue'
import LayerControl from '@/components/map/LayerControl.vue'
import MapPopup from '@/components/map/MapPopup.vue'
import RadiusQuery from '@/components/map/RadiusQuery.vue'
import StandEditDrawer from '@/components/stand/StandEditDrawer.vue'
import { useMap } from '@/composables/useMap'
import { fetchStands, createStand, updateStand, deleteStand } from '@/api/forest'

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

// 添加路由
const router = useRouter()

// 引用和状态定义保持不变...
const radiusQueryRef = ref(null)
const editDrawerRef = ref(null)
const isInitialized = ref(false)
const error = ref(null)
const currentZoom = ref(props.initialZoom)
const mousePosition = ref('')
const radiusQueryActive = ref(false)
const radiusQueryRadius = ref(1000)
const drawerVisible = ref(false)
const currentEditStand = ref(null)
const editMode = ref(false)
const mapClickListener = ref(null)
const tempMarkerLayer = ref(null)

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

// useMap 保持不变
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
  closePopup: mapClosePopup,
  popupContent,
  popupVisible
} = mapState

// ==================== 移动端检测 ====================
const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768 || /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

onMounted(async () => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  
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
    
    map.value?.on('click', handleMapClick)
    
    await loadInitialData()
    
  } catch (err) {
    error.value = '地图初始化失败: ' + err.message
    emit('error', err)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  exitEditMode()
  destroyMap()
})

// 其他方法保持不变...
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

// ==================== 修改后的地图点击处理 ====================
const handleMapClick = (evt) => {
  // 如果不在编辑模式，处理林分点击
  if (!editMode.value) {
    const feature = map.value.forEachFeatureAtPixel(evt.pixel, (f) => f)
    if (feature) {
      const standData = feature.get('standData') || feature.getProperties()
      if (standData && (standData.standId || standData.id)) {
        // 移动端跳转到新标签页，桌面端显示弹窗
        if (isMobile.value) {
          openStandDetailInNewTab(standData)
        } else {
          showStandPopup(standData, evt.coordinate)
        }
      }
    }
  }
}

// ==================== 移动端：在新标签页打开详情 ====================
const openStandDetailInNewTab = (stand) => {
  const standId = stand.standId || stand.id
  if (!standId) return
  
  // 构建详情页 URL
  const detailUrl = `${window.location.origin}/stand/${standId}`
  
  // 在新标签页打开
  window.open(detailUrl, '_blank')
  
  ElMessage.success('已在新标签页打开林分详情')
}

// ==================== 桌面端：显示弹窗 ====================
const showStandPopup = (stand, coordinate) => {
  const data = {
    type: 'stand_detail',
    standId: stand.standId || stand.id,
    standName: stand.standName || '未命名林分',
    standNo: stand.standNo || stand.xiaoBanCode || '-',
    species: stand.dominantSpecies || '未知',
    origin: stand.origin || '未知',
    area: stand.areaHa || 0,
    volumePerHa: stand.volumePerHa || 0,
    totalVolume: ((stand.volumePerHa || 0) * (stand.areaHa || 0)).toFixed(2),
    age: stand.standAge || '-',
    density: stand.canopyDensity || '-',
    _raw: stand
  }
  
  showPopup(data, coordinate)
}

// 其他方法保持不变...
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

const handleClearSpecies = () => {
  filters.value.species = ''
  handleFilterChange()
}

const handleClearOrigin = () => {
  filters.value.origin = ''
  handleFilterChange()
}

const handleFilterChange = () => {
  doApplyFilters()
}

const doApplyFilters = () => {
  if (!applyFilters) {
    console.warn('applyFilters 方法不可用')
    return
  }
  
  const safeFilters = {
    species: filters.value.species || '',
    origin: filters.value.origin || '',
    minVolume: filters.value.minVolume || 0
  }
  
  applyFilters(safeFilters)
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

const openCreateDrawer = () => {
  currentEditStand.value = null
  drawerVisible.value = true
}

const openEditDrawer = (stand) => {
  currentEditStand.value = { ...stand }
  drawerVisible.value = true
}

const handleSaveStand = async (formData, isEdit) => {
  try {
    const standId = formData.standId || formData.id
    
    if (isEdit && standId) {
      await updateStand(standId, formData)
      ElMessage.success('林分更新成功')
    } else {
      await createStand(formData)
      ElMessage.success('林分创建成功')
    }
    
    await loadInitialData()
    drawerVisible.value = false
    exitEditMode()
    
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleDeleteStand = async (stand) => {
  try {
    await ElMessageBox.confirm('确定要删除这个林分吗？此操作不可恢复！', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const standId = stand.standId || stand.id
    if (!standId) {
      throw new Error('无法获取林分ID')
    }
    
    await deleteStand(standId)
    ElMessage.success('林分已删除')
    
    await loadInitialData()
    closePopup()
    drawerVisible.value = false
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const startMapSelection = () => {
  editMode.value = true
  
  if (map.value && !mapClickListener.value) {
    mapClickListener.value = (evt) => {
      const coord = toLonLat(evt.coordinate)
      const [lon, lat] = coord
      
      editDrawerRef.value?.setMapLocation(lon, lat)
      addTempMarker(evt.coordinate)
    }
    
    map.value.on('click', mapClickListener.value)
  }
}

const exitEditMode = () => {
  editMode.value = false
  
  if (map.value && mapClickListener.value) {
    map.value.un('click', mapClickListener.value)
    mapClickListener.value = null
  }
  
  clearTempMarker()
}

const addTempMarker = (coordinate) => {
  clearTempMarker()
  
  const feature = new Feature({
    geometry: new Point(coordinate),
    name: 'selected-location'
  })
  
  const source = new VectorSource({
    features: [feature]
  })
  
  tempMarkerLayer.value = new VectorLayer({
    source: source,
    style: new Style({
      image: new CircleStyle({
        radius: 10,
        fill: new Fill({ color: '#FF5722' }),
        stroke: new Stroke({ color: '#fff', width: 3 })
      })
    }),
    zIndex: 1000
  })
  
  map.value.addLayer(tempMarkerLayer.value)
}

const clearTempMarker = () => {
  if (tempMarkerLayer.value && map.value) {
    map.value.removeLayer(tempMarkerLayer.value)
    tempMarkerLayer.value = null
  }
}

const handleLocationChange = ({ lon, lat }) => {
  if (map.value && view.value) {
    const coordinate = fromLonLat([lon, lat])
    
    view.value.animate({
      center: coordinate,
      zoom: 16,
      duration: 500
    })
    
    addTempMarker(coordinate)
  }
}

const handleEditFromPopup = () => {
  const stand = popupContent.value?._raw
  if (stand) {
    closePopup()
    openEditDrawer(stand)
  }
}

const closePopup = () => {
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
      const fid = f.get('standId') || f.get('id') || f.get('zone_id') || f.get('stand_id')
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

// ==================== 半径查询相关 ====================
const handleShowCircleChange = (visible) => {
  const highlightLayer = mapState.getLayerByName('highlight')
  if (highlightLayer) {
    highlightLayer.setVisible(visible)
  }
}

const handleRadiusSelectStand = (stand) => {
  console.log('半径查询结果中选择林分:', stand)
  
  // 移动端也跳转到新标签页
  if (isMobile.value) {
    openStandDetailInNewTab(stand)
    return
  }
  
  if (view.value && stand.centerLon && stand.centerLat) {
    view.value.animate({
      center: fromLonLat([stand.centerLon, stand.centerLat]),
      duration: 500
    })
  }
  
  const coordinate = fromLonLat([stand.centerLon, stand.centerLat])
  
  const data = {
    type: 'stand_detail',
    standId: stand.standId || stand.id,
    standName: stand.standName || '未命名林分',
    standNo: stand.standNo || stand.xiaoBanCode || '-',
    species: stand.dominantSpecies || '未知',
    origin: stand.origin || '未知',
    area: stand.areaHa || 0,
    volumePerHa: stand.volumePerHa || 0,
    totalVolume: ((stand.volumePerHa || 0) * (stand.areaHa || 0)).toFixed(2),
    age: stand.standAge || '-',
    density: stand.canopyDensity || '-',
    _raw: stand
  }
  
  showPopup(data, coordinate)
  highlightStandById(stand.standId || stand.id)
}

const highlightStandById = (standId) => {
  const markerLayer = mapState.getLayerByName('stands_markers')
  if (!markerLayer) return
  
  const markers = markerLayer.getSource().getFeatures()
  const marker = markers.find(f => {
    const fid = f.get('standId') || f.get('id') || f.get('zone_id') || f.get('stand_id')
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
  
  const formatVolume = (v) => {
    if (!v || v === 0) return '0 m³'
    if (v >= 10000) return (v / 10000).toFixed(2) + ' 万m³'
    return v.toFixed(2) + ' m³'
  }
  
  ElMessage.success(`找到 ${stands.length} 个林分，总蓄积 ${formatVolume(totalVolume)}`)
}

defineExpose({
  refresh: loadInitialData,
  highlightStand: highlightStandById
})
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
  min-height: 400px;
  transform: translateZ(0);
  will-change: transform;
}

.map-error {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1001;
  width: 400px;
}

.edit-mode-banner {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  width: 500px;
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

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  
  .edit-mode-banner {
    width: calc(100% - 40px);
  }
}

.filter-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

@media (max-width: 768px) {
  /* 筛选面板改为底部抽屉式 */
  .filter-panel {
    position: fixed;
    left: 10px;
    right: 10px;
    top: auto;
    bottom: 50px;
    width: auto;
    max-width: none;
    max-height: 45vh;
    overflow-y: auto;
    z-index: 100;
    transition: transform 0.3s ease;
  }

  /* 图层控制移到左上角 */
  :deep(.layer-control) {
    position: fixed;
    left: 10px;
    top: 70px;
    right: auto;
    width: auto;
    min-width: 200px;
    max-width: calc(100% - 20px);
  }

  /* 半径查询简化为小按钮 */
  :deep(.radius-query) {
    position: fixed;
    right: 10px;
    top: 70px;
    left: auto;
    width: auto;
  }

  :deep(.radius-query .el-card) {
    max-width: 200px;
  }

  /* 隐藏非关键信息 */
  .map-info {
    display: none;
  }

  .map-container {
    /* 降低图像质量提升性能 */
    image-rendering: pixelated;
  }

  /* 编辑提示优化 */
  .edit-mode-banner {
    left: 10px;
    right: 10px;
    width: auto;
    transform: none;
    top: auto;
    bottom: 20px;
  }

  /* 增大触摸目标 */
  :deep(.el-button),
  :deep(.el-radio__input),
  :deep(.el-checkbox__input) {
    min-height: 44px;
    min-width: 44px;
  }

  :deep(.el-slider__runway) {
    height: 8px;
    margin: 12px 0;
  }

  :deep(.el-slider__button) {
    width: 20px;
    height: 20px;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .filter-panel,
  :deep(.layer-control),
  :deep(.radius-query) {
    backdrop-filter: blur(10px);
    background: rgba(255,255,255,0.95);
  }
}

</style>