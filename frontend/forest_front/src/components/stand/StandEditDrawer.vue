<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑林分' : '新增林分'"
    :size="isMobile ? '100%' : '680px'"
    :close-on-click-modal="false"
    class="stand-edit-drawer"
    :class="{ 'mobile-drawer': isMobile }"
    @closed="handleClosed"
  >
    <!-- 移动端顶部进度指示器 -->
    <div v-if="isMobile" class="mobile-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <span class="progress-text">{{ currentSection }}/{{ totalSections }}</span>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-position="isMobile ? 'top' : 'right'"
      :label-width="isMobile ? 'auto' : '110px'"
      size="default"
      class="edit-form"
      :class="{ 'mobile-form': isMobile }"
    >
      <!-- 基本信息 - 始终展开 -->
      <el-divider content-position="left">
        <span class="section-title">基本信息</span>
      </el-divider>

      <el-form-item label="分区ID" prop="zoneId">
        <el-input-number 
          v-model="formData.zoneId" 
          :min="1" 
          :precision="0" 
          style="width: 100%" 
          :size="isMobile ? 'large' : 'default'" 
          controls-position="right" 
        />
      </el-form-item>

      
      <div class="form-section">
        <el-form-item label="林分编号" prop="xiaoBanCode">
          <el-input 
            v-model="formData.xiaoBanCode" 
            placeholder="如：01-05"
            :disabled="isEdit"
            :size="isMobile ? 'large' : 'default'"
          />
        </el-form-item>

        <el-form-item label="林分名称" prop="standName">
          <el-input 
            v-model="formData.standName" 
            placeholder="输入林分名称"
            :size="isMobile ? 'large' : 'default'"
          />
        </el-form-item>

        <el-row :gutter="isMobile ? 0 : 16">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="优势树种" prop="dominantSpecies">
              <el-select 
                v-model="formData.dominantSpecies" 
                placeholder="选择树种"
                style="width: 100%"
                filterable
                allow-create
                :size="isMobile ? 'large' : 'default'"
              >
                <el-option
                  v-for="species in speciesOptions"
                  :key="species"
                  :label="species"
                  :value="species"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="起源类型" prop="origin">
              <el-radio-group v-model="formData.origin" class="origin-radio">
                <el-radio-button value="天然">天然林</el-radio-button>
                <el-radio-button value="人工">人工林</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 面积与蓄积 - 可折叠 -->
      <el-collapse v-model="activeSections" class="mobile-collapse">
        <el-collapse-item name="area">
          <template #title>
            <span class="section-title">面积与蓄积</span>
            <el-tag v-if="hasAreaData" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="面积 (ha)" prop="areaHa" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.areaHa" 
                    :min="0.01"
                    :precision="2"
                    :step="0.1"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    placeholder="0.00"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="每公顷蓄积 (m³)" prop="volumePerHa" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.volumePerHa" 
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    placeholder="0.00"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="isMobile ? 0 : 16" style="margin-top: 8px;">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="总蓄积" class="compact-label compact-volume">
                  <div class="total-volume-display">
                    <span class="volume-value">{{ totalVolume.toFixed(2) }}</span>
                    <span class="volume-unit">m³</span>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <!-- 林分特征 - 可折叠 -->
        <el-collapse-item name="feature">
          <template #title>
            <span class="section-title">林分特征</span>
            <el-tag v-if="hasFeatureData" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="林龄 (年)" prop="standAge" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.standAge" 
                    :min="1"
                    :max="200"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="郁闭度" prop="canopyDensity">
                  <el-slider 
                    v-model="formData.canopyDensity" 
                    :max="1"
                    :step="0.1"
                    show-stops
                    :marks="{0: '0', 0.5: '0.5', 1: '1'}"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="isMobile ? 0 : 16" style="margin-top: 8px;">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="立地等级" prop="siteClass">
                  <el-rate
                    v-model="formData.siteClass"
                    :max="5"
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                    show-text
                    :texts="['Ⅴ', 'Ⅳ', 'Ⅲ', 'Ⅱ', 'Ⅰ']"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="平均胸径(cm)" prop="avgDbh" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.avgDbh" 
                    :min="0"
                    :precision="1"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="平均树高(m)" prop="avgHeight" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.avgHeight" 
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="isMobile ? 0 : 16" style="margin-top: 8px;">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="海拔(m)" prop="elevation" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.elevation" 
                    :min="0"
                    :precision="0"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <!-- 地形因子 - 可折叠 -->
        <el-collapse-item name="terrain">
          <template #title>
            <span class="section-title">地形因子</span>
            <el-tag v-if="hasTerrainData" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="坡度(°)" prop="slope" class="compact-label wide-input">
                  <el-input-number 
                    v-model="formData.slope" 
                    :min="0"
                    :max="90"
                    :precision="2"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                  controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="坡向" prop="aspect">
                  <el-select 
                    v-model="formData.aspect" 
                    placeholder="选择坡向"
                    style="width: 100%"
                    clearable
                    :size="isMobile ? 'large' : 'default'"
                  >
                    <el-option label="北" value="北" />
                    <el-option label="东北" value="东北" />
                    <el-option label="东" value="东" />
                    <el-option label="东南" value="东南" />
                    <el-option label="南" value="南" />
                    <el-option label="西南" value="西南" />
                    <el-option label="西" value="西" />
                    <el-option label="西北" value="西北" />
                    <el-option label="无" value="无" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="立地类型" prop="siteType">
              <el-input 
                v-model="formData.siteType" 
                placeholder="如：低山黄壤、低山红壤"
                :size="isMobile ? 'large' : 'default'"
              />
            </el-form-item>
          </div>
        </el-collapse-item>

        <!-- 树种组成 - 可折叠 -->
        <el-collapse-item name="composition">
          <template #title>
            <span class="section-title">树种组成</span>
            <el-tag v-if="hasCompositionData" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <div class="composition-list">
              <div v-for="(item, index) in formData.speciesComposition" :key="index" class="composition-item" >
                <el-row :gutter="8">
                  <el-col :span="12">
                    <el-select v-model="item.species" placeholder="选择树种" style="width: 100%" :size="isMobile ? 'large' : 'default'" >
                      <el-option v-for="species in speciesOptions" :key="species" :label="species" :value="species" />
                    </el-select>
                  </el-col>
                  <el-col :span="8">
                    <el-input-number 
                      v-model="item.ratio" 
                      :min="0" 
                      :max="getMaxRatio(index)" 
                      :precision="2" 
                      :step="0.1" 
                      style="width: 100%" 
                      :size="isMobile ? 'large' : 'default'" 
                      placeholder="比例" 
                      controls-position="right" 
                    />
                  </el-col>
                  <el-col :span="4">
                    <el-button type="danger" link> <el-icon><Delete /></el-icon> </el-button>
                  </el-col>
                </el-row>
              </div>

              <el-button 
                type="primary" 
                link 
                :size="isMobile ? 'large' : 'default'"
                @click="addCompositionItem"
                class="add-composition-btn"
              >
                <el-icon><Plus /></el-icon> 添加树种
              </el-button>
            </div>
          </div>
        </el-collapse-item>

        <!-- 调查信息 - 可折叠 -->
        <el-collapse-item name="survey">
          <template #title>
            <span class="section-title">调查信息</span>
            <el-tag v-if="hasSurveyData" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="调查日期" prop="surveyDate">
                  <el-date-picker
                    v-model="formData.surveyDate"
                    type="date"
                    placeholder="选择日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :size="isMobile ? 'large' : 'default'"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="调查员" prop="surveyor">
                  <el-input 
                    v-model="formData.surveyor" 
                    placeholder="调查员姓名"
                    :size="isMobile ? 'large' : 'default'"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-collapse-item>

        <!-- 空间信息 - 可折叠 -->
        <el-collapse-item name="location">
          <template #title>
            <span class="section-title">空间信息</span>
            <el-tag v-if="hasLocation" type="success" size="small" class="section-tag">已定位</el-tag>
          </template>
          
          <div class="form-section">
            <el-row :gutter="isMobile ? 0 : 16">
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="中心经度" prop="centerLon">
                  <el-input-number 
                    v-model="formData.centerLon" 
                    :precision="6"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                  controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="isMobile ? 24 : 12">
                <el-form-item label="中心纬度" prop="centerLat">
                  <el-input-number 
                    v-model="formData.centerLat" 
                    :precision="6"
                    style="width: 100%"
                    :size="isMobile ? 'large' : 'default'"
                  controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button 
                type="primary" 
                plain 
                :size="isMobile ? 'large' : 'default'"
                @click="startMapSelection"
              >
                <el-icon><MapLocation /></el-icon>
                {{ hasLocation ? '重新选点' : '地图选点' }}
              </el-button>
              <el-button 
                v-if="hasLocation" 
                :size="isMobile ? 'large' : 'default'"
                @click="clearLocation"
              >
                <el-icon><Delete /></el-icon>
                清除位置
              </el-button>
            </el-form-item>
          </div>
        </el-collapse-item>

        <!-- 备注信息 - 可折叠 -->
        <el-collapse-item name="remark">
          <template #title>
            <span class="section-title">备注信息</span>
            <el-tag v-if="formData.remark" type="success" size="small" class="section-tag">已填</el-tag>
          </template>
          
          <div class="form-section">
            <el-form-item label="备注" prop="remark">
              <el-input 
                v-model="formData.remark" 
                type="textarea"
                :rows="isMobile ? 4 : 3"
                maxlength="500"
                show-word-limit
                :size="isMobile ? 'large' : 'default'"
              />
            </el-form-item>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-form>

    <!-- 底部操作栏 - 移动端固定底部 -->
    <template #footer>
      <div class="drawer-footer" :class="{ 'mobile-footer': isMobile }">
        <el-popconfirm
          v-if="isEdit"
          title="确定要删除这个林分吗？"
          confirm-button-text="删除"
          confirm-button-type="danger"
          cancel-button-text="取消"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button 
              type="danger" 
              plain 
              :loading="submitting"
              :size="isMobile ? 'large' : 'default'"
            >
              <el-icon><Delete /></el-icon>
              <span v-if="!isMobile">删除</span>
            </el-button>
          </template>
        </el-popconfirm>
        
        <div class="right-actions">
          <el-button 
            @click="handleCancel"
            :size="isMobile ? 'large' : 'default'"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            :loading="submitting"
            @click="handleSubmit"
            :size="isMobile ? 'large' : 'default'"
            class="submit-btn"
          >
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, MapLocation, Delete, Check, Plus } from '@element-plus/icons-vue'
import { SPECIES_COLORS } from '@/config'

