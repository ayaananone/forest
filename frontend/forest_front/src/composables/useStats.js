/**
 * 统计计算组合式函数
 */
import { ref, computed } from 'vue'
import { calculateStats, calculateSpeciesStats } from '@/utils/calculations'
import { formatNumber, formatArea, formatVolume } from '@/utils/formatters'

export function useStats() {
    const stands = ref([])
    const isLoading = ref(false)
    const error = ref(null)

    // ==================== 计算属性 ====================

    const stats = computed(() => calculateStats(stands.value))

    const speciesStats = computed(() => calculateSpeciesStats(stands.value))

    const statsCards = computed(() => [
        {
            id: 'totalStands',
            title: '小班总数',
            value: formatNumber(stats.value.totalStands),
            icon: 'Tree',
            color: '#2E7D32'
        },
        {
            id: 'totalArea',
            title: '总面积',
            value: formatArea(stats.value.totalArea),
            icon: 'MapLocation',
            color: '#388E3C'
        },
        {
            id: 'totalVolume',
            title: '总蓄积',
            value: formatVolume(stats.value.totalVolume),
            icon: 'TrendCharts',
            color: '#D32F2F'
        },
        {
            id: 'avgVolume',
            title: '平均蓄积',
            value: stats.value.avgVolume.toFixed(2) + ' m³/ha',
            icon: 'DataAnalysis',
            color: '#00796B'
        }
    ])

    const uniqueSpecies = computed(() => {
        const species = stands.value.map(s => s.dominantSpecies).filter(Boolean)
        return [...new Set(species)]
    })

    // 按起源统计
    const originStats = computed(() => {
        const origins = {}
        stands.value.forEach(stand => {
            const origin = stand.origin || '未知'
            if (!origins[origin]) {
                origins[origin] = { origin, count: 0, area: 0, volume: 0 }
            }
            origins[origin].count++
            origins[origin].area += (stand.area || 0)
            origins[origin].volume += (stand.volumePerHa || 0) * (stand.area || 0)
        })
        return Object.values(origins)
    })

    // 蓄积量区间分布
    const volumeDistribution = computed(() => {
        const ranges = [0, 0, 0, 0, 0] // <50, 50-100, 100-150, 150-200, >200
        stands.value.forEach(stand => {
            const volume = stand.volumePerHa || 0
            if (volume < 50) ranges[0]++
            else if (volume < 100) ranges[1]++
            else if (volume < 150) ranges[2]++
            else if (volume < 200) ranges[3]++
            else ranges[4]++
        })
        return {
            labels: ['<50', '50-100', '100-150', '150-200', '>200'],
            data: ranges
        }
    })

    // ==================== 方法 ====================

    const setStands = (data) => {
        stands.value = data || []
    }

    const updateStats = (data) => {
        stands.value = data || []
    }

    const addStand = (stand) => {
        stands.value.push(stand)
    }

    const removeStand = (standId) => {
        const index = stands.value.findIndex(s => s.id === standId)
        if (index > -1) {
            stands.value.splice(index, 1)
        }
    }

    const updateStand = (standId, updates) => {
        const index = stands.value.findIndex(s => s.id === standId)
        if (index > -1) {
            stands.value[index] = { ...stands.value[index], ...updates }
        }
    }

    const getStandById = (standId) => {
        return stands.value.find(s => s.id === standId)
    }

    const filterStands = (predicate) => {
        return stands.value.filter(predicate)
    }

    // ==================== 加载状态 ====================

    const setLoading = (loading) => {
        isLoading.value = loading
    }

    const setError = (err) => {
        error.value = err
    }

    const clearError = () => {
        error.value = null
    }

    return {
        stands,
        isLoading,
        error,
        stats,
        speciesStats,
        statsCards,
        uniqueSpecies,
        originStats,
        volumeDistribution,
        setStands,
        updateStats,
        addStand,
        removeStand,
        updateStand,
        getStandById,
        filterStands,
        setLoading,
        setError,
        clearError
    }
}