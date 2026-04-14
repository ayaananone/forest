<template>
  <div class="cesium-container" ref="cesiumContainer">
    <div v-if="loadError" class="error-overlay">
      <p><strong>Cesium 加载失败:</strong> {{ loadError }}</p>
      <el-button type="danger" @click="$emit('back-to-2d')">返回 2D 地图</el-button>
    </div>
    
    <div v-else class="control-panel">
      <!-- 切换显示模式：点 vs 3D柱 -->
      <el-button size="small" @click="toggleViewMode" type="warning" plain>
        {{ is3DView ? '切换为点位' : '切换为3D柱' }}
      </el-button>
      <!-- 测距工具 -->
      <el-button size="small" @click="toggleMeasure" :type="isMeasuring ? 'danger' : 'info'" plain>
        {{ isMeasuring ? '结束测距' : '空间测距' }}
      </el-button>
      <el-button type="primary" size="small" @click="goTo2D" class="back-btn">
        {{ selectedStand ? '定位到 2D' : '返回 2D' }}
      </el-button>
      <el-button type="success" size="small" @click="resetView">重置视角</el-button>
    </div>

    <!-- 测距结果提示 -->
    <div v-if="measureDistance > 0" class="measure-tip">
      总距离: <strong>{{ measureDistance }} 米</strong>
    </div>

    <!-- 右侧信息面板 -->
    <div v-if="selectedStand" class="info-card">
      <div class="info-header">
        <span>{{ selectedStand.standName || '未命名林分' }}</span>
        <el-button type="danger" link size="small" @click="clearSelection"><el-icon><Close /></el-icon></el-button>
      </div>
      <div class="info-body">
        <p>小班号: {{ selectedStand.xiaoBanCode || '-' }}</p>
        <p>优势树种: <el-tag size="small" :color="getSpeciesColor(selectedStand.dominantSpecies)" effect="dark" style="border:none;">{{ selectedStand.dominantSpecies || '未知' }}</el-tag></p>
        <p>每公顷蓄积: {{ selectedStand.volumePerHa || 0 }} m³/ha</p>
        <p>面积: {{ selectedStand.areaHa || 0 }} 公顷</p>
        <p style="margin-top: 15px;">
          <el-button type="primary" size="small" @click="goTo2D" style="width: 100%;">在 2D 地图中查看</el-button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as Cesium from 'cesium'
import { Close } from '@element-plus/icons-vue'
import request from '@/api/request'
import { SPECIES_COLORS } from '@/config'

const emit = defineEmits(['back-to-2d'])
const cesiumContainer = ref(null)
let viewer = null
let handler = null
const loadError = ref(null)
const selectedStand = ref(null)
let hoveredEntity = null

// 新增状态
const is3DView = ref(true)      // 是否显示3D圆柱
const isMeasuring = ref(false)  // 是否正在测距
const measureDistance = ref(0)   // 测距结果
let measurePoints = []          // 测距点集合
let measureEntities = []        // 测距线段实体

const getSpeciesColor = (species) => SPECIES_COLORS[species] || '#757575'

onMounted(async () => {
  try {
    Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2Y2MxNjk0NS1jZTUyLTQ5YWItYmE1MS1hYjA1Y2E2MzgzNzMiLCJpZCI6NDEzMjI4LCJpYXQiOjE3NzUyMDA4OTB9.nReZH9x5Bi7B36KGKsC8uTdyaUM-GQKFU5udbOvtSHs' //每次更新都得删一次好烦
    if (!cesiumContainer.value) throw new Error('找不到 DOM 容器')

    viewer = new Cesium.Viewer(cesiumContainer.value, {
      animation: false, timeline: false, baseLayerPicker: false, geocoder: false,
      homeButton: false, sceneModePicker: false, navigationHelpButton: false,
      fullscreenButton: false, infoBox: false, selectionIndicator: false, 
      terrain: Cesium.Terrain.fromWorldTerrain(), 
    })

    // ==================== 1. 性能优化 ====================
    viewer.scene.globe.depthTestAgainstTerrain = true // 开启地形深度检测（防止圆柱体钻入地下）
    viewer.scene.requestRenderMode = true 
    viewer.scene.maximumRenderTimeChange = Infinity 
    
    // ==================== 2. 相机防穿模与光影 ====================
    // 限制相机最小高度，防止钻入地下导致画面闪烁
    viewer.scene.screenSpaceCameraController.minimumZoomDistance = 50 
    // 开启大气散射和光影效果（质感拉满，但稍微耗性能，如果卡可以注释掉光照）
    viewer.scene.globe.enableLighting = true 

    // WMS 图层 (加了性能优化参数)
    try {
      const wmsLayer = viewer.imageryLayers.addImageryProvider(
        new Cesium.WebMapServiceImageryProvider({
          url: '/geoserver/wms', layers: 'forest:forest_stand',
          parameters: { service: 'WMS', version: '1.3.0', transparent: true, format: 'image/png', CRS: 'EPSG:4326' }
        })
      )
      wmsLayer.alpha = 0.6
    } catch (err) { console.warn('WMS 失败:', err) }

    await loadStandsPoints()
    resetView()
    bindEvents()

  } catch (error) {
    console.error('❌ 初始化失败:', error)
    loadError.value = error.message
  }
})