const props = defineProps({
  modelValue: Boolean,
  editData: Object,
  speciesOptions: {
    type: Array,
    default: () => ['马尾松', '杉木', '樟树', '枫香', '木荷', '毛竹', '油茶', '未知']
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'delete', 'location-change', 'start-map-selection'])

const formRef = ref(null)
const submitting = ref(false)

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768 || /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})
onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const activeSections = ref(['area', 'feature'])
const totalSections = 7

const currentSection = computed(() => {
  const sectionMap = { 'area': 1, 'feature': 2, 'terrain': 3, 'composition': 4, 'survey': 5, 'location': 6, 'remark': 7 }
  return sectionMap[activeSections.value[activeSections.value.length - 1]] || 1
})

const progressPercent = computed(() => (currentSection.value / totalSections) * 100)

const hasAreaData = computed(() => formData.value.areaHa && formData.value.volumePerHa)
const hasFeatureData = computed(() => formData.value.standAge || formData.value.avgDbh || formData.value.avgHeight)
const hasTerrainData = computed(() => formData.value.slope || formData.value.aspect || formData.value.siteType)
const hasCompositionData = computed(() => formData.value.speciesComposition && formData.value.speciesComposition.length > 0)
const hasSurveyData = computed(() => formData.value.surveyDate || formData.value.surveyor)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.editData?.standId)
const hasLocation = computed(() => formData.value.centerLon && formData.value.centerLat)
const totalVolume = computed(() => (formData.value.areaHa || 0) * (formData.value.volumePerHa || 0))

