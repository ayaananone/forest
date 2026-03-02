<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '✏️ 编辑林分' : '➕ 新增林分'"
    size="500px"
    :close-on-click-modal="false"
    class="stand-edit-drawer"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      size="default"
      class="edit-form"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-form-item label="林分编号" prop="xiaoBanCode">
        <el-input 
          v-model="formData.xiaoBanCode" 
          placeholder="如：01-05"
          :disabled="isEdit"
        >
          <template #prefix>
            <el-icon><Document /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="林分名称" prop="standName">
        <el-input v-model="formData.standName" placeholder="输入林分名称" />
      </el-form-item>

      <el-form-item label="优势树种" prop="dominantSpecies">
        <el-select 
          v-model="formData.dominantSpecies" 
          placeholder="选择树种"
          style="width: 100%"
          filterable
          allow-create
        >
          <el-option
            v-for="species in speciesOptions"
            :key="species"
            :label="species"
            :value="species"
          >
            <span style="display: flex; align-items: center; gap: 8px;">
              <span 
                class="color-dot" 
                :style="{ backgroundColor: getSpeciesColor(species) }"
              />
              {{ species }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="起源类型" prop="origin">
        <el-radio-group v-model="formData.origin">
          <el-radio-button label="天然">🌲 天然林</el-radio-button>
          <el-radio-button label="人工">🌱 人工林</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 面积与蓄积 -->
      <el-divider content-position="left">面积与蓄积</el-divider>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="面积" prop="area">
            <el-input-number 
              v-model="formData.area" 
              :min="0.01"
              :precision="2"
              :step="0.1"
              style="width: 100%"
              placeholder="公顷"
            >
              <template #append>ha</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每公顷蓄积" prop="volumePerHa">
            <el-input-number 
              v-model="formData.volumePerHa" 
              :min="0"
              :precision="2"
              :step="1"
              style="width: 100%"
              placeholder="m³/ha"
            >
              <template #append>m³</template>
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="总蓄积">
        <el-statistic 
          :value="totalVolume" 
          suffix=" m³"
          :precision="2"
          class="volume-statistic"
        />
      </el-form-item>

      <!-- 林分特征 -->
      <el-divider content-position="left">林分特征</el-divider>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="林龄" prop="standAge">
            <el-input-number 
              v-model="formData.standAge" 
              :min="1"
              :max="200"
              style="width: 100%"
              placeholder="年"
            >
              <template #append>年</template>
            </el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="郁闭度" prop="canopyDensity">
            <el-slider 
              v-model="formData.canopyDensity" 
              :max="1"
              :step="0.1"
              show-stops
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="立地等级" prop="siteClass">
        <el-rate
          v-model="formData.siteClass"
          :max="5"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          show-text
          :texts="['Ⅴ', 'Ⅳ', 'Ⅲ', 'Ⅱ', 'Ⅰ']"
        />
      </el-form-item>

      <!-- 空间信息 -->
      <el-divider content-position="left">
        <span class="location-title">
          空间信息
          <el-tag v-if="hasLocation" type="success" size="small">已定位</el-tag>
          <el-tag v-else type="warning" size="small">未定位</el-tag>
        </span>
      </el-divider>

      <el-form-item label="中心经度" prop="centerLon">
        <el-input-number 
          v-model="formData.centerLon" 
          :precision="6"
          style="width: 100%"
          placeholder="点击地图选择"
          @change="emitLocationChange"
        />
      </el-form-item>

      <el-form-item label="中心纬度" prop="centerLat">
        <el-input-number 
          v-model="formData.centerLat" 
          :precision="6"
          style="width: 100%"
          placeholder="点击地图选择"
          @change="emitLocationChange"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" plain @click="startMapSelection">
          <el-icon><MapLocation /></el-icon>
          {{ hasLocation ? '重新选点' : '地图选点' }}
        </el-button>
        <el-button v-if="hasLocation" @click="clearLocation">
          <el-icon><Delete /></el-icon>
          清除位置
        </el-button>
      </el-form-item>

      <!-- 备注 -->
      <el-divider content-position="left">备注信息</el-divider>

      <el-form-item label="备注" prop="remark">
        <el-input 
          v-model="formData.remark" 
          type="textarea"
          :rows="3"
          placeholder="输入备注信息..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 底部操作栏 -->
    <template #footer>
      <div class="drawer-footer">
        <el-popconfirm
          v-if="isEdit"
          title="确定要删除这个林分吗？此操作不可恢复！"
          confirm-button-text="删除"
          confirm-button-type="danger"
          cancel-button-text="取消"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button type="danger" plain :loading="submitting">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-popconfirm>
        
        <div class="right-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button 
            type="primary" 
            :loading="submitting"
            @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存修改' : '创建林分' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, MapLocation, Delete, Check } from '@element-plus/icons-vue'
import { SPECIES_COLORS } from '@/config'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  editData: {
    type: Object,
    default: null
  },
  speciesOptions: {
    type: Array,
    default: () => ['马尾松', '杉木', '樟树', '枫香', '木荷', '毛竹', '油茶', '未知']
  }
})

