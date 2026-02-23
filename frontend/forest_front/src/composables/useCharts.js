/**
 * 图表组合式函数
 */
import { ref, onUnmounted } from 'vue'
import Chart from 'chart.js/auto'
import { SPECIES_COLORS } from '@/config'
import { calculateVolumeRanges } from '@/utils/calculations'

// ==================== 树种分布饼图 ====================
export function useSpeciesChart() {
    const chartInstance = ref(null)
    const legendData = ref([])

    const initChart = (canvas) => {
        if (!canvas) return

        chartInstance.value = new Chart(canvas, {
            type: 'doughnut',
            data: {
                labels: [],
                datasets: [{
                    data: [],
                    backgroundColor: Object.values(SPECIES_COLORS),
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const label = context.label || ''
                                const value = context.parsed || 0
                                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
                                return `${label}: ${value}个小班 (${percentage}%)`
                            }
                        }
                    }
                },
                cutout: '60%'
            }
        })
    }

    const updateChart = (stats) => {
        if (!chartInstance.value) return

        // 防御性处理
        if (!stats || !Array.isArray(stats) || stats.length === 0) {
            chartInstance.value.data.labels = []
            chartInstance.value.data.datasets[0].data = []
            chartInstance.value.update()
            legendData.value = []
            return
        }

        const validStats = stats.filter(s => s && typeof s === 'object')
        const labels = validStats.map(s => s.species || s.dominant_species || '未知')
        const data = validStats.map(s => s.count || s.totalCount || 0)

        chartInstance.value.data.labels = labels
        chartInstance.value.data.datasets[0].data = data
        chartInstance.value.update()

        const total = data.reduce((a, b) => a + b, 0)
        legendData.value = validStats.map((s, index) => ({
            ...s,
            color: Object.values(SPECIES_COLORS)[index % Object.values(SPECIES_COLORS).length],
            percentage: total > 0 ? (((s.count || s.totalCount || 0) / total) * 100).toFixed(1) : 0
        }))
    }

    onUnmounted(() => {
        chartInstance.value?.destroy()
    })

    return { chartInstance, legendData, initChart, updateChart }
}

// ==================== 蓄积量分布柱状图 ====================
export function useVolumeChart() {
    const chartInstance = ref(null)

    const initChart = (canvas) => {
        if (!canvas) return

        chartInstance.value = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: ['<50', '50-100', '100-150', '150-200', '>200'],
                datasets: [{
                    label: '小班数量',
                    data: [0, 0, 0, 0, 0],
                    backgroundColor: 'rgba(46, 125, 50, 0.7)',
                    borderColor: '#2E7D32',
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    title: {
                        display: true,
                        text: '蓄积量分布 (m³/ha)',
                        font: { size: 14 }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        })
    }

    const updateChart = (stands) => {
        if (!chartInstance.value) return
        const ranges = calculateVolumeRanges(stands)
        chartInstance.value.data.datasets[0].data = ranges
        chartInstance.value.update()
    }

    onUnmounted(() => {
        chartInstance.value?.destroy()
    })

    return { chartInstance, initChart, updateChart }
}

// ==================== 生长趋势折线图 ====================
export function useTrendChart() {
    const chartInstance = ref(null)

    const initChart = (canvas) => {
        if (!canvas) return

        chartInstance.value = new Chart(canvas, {
            type: 'line',
            data: {
                labels: ['1年', '2年', '3年', '4年', '5年'],
                datasets: [{
                    label: '平均蓄积增长',
                    data: [0, 0, 0, 0, 0],
                    borderColor: '#388E3C',
                    backgroundColor: 'rgba(56, 142, 60, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: '#388E3C'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        })
    }

    const updateChart = (growthData) => {
        if (!chartInstance.value) return
        chartInstance.value.data.datasets[0].data = growthData
        chartInstance.value.update()
    }

    onUnmounted(() => {
        chartInstance.value?.destroy()
    })

    return { chartInstance, initChart, updateChart }
}

// ==================== 组合使用 ====================
export function useAllCharts() {
    const species = useSpeciesChart()
    const volume = useVolumeChart()
    const trend = useTrendChart()

    const initAll = (speciesCanvas, volumeCanvas, trendCanvas) => {
        species.initChart(speciesCanvas)
        volume.initChart(volumeCanvas)
        trend.initChart(trendCanvas)
    }

    const updateAll = (stats, stands, growthData) => {
        species.updateChart(stats)
        volume.updateChart(stands)
        trend.updateChart(growthData)
    }

    const destroyAll = () => {
        species.chartInstance.value?.destroy()
        volume.chartInstance.value?.destroy()
        trend.chartInstance.value?.destroy()
    }

    return {
        species,
        volume,
        trend,
        initAll,
        updateAll,
        destroyAll
    }
}