// 计算总和
const compositionRatioSum = computed(() => {
  if (!formData.value.speciesComposition || formData.value.speciesComposition.length === 0) return 0
  return formData.value.speciesComposition.reduce((sum, item) => sum + (Number(item.ratio) || 0), 0)
})

const getMaxRatio = (currentIndex) => {
  // 算出除了当前项之外，其他项的总和
  const otherSum = formData.value.speciesComposition.reduce((sum, item, index) => {
    return index === currentIndex ? sum : sum + (Number(item.ratio) || 0)
  }, 0)
  // 剩余空间 = 1 - 其他项总和，至少为 0
  return Math.max(0, +(1 - otherSum).toFixed(2))
}

const defaultForm = {
  zoneId: undefined,
  standId: null,
  xiaoBanCode: '',
  standName: '',
  dominantSpecies: '',
  origin: '天然',
  areaHa: undefined,
  volumePerHa: undefined,
  standAge: undefined,
  canopyDensity: 0.7,
  siteClass: 3,
  avgDbh: undefined,
  avgHeight: undefined,
  elevation: undefined,
  slope: undefined,
  aspect: '',
  siteType: '',
  speciesComposition: [],
  surveyDate: '',
  surveyor: '',
  centerLon: undefined,
  centerLat: undefined,
  remark: ''
}

