<template>
  <div class="stand-detail-page">
    <el-page-header @back="goHome" title="返回首页" />

    <div class="detail-content" v-if="stand">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <h2>{{ stand.standName || '未命名林分' }}</h2>
            <el-tag :color="getSpeciesColor(stand.domininantSpecies)" effect="dark">
              {{ stand.domininantSpecies || '未知' }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="林分ID">
            <span class="highlight">{{ stand.standId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="小班号">{{ stand.xiaoBanCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="起源类型">{{ stand.origin || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面积">
            <span class="highlight area">{{ stand.areaHa }} 公顷</span>
          </el-descriptions-item>
          <el-descriptions-item label="每公顷蓄积">
            <span class="highlight volume">{{ stand.volumePerHa }} m³/ha</span>
          </el-descriptions-item>
          <el-descriptions-item label="总蓄积">
            <span class="highlight total">{{ stand.totalVolume }} m³</span>
          </el-descriptions-item>
          <el-descriptions-item label="林龄">{{ stand.standAge || '-' }} 年</el-descriptions-item>
          <el-descriptions-item label="郁闭度">{{ stand.canopyDensity || '-' }}</el-descriptions-item>
          <el-descriptions-item label="平均胸径(cm)">{{ stand.avgDbh || '-' }}</el-descriptions-item>
          <el-descriptions-item label="平均树高(m)">{{ stand.avgHeight || '-' }}</el-descriptions-item>
          <el-descriptions-item label="海拔(m)">{{ stand.elevation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="坡度">{{ stand.slope || '-' }} °</el-descriptions-item>
          <el-descriptions-item label="坡向">{{ stand.aspect || '-' }}</el-descriptions-item>
          <el-descriptions-item label="立地类型">{{ stand.siteType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调查日期">{{ stand.surveyDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调查员">{{ stand.surveyor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="中心经度">{{ stand.centerLon || '-' }}</el-descriptions-item>
          <el-descriptions-item label="中心纬度">{{ stand.centerLat || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ stand.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="action-buttons">
        <el-button type="primary" @click="handleDownload('csv')">CSV</el-button>
        <el-button type="success" @click="handleDownload('excel')">Excel</el-button>
        <el-button type="warning" @click="handleDownload('json')">JSON</el-button>
      </div>
    </div>

    <div v-else class="loading-state">
      <el-icon :size="48"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { fetchStandDetail, exportStandTrees } from '@/api/forest'
import { SPECIES_COLORS } from '@/config'

const route = useRoute()
const router = useRouter()
const stand = ref(null)

onMounted(async () => {
  const id = route.params.id
  if (!id) { ElMessage.error('无效ID'); return }
  try {
    const data = await fetchStandDetail(id)
    stand.value = data
  } catch (e) {
    ElMessage.error('加载失败')
  }
})

const goHome = () => router.push('/')
const getSpeciesColor = (s) => SPECIES_COLORS[s] || '#757575'
const handleDownload = async (fmt) => {
  try {
    await exportStandTrees(stand.value.standId, fmt)
    ElMessage.success('下载成功')
  } catch (e) {
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
.detail-content { margin-top: 16px; }
.info-card { margin-bottom: 16px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h2 { margin: 0; font-size: 18px; color: #2E7D32; }
.highlight { font-weight: 600; }
.area { color: #388E3C; }
.volume { color: #00796B; }
.total { color: #D32F2F; font-size: 18px; }
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
.loading-state {
  height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}
</style>