<template>
  <transition name="fade">
    <div v-if="visible" class="loading-mask" :style="maskStyle">
      <div class="loading-content">
        <el-icon class="loading-icon" :size="iconSize" :color="iconColor">
          <Loading />
        </el-icon>
        <p v-if="text" class="loading-text">{{ text }}</p>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: '加载中...'
  },
  iconSize: {
    type: Number,
    default: 48
  },
  iconColor: {
    type: String,
    default: '#2E7D32'
  },
  background: {
    type: String,
    default: 'rgba(255, 255, 255, 0.9)'
  },
  fullscreen: {
    type: Boolean,
    default: false
  },
  zIndex: {
    type: Number,
    default: 2000
  }
})

const maskStyle = computed(() => ({
  backgroundColor: props.background,
  position: props.fullscreen ? 'fixed' : 'absolute',
  zIndex: props.zIndex
}))
</script>

<style scoped>
.loading-mask {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.3s;
}

.loading-content {
  text-align: center;
}

.loading-icon {
  animation: rotate 1.5s linear infinite;
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>