const formData = ref({ ...defaultForm })

const formRules = {
  xiaoBanCode: [
    { required: true, message: '请输入林分编号', trigger: 'blur' },
    { pattern: /^\d{2}-\d{2}$/, message: '格式如：01-05', trigger: 'blur' }
  ],
  standName: [{ required: true, message: '请输入林分名称', trigger: 'blur' }],
  dominantSpecies: [{ required: true, message: '请选择优势树种', trigger: 'change' }],
  origin: [{ required: true, message: '请选择起源类型', trigger: 'change' }],
  areaHa: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  volumePerHa: [{ required: true, message: '请输入每公顷蓄积', trigger: 'blur' }],
  zoneId: [{ required: true, message: '请输入分区ID', trigger: 'blur' }]
}

// ==============================================
//赋值所有字段（立地等级 + 地形 + 调查 + 坐标 + 备注）
// ==============================================
watch(() => props.editData, (newVal, oldVal) => {
  console.log('=== 调试信息 ===')
  console.log('newVal:', newVal)
  console.log('oldVal:', oldVal)
  console.log('newVal?.standId:', newVal?.standId)

  // 明确判断：只有当有有效的 standId 时才加载编辑数据
  if (newVal && newVal.standId) {
    // 编辑模式：加载数据
    let compositionData = []
    if (newVal.speciesComposition) {
      if (Array.isArray(newVal.speciesComposition)) {
        compositionData = newVal.speciesComposition
      } else if (typeof newVal.speciesComposition === 'string') {
        try {
          compositionData = JSON.parse(newVal.speciesComposition)
        } catch (e) {
          console.warn('树种组成解析失败:', e)
          compositionData = []
        }
      }
    }

    formData.value = {
      ...defaultForm,
      zoneId: newVal.zoneId || 66,
      standId: newVal.standId,
      xiaoBanCode: newVal.xiaoBanCode || '',
      standName: newVal.standName || '',
      dominantSpecies: newVal.dominantSpecies || '',
      origin: newVal.origin || '天然',
      areaHa: newVal.areaHa !== null && newVal.areaHa !== undefined ? Number(newVal.areaHa) : undefined,
      volumePerHa: newVal.volumePerHa !== null && newVal.volumePerHa !== undefined ? Number(newVal.volumePerHa) : undefined,
      standAge: newVal.standAge !== null && newVal.standAge !== undefined ? Number(newVal.standAge) : undefined,
      canopyDensity: newVal.canopyDensity !== null && newVal.canopyDensity !== undefined ? Number(newVal.canopyDensity) : 0.7,
      siteClass: newVal.siteClass !== null && newVal.siteClass !== undefined ? Number(newVal.siteClass) : 3,
      avgDbh: newVal.avgDbh !== null && newVal.avgDbh !== undefined ? Number(newVal.avgDbh) : undefined,
      avgHeight: newVal.avgHeight !== null && newVal.avgHeight !== undefined ? Number(newVal.avgHeight) : undefined,
      elevation: newVal.elevation !== null && newVal.elevation !== undefined ? Number(newVal.elevation) : undefined,
      slope: newVal.slope !== null && newVal.slope !== undefined ? Number(newVal.slope) : undefined,
      aspect: newVal.aspect || '',
      siteType: newVal.siteType || '',
      speciesComposition: compositionData,
      surveyDate: newVal.surveyDate || '',
      surveyor: newVal.surveyor || '',
      centerLon: newVal.centerLon !== null && newVal.centerLon !== undefined ? Number(newVal.centerLon) : undefined,
      centerLat: newVal.centerLat !== null && newVal.centerLat !== undefined ? Number(newVal.centerLat) : undefined,
      remark: newVal.remark || ''
    }
    
    console.log('编辑数据加载:', formData.value)
    activeSections.value = ['area','feature','terrain','composition','survey','location','remark']
  } else {
    // 新增模式：强制重置为默认值
    console.log('新增模式：重置表单')
    formData.value = { ...defaultForm }
    activeSections.value = ['area', 'feature']
    // 重置表单验证状态
    formRef.value?.resetFields()
  }
}, { immediate: true, deep: true })

