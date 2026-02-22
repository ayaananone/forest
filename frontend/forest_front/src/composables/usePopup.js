/**
 * 弹窗管理组合式函数
 */
import { ref, nextTick } from 'vue'
import { Overlay } from 'ol'
import { fromLonLat } from 'ol/proj'
import { SPECIES_COLORS } from '@/config'
import { calculateDistance } from '@/utils/calculations'
import { exportRadiusQueryResult } from '@/utils/export'

export function usePopup(map) {
    const popupOverlay = ref(null)
    const currentFeature = ref(null)
    const popupContent = ref(null)
    const popupPosition = ref(null)
    const popupVisible = ref(false)
    const popupType = ref('detail') // 'detail', 'list', 'loading', 'error', 'radius'

    // ==================== 初始化 ====================

    const initPopup = () => {
        if (!map.value) return

        nextTick(() => {
            const popupElement = document.getElementById('popup')
            if (!popupElement) {
                console.warn('未找到弹窗元素 #popup')
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
        })
    }

    // ==================== 弹窗控制 ====================

    const showPopup = (data, coordinate) => {
        popupContent.value = data
        popupPosition.value = coordinate
        popupType.value = data.type || 'detail'
        popupVisible.value = true

        if (popupOverlay.value) {
            popupOverlay.value.setPosition(coordinate)
        }

        currentFeature.value = data
    }

    const closePopup = () => {
        popupVisible.value = false
        if (popupOverlay.value) {
            popupOverlay.value.setPosition(undefined)
        }
        currentFeature.value = null
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
        const totalArea = stands.reduce((sum, s) => sum + (s.area || 0), 0)

        // 计算每个林分的距离
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
            totalArea,
            totalVolume,
            count: stands.length
        }

        showPopup(result, coordinate)
    }

    // ==================== 辅助方法 ====================

    const getSpeciesColor = (species) => {
        return SPECIES_COLORS[species] || '#757575'
    }

    const formatDistance = (lon1, lat1, lon2, lat2) => {
        return calculateDistance(lon1, lat1, lon2, lat2).toFixed(0)
    }

    const setPosition = (coordinate) => {
        if (popupOverlay.value) {
            popupOverlay.value.setPosition(coordinate)
        }
    }

    // ==================== 导出功能 ====================

    const exportQueryResult = () => {
        if (popupContent.value?.type === 'radius' && popupContent.value.stands) {
            exportRadiusQueryResult(popupContent.value.stands)
        }
    }

    return {
        popupOverlay,
        currentFeature,
        popupContent,
        popupPosition,
        popupVisible,
        popupType,
        initPopup,
        showPopup,
        closePopup,
        showLoading,
        showError,
        showRadiusQueryResult,
        exportQueryResult,
        getSpeciesColor,
        formatDistance,
        setPosition
    }
}