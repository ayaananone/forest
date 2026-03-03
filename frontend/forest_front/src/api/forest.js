// /src/api/forest.js

/**
 * 林场数据API - 集成JWT认证
 */
import request from './request'
import { CONFIG } from '@/config'

const BASE_URL = CONFIG.API_BASE

// ==================== 数据转换函数 ====================

/**
 * 将后端林分数据转换为前端标准格式
 * 明确字段映射关系：
 * - standId: 数据库主键，用于所有API调用
 * - standNo: 业务编号（小班号），仅用于展示
 */
function transformStand(backendData) {
    if (!backendData) return null
    
    return {
        standId: backendData.standId,              // 数据库主键，用于API调用
        standNo: backendData.xiaoBanCode,          // 小班号，仅展示
        standName: backendData.standName,
        xiaoBanCode: backendData.xiaoBanCode,      // 林分编号
        area: backendData.areaHa,                   
        areaHa: backendData.areaHa,                // 兼容两种命名
        dominantSpecies: backendData.dominantSpecies,
        volumePerHa: backendData.volumePerHa,      // m³/ha
        totalVolume: backendData.totalVolume,      // m³ (后端已计算)
        centerLon: backendData.centerLon,
        centerLat: backendData.centerLat,
        age: backendData.standAge,
        standAge: backendData.standAge,            // 兼容两种命名
        density: backendData.canopyDensity,        // 郁闭度
        canopyDensity: backendData.canopyDensity,  // 兼容两种命名
        origin: backendData.origin,                // 起源
        siteType: backendData.siteType,            // 立地类型
        siteClass: backendData.siteClass,          // 立地等级
        avgHeight: backendData.avgHeight,          // 平均树高
        avgDbh: backendData.avgDbh,                // 平均胸径
        speciesComposition: backendData.speciesComposition, // 树种组成
        elevation: backendData.elevation,          // 海拔
        slope: backendData.slope,                  // 坡度
        aspect: backendData.aspect,                // 坡向
        surveyDate: backendData.surveyDate,        // 调查日期
        surveyor: backendData.surveyor,            // 调查员
        remark: backendData.remark,                // 备注
        createTime: backendData.createTime,
        updateTime: backendData.updateTime,
        createBy: backendData.createBy,
        updateBy: backendData.updateBy
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
            count: item.standCount || item.count || 0,  
            totalArea: item.totalArea || 0,
            totalVolume: item.totalVolume || 0,
            color: color
        }
    })
}

// ==================== API 方法（使用request，自动携带JWT）====================

/**
 * 获取所有林分数据
 */
export async function fetchStands() {
    try {
        const data = await request.get('/stands')
        console.log('fetchStands 原始数据:', data.length, '条')
        const transformed = transformStands(data)
        console.log('fetchStands 转换后:', transformed.length, '条')
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
        const data = await request.get('/stands/statistics/species')
        console.log('fetchSpeciesStatistics 原始数据:', data)
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
        const data = await request.get('/stands/nearby', {
            params: {
                lon: lon,
                lat: lat,
                radiusMeters: radiusMeters
            }
        })
        console.log('fetchNearbyStands 原始数据:', data.length, '条')
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
        const data = await request.get(`/stands/${standId}`)
        return transformStand(data)
    } catch (error) {
        console.error('获取林分详情失败:', error)
        throw error
    }
}

/**
 * 创建新林分（需要管理员权限）
 */
export async function createStand(standData) {
    try {
        console.log('createStand 提交数据:', standData)
        const data = await request.post('/stands', standData)
        console.log('createStand 返回数据:', data)
        return transformStand(data)
    } catch (error) {
        console.error('创建林分失败:', error)
        throw error
    }
}

/**
 * 更新林分信息（需要管理员权限）
 */
export async function updateStand(standId, standData) {
    try {
        console.log('updateStand 提交数据:', standId, standData)
        const data = await request.put(`/stands/${standId}`, standData)
        console.log('updateStand 返回数据:', data)
        return transformStand(data)
    } catch (error) {
        console.error('更新林分失败:', error)
        throw error
    }
}

/**
 * 删除林分（需要管理员权限）
 */
export async function deleteStand(standId) {
    try {
        console.log('deleteStand 删除林分:', standId)
        await request.delete(`/stands/${standId}`)
        console.log('deleteStand 删除成功')
        return true
    } catch (error) {
        console.error('删除林分失败:', error)
        throw error
    }
}

/**
 * 批量导入林分数据（需要管理员权限）
 */