const loadStandsPoints = async () => {
  try {
    const stands = await request.get('/stands')
    if (!Array.isArray(stands) || stands.length === 0) return
    viewer.entities.removeAll()
    
    stands.map(stand => {
      const lon = stand.centerLon ?? stand.center_lon
      const lat = stand.centerLat ?? stand.center_lat
      const standId = stand.standId ?? stand.stand_id
      if (!lon || !lat || !standId) return null

      const color = Cesium.Color.fromCssColorString(SPECIES_COLORS[stand.dominantSpecies] || '#757575')
      // 根据蓄积量计算圆柱高度（假设每公顷50蓄积对应100米高，自己按需调比例）
      const volume = stand.volumePerHa || 10
      const height = Math.max(50, volume * 2) 

      const entityConfig = {
        id: String(standId),
        position: Cesium.Cartesian3.fromDegrees(parseFloat(lon), parseFloat(lat)),
        label: {
          text: stand.standName || stand.xiaoBanCode || '未知',
          font: '13px Microsoft YaHei', style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          outlineWidth: 2, outlineColor: Cesium.Color.BLACK, fillColor: Cesium.Color.WHITE,
          backgroundColor: new Cesium.Color(0.15, 0.15, 0.15, 0.8), showBackground: true,
          backgroundPadding: new Cesium.Cartesian2(8, 5), verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          pixelOffset: new Cesium.Cartesian2(0, -15), disableDepthTestDistance: Number.POSITIVE_INFINITY,
          scaleByDistance: new Cesium.NearFarScalar(1.0e3, 1.0, 8.0e4, 0.3),
          show: false
        },
        properties: { 
          ...stand,
          _height: height // 把高度存起来，切换模式时用
        }
      }

      // ==================== 3. 动态形状（点 vs 3D柱） ====================
      if (is3DView.value) {
        entityConfig.cylinder = {
          length: height, // 高度
          topRadius: 40.0, // 顶面半径（米）
          bottomRadius: 40.0, // 底面半径
          material: color.withAlpha(0.8),
          outline: true,
          outlineColor: Cesium.Color.WHITE.withAlpha(0.5),
          outlineWidth: 1,
          slices: 12 // 边数，越小性能越好，12足够了
        }
      } else {
        entityConfig.point = { 
          pixelSize: 10, color, outlineColor: Cesium.Color.WHITE, outlineWidth: 2, 
          disableDepthTestDistance: Number.POSITIVE_INFINITY 
        }
      }

      return viewer.entities.add(entityConfig)
    }).filter(Boolean)
    
    // 强制刷新一帧
    viewer.scene.requestRender()
  } catch (error) { console.error('❌ 加载失败:', error.message) }
}