const addCompositionItem = () => {
  formData.value.speciesComposition.push({ species: '', ratio: 0 })
}
const removeCompositionItem = (index) => {
  formData.value.speciesComposition.splice(index, 1)
}
const startMapSelection = () => { emit('start-map-selection') }
const clearLocation = () => { formData.value.centerLon = undefined; formData.value.centerLat = undefined }

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(valid => {
    if (valid) {
      submitting.value = true
      
      const submitData = { 
        ...formData.value, 
        totalVolume: totalVolume.value,
        speciesComposition: formData.value.speciesComposition && formData.value.speciesComposition.length > 0 
          ? JSON.stringify(formData.value.speciesComposition) 
          : null
      }
      
      emit("submit", submitData, isEdit.value)
      submitting.value = false
    }
  })
}

const handleDelete = () => { emit('delete', props.editData); visible.value = false }

const handleCancel = () => { 
  visible.value = false
  // 延迟重置，确保抽屉关闭后再清理数据
  setTimeout(() => {
    formData.value = { ...defaultForm }
    activeSections.value = ['area', 'feature']
    formRef.value?.resetFields()
  }, 300)
}

const handleClosed = () => { 
  // 抽屉完全关闭后强制重置
  formData.value = { ...defaultForm }
  activeSections.value = ['area', 'feature']
  submitting.value = false
  formRef.value?.clearValidate()
}

const setMapLocation = (lon, lat) => {
  formData.value.centerLon = Number(lon.toFixed(6))
  formData.value.centerLat = Number(lat.toFixed(6))
}

defineExpose({ setMapLocation })
</script>

<style scoped>
/* 基础样式 */
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

.section-title {
  font-weight: 600;
  color: #2E7D32;
}

.section-tag {
  margin-left: 8px;
}

.form-section {
  padding: 0 8px;
}

.total-volume-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 8px 0;
}

.volume-value {
  font-size: 20px;
  font-weight: bold;
  color: #D32F2F;
}

