<template>
  <div 
    v-show="visible"
    id="popup" 
    class="map-popup"
  >
    <!-- 林场详情弹窗 -->
    <div v-if="content?.type === 'stand_detail'" class="popup-detail">
      <div class="popup-header">
        <h4>
          <el-icon color="#2E7D32"><View /></el-icon>
          {{ content?.name || '林分信息' }}
        </h4>
        <el-button type="danger" link size="small" @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="popup-body">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="林分编号">
            {{ content?.standNo || content?.id || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="优势树种">
            <el-tag 
              :color="getSpeciesColor(content?.species)" 
              effect="dark"
              size="small"
            >
              {{ content?.species || '未知' }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="起源类型">
            {{ content?.origin || '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="林场总面积">
            <span class="highlight-value area-value">{{ content?.area || 0 }} 公顷</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="每公顷蓄积量">
            <span class="highlight-value volume-value">{{ content?.volumePerHa || 0 }} m³/ha</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="总蓄积量">
            <span class="highlight-value total-volume">{{ content?.totalVolume || 0 }} m³</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="林龄">
            {{ content?.age ? content.age + ' 年' : '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="郁闭度">
            {{ content?.density && content.density !== '-' ? content.density : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 下载按钮组 - 支持 CSV、Excel、JSON 三种格式 -->
      <div class="popup-footer">
        <el-dropdown 
          split-button 
          type="success" 
          size="small" 
          @click="handleDownload('csv')"
          :disabled="!content?.id && !content?.standNo"
        >
          <el-icon><Download /></el-icon> 下载单木数据
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleDownload('csv')">
                <el-icon><Document /></el-icon> CSV 格式
              </el-dropdown-item>
              <el-dropdown-item @click="handleDownload('excel')">
                <el-icon><Grid /></el-icon> Excel 格式
              </el-dropdown-item>
              <el-dropdown-item @click="handleDownload('json')">
                <el-icon><DataLine /></el-icon> JSON 格式
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 加载中 -->
    <div v-else-if="content?.type === 'loading'" class="popup-loading">
      <el-icon class="loading-icon" :size="24"><Loading /></el-icon>
      <span>{{ content?.message || '查询中...' }}</span>
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="content?.type === 'error'" class="popup-error">
      <el-icon :size="24" color="#F56C6C"><CircleClose /></el-icon>
      <span>{{ content?.message || '查询失败' }}</span>
    </div>
    
    <!-- 半径查询结果 -->
    <div v-else-if="content?.type === 'radius'" class="popup-radius">
      <div class="popup-header">
        <h4>
          <el-icon color="#2E7D32"><Search /></el-icon>
          半径查询结果
        </h4>
        <el-button type="danger" link size="small" @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="popup-body">
        <div class="radius-info">
          <p>查询半径: <strong>{{ content?.radius }}m</strong></p>
          <p>找到林分: <strong>{{ content?.stands?.length || 0 }} 个</strong></p>
          <p>总蓄积量: <strong>{{ formatVolume(content?.totalVolume) }}</strong></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { 
  View, 
  Close, 
  Loading, 
  CircleClose, 
  Search,
  Download,
  Document,
  Grid,
  DataLine
} from '@element-plus/icons-vue'
import { SPECIES_COLORS } from '@/config'
import { ElMessage } from 'element-plus'

const props = defineProps({
  content: {
    type: Object,
    default: null
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'zoom-to'])

const handleClose = () => {
  emit('close')
}

/**
 * 处理下载按钮点击 - 支持 CSV、Excel、JSON 三种格式
 * @param {string} format - 导出格式：csv、excel、json
 */
const handleDownload = async (format = 'csv') => {
  // 获取林分ID，优先使用standNo（小班编码，如"02-05"），其次使用id
  const standId = props.content?.id
  
  if (!standId) {
    ElMessage.warning('无效的小班ID')
    return
  }
  
  // 确保转换为字符串并使用encodeURIComponent编码
  const standIdStr = String(standId).trim()
  
  try {
    ElMessage.info(`正在下载单木数据(${format.toUpperCase()}格式)...`)
    
    // 使用新的端点 /api/trees/stand/export
    const response = await fetch(
      `/api/trees/stand/export?standId=${encodeURIComponent(standIdStr)}&format=${format}`, 
      {
        method: 'GET',
        headers: {
          'Accept': '*/*'
        }
      }
    )
    
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('该小班暂无单木数据')
      }
      const errorText = await response.text()
      throw new Error(`下载失败: ${errorText || response.statusText}`)
    }
    
    // 获取文件名
    const contentDisposition = response.headers.get('content-disposition')
    let filename = `小班_${standIdStr}_单木数据.${format === 'excel' ? 'xlsx' : format}`
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/i)
      if (filenameMatch) {
        filename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''))
      }
    }
    
    // 创建下载链接
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`小班 ${standIdStr} 的单木数据下载成功`)
    
  } catch (error) {
    console.error('下载单木数据失败:', error)
    ElMessage.error(error.message || '下载失败，请重试')
  }
}

const getSpeciesColor = (species) => {
  return SPECIES_COLORS[species] || '#757575'
}

// 简单的体积格式化函数
const formatVolume = (volume) => {
  if (!volume || volume === 0) return '0 m³'
  if (volume >= 10000) {
    return (volume / 10000).toFixed(2) + ' 万m³'
  }
  return volume.toFixed(2) + ' m³'
}
</script>

<style scoped>
.map-popup {
  position: absolute;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 280px;
  max-width: 320px;
  z-index: 1000;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(135deg, #f1f8e9 0%, #fff 100%);
  border-radius: 12px 12px 0 0;
}

.popup-header h4 {
  margin: 0;
  font-size: 15px;
  color: #2E7D32;
  display: flex;
  align-items: center;
  gap: 6px;
}

.popup-body {
  padding: 12px 16px;
  max-height: 400px;
  overflow-y: auto;
}

.popup-footer {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
  border-radius: 0 0 12px 12px;
  justify-content: center;
}

.highlight-value {
  font-weight: 600;
  font-size: 14px;
}

.area-value {
  color: #388E3C;
}

.volume-value {
  color: #00796B;
}

.total-volume {
  color: #D32F2F;
  font-size: 15px;
}

.popup-loading,
.popup-error {
  padding: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.popup-radius .radius-info {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
}

.popup-radius .radius-info p {
  margin: 6px 0;
  color: #606266;
}

.popup-radius .radius-info strong {
  color: #2E7D32;
  font-size: 14px;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>