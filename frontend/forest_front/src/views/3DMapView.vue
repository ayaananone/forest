<template>
  <div class="map-3d-view">
    <div class="map-sidebar">
      <h3>林分信息</h3>
      <!-- 选中林分的详情面板 -->
      <div v-if="selectedStand" class="stand-info">
        <p>林分名称: {{ selectedStand.name }}</p>
        <p>面积: {{ selectedStand.area }} ha</p>
        <p>蓄积量: {{ selectedStand.volume }} m³</p>
      </div>
    </div>
    <CesiumMap class="map-container" @select-stand="handleSelectStand" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CesiumMap from '@/components/cesium/CesiumMap.vue'

const selectedStand = ref(null)

const handleSelectStand = (stand) => {
  selectedStand.value = stand
}
</script>

<style scoped>
.map-3d-view {
  display: flex;
  height: 100vh;
}

.map-sidebar {
  width: 300px;
  padding: 20px;
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.map-container {
  flex: 1;
}
</style>