.volume-unit {
  font-size: 14px;
  color: #606266;
}

.composition-list {
  padding: 8px 0;
}

.composition-item {
  margin-bottom: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.add-composition-btn {
  margin-top: 8px;
  width: 100%;
}

.origin-radio {
  display: flex;
  width: 100%;
}

.origin-radio :deep(.el-radio-button) {
  flex: 1;
}

.origin-radio :deep(.el-radio-button__inner) {
  width: 100%;
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

/* 移动端优化样式 */
.mobile-drawer :deep(.el-drawer__header) {
  padding: 12px 16px;
  position: sticky;
  top: 0;
  z-index: 10;
  background: linear-gradient(135deg, #f1f8e9 0%, #fff 100%);
}

.mobile-progress {
  position: sticky;
  top: 56px;
  z-index: 9;
  background: #fff;
  padding: 8px 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #67C23A, #2E7D32);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  min-width: 40px;
  text-align: right;
}

.mobile-form {
  padding: 12px 16px 80px 16px; /* 底部留出空间给固定按钮 */
}

.mobile-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.mobile-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  padding: 0;
  line-height: 1.5;
}

.mobile-form :deep(.el-input__inner),
.mobile-form :deep(.el-input-number__input) {
  height: 44px;
  font-size: 16px; /* 防止iOS缩放 */
}

.mobile-form :deep(.el-select) {
  width: 100%;
}

.mobile-form :deep(.el-slider__runway) {
  margin: 16px 0;
}

.mobile-collapse {
  border: none;
}

.mobile-collapse :deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 600;
  color: #2E7D32;
  padding: 12px 8px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.mobile-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 16px;
}

.mobile-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

.mobile-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 100;
}

.mobile-footer .submit-btn {
  flex: 1;
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .mobile-form :deep(.el-input__inner),
  .mobile-form :deep(.el-button),
  .mobile-form :deep(.el-select) {
    min-height: 44px;
  }
  
  .mobile-form :deep(.el-slider__button) {
    width: 24px;
    height: 24px;
  }
}

/* 小屏幕优化 */
@media (max-width: 375px) {
  .mobile-form {
    padding: 8px 12px 80px 12px;
  }
  
  .mobile-form :deep(.el-form-item__label) {
    font-size: 13px;
  }
}


/* 紧凑标签样式 - 减小标签字体 */
.compact-label :deep(.el-form-item__label) {
  font-size: 12px !important;
  padding-right: 4px;
}

/* 宽输入框样式 - 修复按钮遮挡 */
.wide-input :deep(.el-input-number) {
  width: 100%;
}

.wide-input :deep(.el-input-number .el-input__inner) {
  font-size: 13px;
  padding-left: 4px;
  padding-right: 28px;
}

/* 修复数字输入框按钮 */
.wide-input :deep(.el-input-number .el-input-number__decrease),
.wide-input :deep(.el-input-number .el-input-number__increase) {
  width: 22px;
}

/* 总蓄积紧凑样式 */
.compact-volume :deep(.el-form-item__label) {
  font-size: 11px !important;
}

.compact-volume .total-volume-display {
  padding: 2px 0;
  white-space: nowrap;
}

.compact-volume .volume-value {
  font-size: 14px;
  font-weight: 600;
  color: #D32F2F;
}

.compact-volume .volume-unit {
  font-size: 11px;
  color: #606266;
  margin-left: 2px;
}

/* 郁闭度和立地等级字体减小 */
:deep(.el-slider__marks-text) {
  font-size: 11px;
}

:deep(.el-rate__text) {
  font-size: 11px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .compact-label :deep(.el-form-item__label) {
    font-size: 12px !important;
  }

  .wide-input :deep(.el-input-number .el-input__inner) {
    padding-right: 32px;
  }

  .compact-volume .volume-value {
    font-size: 15px;
  }

  :deep(.el-slider__marks-text) {
    font-size: 10px;
  }
}

</style>