// ==================== 交互事件绑定 ====================
const bindEvents = () => {
  handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

  handler.setInputAction((movement) => {
    // 如果正在测距，不处理悬停
    if (isMeasuring.value) {
      cesiumContainer.value.style.cursor = 'crosshair'
      return
    }
    const pickedObject = viewer.scene.pick(movement.endPosition)
    if (hoveredEntity) { hoveredEntity.label.show = false; hoveredEntity = null }
    if (Cesium.defined(pickedObject) && Cesium.defined(pickedObject.id) && pickedObject.id.label) {
      hoveredEntity = pickedObject.id
      hoveredEntity.label.show = true
      cesiumContainer.value.style.cursor = 'pointer'
    } else {
      cesiumContainer.value.style.cursor = 'default'
    }
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

  handler.setInputAction((click) => {
    // ==================== 4. 测距逻辑 ====================
    if (isMeasuring.value) {
      const cartesian = viewer.scene.pickPosition(click.position)
      if (Cesium.defined(cartesian)) {
        measurePoints.push(cartesian)
        // 画点
        viewer.entities.add({ position: cartesian, point: { pixelSize: 6, color: Cesium.Color.RED, disableDepthTestDistance: Number.POSITIVE_INFINITY } })
        
        if (measurePoints.length > 1) {
          const prevPoint = measurePoints[measurePoints.length - 2]
          const lineEntity = viewer.entities.add({
            polyline: { positions: [prevPoint, cartesian], width: 3, material: new Cesium.PolylineDashMaterialProperty({ color: Cesium.Color.YELLOW }), clampToGround: true }
          })
          measureEntities.push(lineEntity)
          // 计算距离
          const distance = Cesium.Cartesian3.distance(prevPoint, cartesian)
          measureDistance.value = Math.round(measureDistance.value + distance)
        }
      }
      return
    }

    // 正常点击逻辑
    const pickedObject = viewer.scene.pick(click.position)
    if (Cesium.defined(pickedObject) && Cesium.defined(pickedObject.id)) {
      const entity = pickedObject.id
      const standData = entity.properties?.getValue?.() || entity.properties
      if (standData && standData.standId) {
        selectedStand.value = standData
        if(entity.label) entity.label.show = true
        // 飞行时设置偏移量，确保能看到3D圆柱的全貌
        viewer.flyTo(entity, { duration: 1.5, offset: new Cesium.HeadingPitchRange(0, Cesium.Math.toRadians(-30), 1500) })
      }
    } else { clearSelection() }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
}

// ==================== 工具栏功能 ====================
const toggleViewMode = () => {
  is3DView.value = !is3DView.value
  loadStandsPoints() // 重新加载实体以切换形态
}

const toggleMeasure = () => {
  isMeasuring.value = !isMeasuring.value
  if (!isMeasuring.value) {
    // 结束测距，清理画线
    measurePoints = []
    measureEntities.forEach(e => viewer.entities.remove(e))
    measureEntities = []
    measureDistance.value = 0
    viewer.scene.requestRender()
  }
}

const clearSelection = () => {
  if (hoveredEntity) { hoveredEntity.label.show = false; hoveredEntity = null }
  selectedStand.value = null
}

const resetView = () => {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(118.7, 32.1, 5000), // 稍微拉高一点看3D柱
    orientation: { heading: 0, pitch: Cesium.Math.toRadians(-45), roll: 0 }, duration: 2
  })
}

const goTo2D = () => {
  emit('back-to-2d', selectedStand.value ? {
    standId: selectedStand.value.standId, lon: selectedStand.value.centerLon, lat: selectedStand.value.centerLat,
    standName: selectedStand.value.standName, xiaoBanCode: selectedStand.value.xiaoBanCode,
    dominantSpecies: selectedStand.value.dominantSpecies, origin: selectedStand.value.origin,
    areaHa: selectedStand.value.areaHa, volumePerHa: selectedStand.value.volumePerHa,
    standAge: selectedStand.value.standAge, canopyDensity: selectedStand.value.canopyDensity
  } : null)
}

onUnmounted(() => {
  if (handler) handler.destroy()
  if (viewer) { viewer.destroy(); viewer = null }
})
</script>

<style scoped>
.cesium-container { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 99999; background-color: #1a1a1a; }
:deep(.cesium-viewer-bottom) { display: none !important; }
.control-panel { position: absolute; top: 20px; left: 20px; z-index: 100000; display: flex; gap: 8px; flex-wrap: wrap; }
.info-card { position: absolute; top: 20px; right: 20px; width: 280px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); z-index: 100000; overflow: hidden; }
.info-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f1f8e9; color: #2E7D32; font-weight: bold; border-bottom: 1px solid #ebeef5; }
.info-body { padding: 16px; font-size: 13px; color: #606266; }
.info-body p { margin: 8px 0; }
.error-overlay { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); text-align: center; z-index: 100001; max-width: 400px; }
.error-overlay p { color: #F56C6C; margin-bottom: 15px; }
/* 测距提示框 */
.measure-tip { position: absolute; top: 70px; left: 50%; transform: translateX(-50%); background: rgba(0,0,0,0.75); color: #fff; padding: 8px 20px; border-radius: 20px; font-size: 14px; z-index: 100000; pointer-events: none; }
.measure-tip strong { color: #67C23A; font-size: 16px; margin-left: 5px; }
</style>