const emit = defineEmits([
  'update:modelValue',
  'submit',
  'delete',
  'location-change',
  'start-map-selection'
])

// 表单引用
const formRef = ref(null)
const submitting = ref(false)

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.editData?.id || !!props.editData?.xiaoBanCode)

const hasLocation = computed(() => {
  return formData.value.centerLon && formData.value.centerLat
})

const totalVolume = computed(() => {
  return (formData.value.area || 0) * (formData.value.volumePerHa || 0)
})

// 表单数据
const defaultForm = {
  xiaoBanCode: '',
  standName: '',
  dominantSpecies: '',
  origin: '天然',
  area: undefined,
  volumePerHa: undefined,
  standAge: undefined,
  canopyDensity: 0.7,
  siteClass: 3,
  centerLon: undefined,
  centerLat: undefined,
  remark: ''
}

const formData = ref({ ...defaultForm })

// 表单校验规则
const formRules = {
  xiaoBanCode: [
    { required: true, message: '请输入林分编号', trigger: 'blur' },
    { pattern: /^\d{2}-\d{2}$/, message: '格式如：01-05', trigger: 'blur' }
  ],
  standName: [
    { required: true, message: '请输入林分名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dominantSpecies: [
    { required: true, message: '请选择优势树种', trigger: 'change' }
  ],
  origin: [
    { required: true, message: '请选择起源类型', trigger: 'change' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '面积必须大于0', trigger: 'blur' }
  ],
  volumePerHa: [
    { required: true, message: '请输入每公顷蓄积', trigger: 'blur' }
  ]
}

// 监听编辑数据变化
watch(() => props.editData, (newVal) => {
  if (newVal) {
    formData.value = {
      ...defaultForm,
      ...newVal,
      // 确保数值类型正确
      area: newVal.area ? Number(newVal.area) : undefined,
      volumePerHa: newVal.volumePerHa ? Number(newVal.volumePerHa) : undefined,
      standAge: newVal.standAge ? Number(newVal.standAge) : undefined,
      canopyDensity: newVal.canopyDensity ? Number(newVal.canopyDensity) : 0.7,
      siteClass: newVal.siteClass ? Number(newVal.siteClass) : 3,
      centerLon: newVal.centerLon ? Number(newVal.centerLon) : undefined,
      centerLat: newVal.centerLat ? Number(newVal.centerLat) : undefined
    }
  } else {
    formData.value = { ...defaultForm }
  }
}, { immediate: true })

// 方法
const getSpeciesColor = (species) => {
  return SPECIES_COLORS[species] || '#909399'
}

const emitLocationChange = () => {
  if (hasLocation.value) {
    emit('location-change', {
      lon: formData.value.centerLon,
      lat: formData.value.centerLat
    })
  }
}

const startMapSelection = () => {
  emit('start-map-selection')
  ElMessage.info('请在地图上点击选择位置')
}

const clearLocation = () => {
  formData.value.centerLon = undefined
  formData.value.centerLat = undefined
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const submitData = {
      ...formData.value,
      totalVolume: totalVolume.value
    }
    
    emit('submit', submitData, isEdit.value)
    
  } catch (error) {
    console.error('表单校验失败:', error)
    ElMessage.warning('请检查表单填写是否正确')
  }
}

const handleDelete = () => {
  emit('delete', props.editData)
  visible.value = false
}

const handleCancel = () => {
  visible.value = false
}

const handleClosed = () => {
  formRef.value?.resetFields()
  submitting.value = false
}

// 外部调用：设置地图选点坐标
const setMapLocation = (lon, lat) => {
  formData.value.centerLon = Number(lon.toFixed(6))
  formData.value.centerLat = Number(lat.toFixed(6))
  ElMessage.success(`已设置位置：${lon.toFixed(4)}, ${lat.toFixed(4)}`)
}

defineExpose({
  setMapLocation
})
</script>

<style scoped>
.stand-edit-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(135deg, #f1f8e9 0%, #fff 100%);
}

.stand-edit-drawer :deep(.el-drawer__title) {
  font-size: 16px;
  font-weight: 600;
  color: #2E7D32;
}

.edit-form {
  padding: 20px;
}

.edit-form :deep(.el-divider__text) {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.location-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.volume-statistic :deep(.el-statistic__content) {
  color: #D32F2F;
  font-size: 20px;
  font-weight: bold;
}

.drawer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
}

.right-actions {
  display: flex;
  gap: 12px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>