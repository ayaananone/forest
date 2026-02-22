<template>
  <div 
    id="popup" 
    class="map-popup" 
    :class="{ 'popup-visible': visible }"
  >
    <!-- 详情弹窗 -->
    <div v-if="content?.type === 'detail'" class="popup-detail">
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
            {{ content?.id || '-' }}
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
          
          <el-descriptions-item label="林龄">
            {{ content?.age ? content.age + ' 年' : '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="郁闭度">
            {{ content?.density && content.density !== '-' ? content.density + '%' : '-' }}
          </el-descriptions-item>
          
          <el-descriptions-item label="面积">
            <span class="highlight-value">{{ formatArea(content?.area) }}</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="每公顷蓄积">
            <span class="volume-value">{{ content?.volume || 0 }} m³</span>
          </el-descriptions-item>
          
          <el-descriptions-item label="总蓄积估算">
            <span class="total-volume">
              {{ calculateTotalVolume(content?.volume, content?.area) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="popup-footer">
        <el-button type="primary" size="small" @click="handleShowDetail">
          <el-icon><Document /></el-icon> 详情
        </el-button>
        <el-button type="success" size="small" @click="handleZoomTo">
          <el-icon><ZoomIn /></el-icon> 定位
        </el-button>
      </div>
    </div>
    
    <!-- ... 其他弹窗类型 ... -->
  </div>
</template>

<script setup>
import { 
  View, 
  Close, 
  Document, 
  ZoomIn 
} from '@element-plus/icons-vue'
import { SPECIES_COLORS } from '@/config'
import { formatArea, formatVolume } from '@/utils/formatters'

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

const emit = defineEmits(['close', 'zoom-to', 'show-detail', 'select-stand'])

const handleClose = () => {
  emit('close')
}

const handleZoomTo = () => {
  emit('zoom-to', props.content?.id)
}

const handleShowDetail = () => {
  emit('show-detail', props.content?.id)
}

const getSpeciesColor = (species) => {
  return SPECIES_COLORS[species] || '#757575'
}

const calculateTotalVolume = (volume, area) => {
  if (!volume || !area) return '-'
  return formatVolume(volume * area)
}
</script>