<template>
  <div class="cesium-container" ref="cesiumContainer"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'

const cesiumContainer = ref(null)
let viewer = null

const loadTreePoints = (trees) => {
  const instances = trees.map(tree => {
    return {
      position: Cesium.Cartesian3.fromDegrees(tree.lon, tree.lat, tree.height),
      attributes: {
        color: Cesium.ColorGeometryInstanceAttribute.fromColor(
          getColorBySpecies(tree.species)
        )
      }
    }
  })
  
  viewer.scene.primitives.add(new Cesium.PointPrimitiveCollection({
    instances: instances,
    pointSize: 5
  }))
}

viewer.selectedEntityChanged.addEventListener((entity) => {
  if (entity && entity.properties) {
    const standId = entity.properties.standId.getValue()
    // 调用你现有的 API 获取详细数据
    fetchStandDetail(standId).then(data => {
      // 更新 Vue 组件状态，触发图表更新
      emit('select-stand', data)
    })
  }
})

onMounted(() => {
  // 初始化 Cesium Viewer
  viewer = new Cesium.Viewer(cesiumContainer.value, {
    // 基础控件配置
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    navigationHelpButton: false,
    fullscreenButton: false,
    
    // 地形服务（需要 Cesium Ion Token）
    terrain: Cesium.Terrain.fromWorldTerrain(),
    
    // 使用你的 GeoServer WMS 作为底图
    imageryProvider: new Cesium.WebMapServiceImageryProvider({
      url: '/geoserver/wms',  // 使用代理地址，避免跨域
      layers: 'forest:forest_stand',  // 你的林分图层
      parameters: {
        service: 'WMS',
        version: '1.1.1',
        request: 'GetMap',
        transparent: true,
        format: 'image/png',
        srs: 'EPSG:4326'
      }
    })
  })

  // 设置初始视角（根据你的实际区域调整坐标）
  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(118.7, 32.1, 15000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-45),
      roll: 0
    }
  })
})

onUnmounted(() => {
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
})
</script>

<style scoped>
.cesium-container {
  width: 100%;
  height: 100%;
  min-height: 600px;
}

/* 隐藏 Cesium 版权信息（可选） */
:deep(.cesium-viewer-bottom) {
  display: none;
}
</style>