export async function importStands(file) {
    try {
        const formData = new FormData()
        formData.append('file', file)
        
        const data = await request.post('/stands/import', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
        return data
    } catch (error) {
        console.error('导入林分数据失败:', error)
        throw error
    }
}

/**
 * 导出林分数据
 */
export async function exportStands(format = 'csv', filters = {}) {
    try {
        const response = await request.get('/stands/export', {
            params: {
                format: format,
                ...filters
            },
            responseType: 'blob'
        })
        
        downloadBlob(response, `林分数据_${new Date().toISOString().slice(0,10)}.${format}`)
        return true
    } catch (error) {
        console.error('导出林分数据失败:', error)
        throw error
    }
}

/**
 * 导出指定林分的单木数据 - 使用standId
 * @param {string|number} standId - 林分ID（数据库主键）
 * @param {string} format - 导出格式：csv、excel、json
 */
export async function exportStandTrees(standId, format = 'csv') {
    if (!standId) {
        throw new Error('林分ID不能为空')
    }
    
    const standIdStr = String(standId).trim()
    
    const response = await request.get('/trees/stand/export', {
        params: {
            standId: standIdStr,  // 明确使用standId参数
            format: format
        },
        responseType: 'blob'
    })
    
    // 从响应头获取文件名
    const contentDisposition = response.headers?.['content-disposition']
    let filename = `林分_${standIdStr}_单木数据.${format === 'excel' ? 'xlsx' : format}`
    
    if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/i)
        if (filenameMatch) {
            filename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''))
        }
    }
    
    downloadBlob(response.data || response, filename)
    return true
}

// ==================== 辅助工具函数 ====================

/**
 * 下载Blob文件
 */
function downloadBlob(blobData, filename) {
    const blob = blobData instanceof Blob ? blobData : new Blob([blobData])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
}

// ==================== GeoServer WFS/WMS 方法（无需JWT，直接访问）====================

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

/**
 * WMS GetFeatureInfo 查询
 */
export async function fetchWMSFeatureInfo(lon, lat, width = 101, height = 101) {
    try {
        const params = new URLSearchParams({
            SERVICE: 'WMS',
            VERSION: '1.1.1',
            REQUEST: 'GetFeatureInfo',
            LAYERS: 'forest:forest_stand',
            QUERY_LAYERS: 'forest:forest_stand',
            INFO_FORMAT: 'application/json',
            FEATURE_COUNT: 10,
            X: Math.floor(width / 2),
            Y: Math.floor(height / 2),
            SRS: 'EPSG:4326',
            WIDTH: width,
            HEIGHT: height,
            BBOX: `${lon - 0.001},${lat - 0.001},${lon + 0.001},${lat + 0.001}`
        })
        
        const url = `${CONFIG.GEOSERVER_URL}/wms?${params.toString()}`
        
        const response = await fetch(url)
        if (!response.ok) throw new Error('WMS查询失败')
        return response.json()
    } catch (error) {
        console.error('WMS GetFeatureInfo失败:', error)
        throw error
    }
}

/**
 * 计算两点间距离（米）
 */
export function calculateDistance(lon1, lat1, lon2, lat2) {
    const R = 6371000 // 地球半径（米）
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLon = (lon2 - lon1) * Math.PI / 180
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    return R * c
}

/**
 * 计算林分中心点
 */
export function calculateCentroid(coordinates) {
    if (!Array.isArray(coordinates) || coordinates.length === 0) {
        return null
    }
    
    let sumLon = 0
    let sumLat = 0
    let count = 0
    
    const flatten = (coords) => {
        if (typeof coords[0] === 'number') {
            sumLon += coords[0]
            sumLat += coords[1]
            count++
        } else {
            coords.forEach(flatten)
        }
    }
    
    flatten(coordinates)
    
    return count > 0 ? {
        lon: sumLon / count,
        lat: sumLat / count
    } : null
}

/**
 * 格式化蓄积量显示
 */
export function formatVolume(volume) {
    if (!volume || volume === 0) return '0 m³'
    if (volume >= 10000) {
        return (volume / 10000).toFixed(2) + ' 万m³'
    }
    if (volume >= 100000000) {
        return (volume / 100000000).toFixed(2) + ' 亿m³'
    }
    return volume.toFixed(2) + ' m³'
}

/**
 * 格式化面积显示
 */
export function formatArea(area) {
    if (!area || area === 0) return '0 ha'
    if (area >= 10000) {
        return (area / 10000).toFixed(2) + ' 万ha'
    }
    return area.toFixed(2) + ' ha'
}

// ==================== 导出 ====================

export { 
    transformStand, 
    transformStands,
    transformSpeciesStats 
}
