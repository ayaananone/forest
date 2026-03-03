<template>
  <div class="stand-detail-page">
    <el-page-header @back="goBack" title="林分详情" />
    
    <div class="detail-content" v-if="stand">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <h2>{{ stand.standName || '未命名林分' }}</h2>
            <el-tag :color="getSpeciesColor(stand.dominantSpecies)" effect="dark">
              {{ stand.dominantSpecies || '未知' }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="林分ID">
            <span class="highlight">{{ stand.standId }}</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="小班号">
            {{ stand.standNo || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="起源类型">
            {{ stand.origin || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="林场总面积">
            <span class="highlight area">{{ stand.area }} 公顷</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="每公顷蓄积量">
            <span class="highlight volume">{{ stand.volumePerHa }} m³/ha</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="总蓄积量">
            <span class="highlight total">{{ stand.totalVolume }} m³</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="林龄">
            {{ stand.age ? stand.age + ' 年' : '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="郁闭度">
            {{ stand.density || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="中心经度">
            {{ stand.centerLon }}
          </el-descriptions-item>
          
          <el-descriptions-item label="中心纬度">
            {{ stand.centerLat }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 下载按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="handleDownload('csv')">
          <el-icon><Download /></el-icon>
          下载 CSV
        </el-button>
        <el-button type="success" size="large" @click="handleDownload('excel')">
          <el-icon><Grid /></el-icon>
          下载 Excel
        </el-button>
        <el-button type="warning" size="large" @click="handleDownload('json')">
          <el-icon><DataLine /></el-icon>
          下载 JSON
        </el-button>
      </div>
    </div>
    
    <div v-else class="loading-state">
      <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Grid, DataLine, Loading } from '@element-plus/icons-vue'
import { fetchStandDetail, exportStandTrees } from '@/api/forest'
import { SPECIES_COLORS } from '@/config'

const route = useRoute()
const router = useRouter()
const stand = ref(null)

onMounted(async () => {
  const standId = route.params.id
  if (!standId) {
    ElMessage.error('无效的林分ID')
    return
  }
  
  try {
    const data = await fetchStandDetail(standId)
    stand.value = data
  } catch (error) {
    ElMessage.error('加载林分详情失败')
  }
})

const goBack = () => {
  router.back()
}

const getSpeciesColor = (species) => {
  return SPECIES_COLORS[species] || '#757575'
}

const handleDownload = async (format) => {
  if (!stand.value?.standId) return
  
  try {
    ElMessage.info(`正在下载${format.toUpperCase()}格式...`)
    await exportStandTrees(stand.value.standId, format)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}
</script>

<style scoped>
.stand-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 16px;
}

.detail-content {
  margin-top: 16px;
}

.info-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #2E7D32;
}

.highlight {
  font-weight: 600;
  font-size: 16px;
}

.highlight.area {
  color: #388E3C;
}

.highlight.volume {
  color: #00796B;
}

.highlight.total {
  color: #D32F2F;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.action-buttons .el-button {
  width: 100%;
  justify-content: center;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
  color: #909399;
}

.loading-icon {
  animation: rotate 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

:deep(.el-page-header__title) {
  color: #2E7D32;
  font-weight: 600;
}
</style>