import { ref, onMounted, onUnmounted } from 'vue'

export function useMobile() {
  const isMobile = ref(false)
  const isTablet = ref(false)
  const viewport = ref({
    width: window.innerWidth,
    height: window.innerHeight
  })

  const checkDevice = () => {
    const width = window.innerWidth
    viewport.value = { width, height: window.innerHeight }
    isMobile.value = width <= 768
    isTablet.value = width > 768 && width <= 1024
  }

  onMounted(() => {
    checkDevice()
    window.addEventListener('resize', checkDevice)
    // 监听屏幕方向变化
    window.addEventListener('orientationchange', checkDevice)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', checkDevice)
    window.removeEventListener('orientationchange', checkDevice)
  })

  return {
    isMobile,
    isTablet,
    viewport
  }
}