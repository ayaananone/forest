// forest.js - 完整修复版

/**
 * 林场数据API
 */
import { CONFIG } from '@/config'

const BASE_URL = CONFIG.API_BASE

// ==================== 数据转换函数 ====================

/**
 * 将后端林分数据转换为前端标准格式
 */
function transformStand(backendData) {
    if (!backendData) return null
    
    return {
        id: backendData.standId,
        standNo: backendData.xiaoBanCode,
        standName: backendData.standName,
        area: backendData.areaHa,                    // 关键：areaHa -> area
        dominantSpecies: backendData.dominantSpecies,
        volumePerHa: backendData.volumePerHa,        // m³/ha
        totalVolume: backendData.totalVolume,        // m³ (后端已计算)
        centerLon: backendData.centerLon,
        centerLat: backendData.centerLat,
        age: backendData.standAge,
        density: backendData.canopyDensity,          // 郁闭度
        origin: backendData.origin                   // 起源
    }
}

/**
 * 批量转换林分数据
 */
function transformStands(backendDataArray) {
    if (!Array.isArray(backendDataArray)) {
        console.warn('transformStands: 输入不是数组', backendDataArray)
        return []
    }
    return backendDataArray.map(transformStand).filter(Boolean)
}

/**
 * 转换树种统计数据
 */
function transformSpeciesStats(backendDataArray) {
    if (!Array.isArray(backendDataArray)) return []
    
    const { SPECIES_COLORS } = CONFIG
    
    return backendDataArray.map((item, index) => {
        const colors = Object.values(SPECIES_COLORS || {})
        const color = SPECIES_COLORS?.[item.species] || colors[index % colors.length] || '#757575'
        
        return {
            species: item.species || '未知',
            count: item.standCount || item.count || 0,  // 修复：standCount -> count
            totalArea: item.totalArea || 0,
            totalVolume: item.totalVolume || 0,
            color: color
        }
    })
}

// ==================== API 方法 ====================

/**
 * 获取所有林分数据
 */
export async function fetchStands() {
    try {
        const response = await fetch(`${BASE_URL}/stands`)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = await response.json()
        console.log('fetchStands 原始数据:', data.length, '条')  // 调试
        const transformed = transformStands(data)
        console.log('fetchStands 转换后:', transformed.length, '条')  // 调试
        return transformed
    } catch (error) {
        console.error('获取林分数据失败:', error)
        throw error
    }
}

/**
 * 获取树种统计
 */
export async function fetchSpeciesStatistics() {
    try {
        const response = await fetch(`${BASE_URL}/stands/statistics/species`)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = await response.json()
        console.log('fetchSpeciesStatistics 原始数据:', data)  // 调试
        return transformSpeciesStats(data)
    } catch (error) {
        console.error('获取树种统计失败:', error)
        throw error
    }
}

/**
 * 半径查询附近林分
 */
export async function fetchNearbyStands(lon, lat, radiusMeters) {
    try {
        const response = await fetch(
            `${BASE_URL}/stands/nearby?lon=${lon}&lat=${lat}&radiusMeters=${radiusMeters}`
        )
        if (!response.ok) throw new Error('查询失败')
        const data = await response.json()
        console.log('fetchNearbyStands 原始数据:', data.length, '条')  // 调试
        return transformStands(data)
    } catch (error) {
        console.error('半径查询失败:', error)
        throw error
    }
}

/**
 * 查询单个林分详情
 */
export async function fetchStandDetail(standId) {
    try {
        const response = await fetch(`${BASE_URL}/stands/${standId}`)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = await response.json()
        return transformStand(data)
    } catch (error) {
        console.error('获取林分详情失败:', error)
        throw error
    }
}
/**
 * 获取小班内所有单木详细数据
 */
/*
export async function fetchStandTrees(standId) {
    try {
        const response = await fetch(`${BASE_URL}/stands/${standId}/trees`)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = await response.json()
        console.log('fetchStandTrees 原始数据:', data.length, '条')  // 调试
        return transformTrees(data)
    } catch (error) {
        console.error('获取单木数据失败:', error)
        throw error
    }
}*/

/**
 * 转换单木数据
 */
/*
function transformTrees(backendDataArray) {
    if (!Array.isArray(backendDataArray)) {
        console.warn('transformTrees: 输入不是数组', backendDataArray)
        return []
    }
    
    return backendDataArray.map((item, index) => ({
        id: item.id || item.treeId || index + 1,
        species: item.species || item.treeSpecies || '-',
        dbhAvg: item.dbhAvg || item.dbh_avg || 0,           // 胸径
        treeHeight: item.treeHeight || item.tree_height || 0, // 树高
        diameterHalfHeight: item.diameterHalfHeight || item.diameter_half_height || 0, // 半高直径
        q2: item.q2 || 0,
        f1: item.f1 || 0,
        basalArea: item.basalArea || item.basal_area || 0,   // 断面积
        volume: item.volume || 0,                            // 材积
        crownWidth: item.crownWidth || item.crown_width || 0  // 冠幅
    }))
}*/

/**
 * 导出小班详细数据为 CSV
 */
/*
export function exportStandTrees(xiaoBanCode, data) {
    if (!data || data.length === 0) {
        console.warn('没有数据可导出')
        return
    }
    
    const headers = [
        '序号', '树种', '胸径(cm)', '树高(m)', '半高直径(cm)', 
        'q2', 'f1', '断面积(m²)', '材积(m³)', '冠幅(m)'
    ]
    
    const rows = data.map((item, index) => [
        index + 1,
        item.species,
        item.dbhAvg,
        item.treeHeight,
        item.diameterHalfHeight,
        item.q2,
        item.f1,
        item.basalArea,
        item.volume,
        item.crownWidth
    ])
    
    const csvContent = [
        ['小班编号:', xiaoBanCode].join(','),
        ['导出时间:', new Date().toLocaleString()].join(','),
        ['总株数:', data.length + ' 株'].join(','),
        [],
        headers.join(','),
        ...rows.map(row => row.join(','))
    ].join('\n')
    
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.href = url
    link.download = `小班_${xiaoBanCode}_单木数据_${new Date().toLocaleDateString()}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
}
*/
/**
 * WFS查询林分几何（GeoServer）
 */
export async function fetchStandGeometry(standId) {
    try {
        // 注意：WFS使用数据库字段名 zone_id
        const url = `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&version=1.1.0&request=GetFeature&` +
            `typename=forest:forest_stand&outputFormat=application/json&` +
            `cql_filter=zone_id=${standId}`
        
        const response = await fetch(url)
        if (!response.ok) throw new Error('WFS查询失败')
        return response.json()
    } catch (error) {
        console.error('WFS查询失败:', error)
        throw error
    }
}

// ==================== 导出辅助函数 ====================

export { transformStand, transformStands }