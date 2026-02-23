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
    
    <RadiusQuery
      v-model:active="radiusQueryActive"
      v-model:radius="radiusQueryRadius"
      @query="handleRadiusQuery"
    />
    
    <MapPopup
      ref="popupRef"
      :content="popupContent"
      :visible="popupVisible"
      @close="closePopup"
      @zoom-to="handleZoomTo"
      @show-detail="handleShowDetail"
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
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { toLonLat, fromLonLat } from 'ol/proj'
import LoadingMask from '@/components/common/LoadingMask.vue'
import LayerControl from '@/components/map/LayerControl.vue'
import MapPopup from '@/components/map/MapPopup.vue'
import RadiusQuery from '@/components/map/RadiusQuery.vue'
import { useMap } from '@/composables/useMap'
import { fetchStands, fetchNearbyStands } from '@/api/forest'

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

const popupRef = ref(null)
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
  { name: 'stands', label: '林分分布', visible: true, opacity: 0.9 }, // 默认勾选
  { name: 'heatmap', label: '蓄积热力图', visible: false, opacity: 0.8 }
])

const popupVisible = ref(false)
const popupContent = ref(null)

// 从 useMap 获取方法和状态
const mapState = useMap(props.targetId, {
  onRadiusQueryResult: handleRadiusQueryResult,
  radiusQuery: {
    active: radiusQueryActive,
    radius: radiusQueryRadius
  }
})

// 解构获取需要的方法
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
  destroyMap
} = mapState

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

const handleLayerToggle = (layerName, visible) => {
  toggleLayer(layerName, visible)
  
  const layer = layerList.value.find(l => l.name === layerName)
  if (layer) layer.visible = visible
  
  if (layerName === 'base' && visible) {
    toggleLayer('satellite', false)
    layerList.value.find(l => l.name === 'satellite').visible = false
  } else if (layerName === 'satellite' && visible) {
    toggleLayer('base', false)
    layerList.value.find(l => l.name === 'base').visible = false
  }
}

const handleOpacityChange = (layerName, opacity) => {
  setLayerOpacity(layerName, opacity)
  const layer = layerList.value.find(l => l.name === layerName)
  if (layer) layer.opacity = opacity
}

// 统一处理筛选变化
const handleFilterChange = () => {
  doApplyFilters()
}

// 执行筛选
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

const showPopup = (data, coordinate) => {
  popupContent.value = data
  popupVisible.value = true
  popupRef.value?.setPosition(coordinate)
}

const closePopup = () => {
  popupVisible.value = false
  popupContent.value = null
  clearHighlight()
}

const handleZoomTo = (standId) => {
  highlightStand(standId)
  closePopup()
}

const handleShowDetail = (standId) => {
  emit('stand-select', standId)
  closePopup()
}

const handleRadiusQuery = async (lon, lat) => {
  if (!radiusQueryActive.value) return
  
  try {
    ElMessage.info(`正在查询 ${radiusQueryRadius.value}m 范围内的林分...`)
    const stands = await fetchNearbyStands(lon, lat, radiusQueryRadius.value)
    emit('radius-query-result', stands, lon, lat, radiusQueryRadius.value)
    ElMessage.success(`找到 ${stands.length} 个林分`)
  } catch (err) {
    ElMessage.error('半径查询失败: ' + err.message)
  }
}

function handleRadiusQueryResult(stands, lon, lat, radius) {
  showPopup({
    type: 'radius',
    stands,
    lon,
    lat,
    radius
  }, fromLonLat([lon, lat]))
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