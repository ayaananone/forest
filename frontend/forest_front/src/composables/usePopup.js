/**
 * 弹窗管理组合式函数 - 简单稳定版
 */
import { ref, nextTick } from 'vue'
import { Overlay } from 'ol'
import { fromLonLat } from 'ol/proj'
import { SPECIES_COLORS } from '@/config'
import { calculateDistance } from '@/utils/calculations'

export function usePopup(map) {
    const popupOverlay = ref(null)
    const popupContent = ref(null)
    const popupVisible = ref(false)

    // ==================== 初始化 ====================

    const initPopup = () => {
        if (!map.value) {
            console.warn('Map not available for popup init')
            return
        }

        // 使用 nextTick 确保 DOM 已渲染
        nextTick(() => {
            const popupElement = document.getElementById('popup')
            
            if (!popupElement) {
                console.warn('未找到弹窗元素 #popup，1秒后重试')
                setTimeout(initPopup, 1000)
                return
            }

            popupOverlay.value = new Overlay({
                element: popupElement,
                positioning: 'bottom-center',
                stopEvent: true,
                offset: [0, -15],
                autoPan: {
                    animation: {
                        duration: 250
                    }
                }
            })

            map.value.addOverlay(popupOverlay.value)
            console.log('✓ Popup overlay initialized')
        })
    }

    // ==================== 弹窗控制 ====================

    const showPopup = (data, coordinate) => {
        popupContent.value = data
        popupVisible.value = true

        nextTick(() => {
            if (popupOverlay.value) {
                popupOverlay.value.setPosition(coordinate)
            }
        })
    }

    const closePopup = () => {
        popupVisible.value = false
        if (popupOverlay.value) {
            popupOverlay.value.setPosition(undefined)
        }
        popupContent.value = null
    }

    const showLoading = (coordinate) => {
        showPopup({ type: 'loading', message: '查询中...' }, coordinate)
    }

    const showError = (message, coordinate) => {
        showPopup({ type: 'error', message }, coordinate)
        setTimeout(closePopup, 2000)
    }

    // ==================== 半径查询结果展示 ====================

    const showRadiusQueryResult = (stands, lon, lat, radius) => {
        const coordinate = fromLonLat([lon, lat])
        
        const totalVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0) * (s.area || 0), 0)

        const standsWithDistance = stands.map(stand => ({
            ...stand,
            distance: Math.round(calculateDistance(lon, lat, stand.centerLon, stand.centerLat))
        }))

        const result = {
            type: 'radius',
            stands: standsWithDistance,
            lon,
            lat,
            radius,
            totalVolume,
            count: stands.length
        }

        showPopup(result, coordinate)
    }

    // ==================== 辅助方法 ====================

    const getSpeciesColor = (species) => {
        return SPECIES_COLORS[species] || '#757575'
    }

    return {
        popupOverlay,
        popupContent,
        popupVisible,
        initPopup,
        showPopup,
        closePopup,
        showLoading,
        showError,
        showRadiusQueryResult,
        getSpeciesColor
    }
}