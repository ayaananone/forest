/**
 * 林场数据API
 */
import { CONFIG } from '@/config'

const BASE_URL = CONFIG.API_BASE

/**
 * 获取所有林分数据
 */
export async function fetchStands() {
    const response = await fetch(`${BASE_URL}/stands`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
}

/**
 * 获取树种统计
 */
export async function fetchSpeciesStatistics() {
    const response = await fetch(`${BASE_URL}/stands/statistics/species`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
}

/**
 * 半径查询
 */
export async function fetchNearbyStands(lon, lat, radiusMeters) {
    const response = await fetch(
        `${BASE_URL}/stands/nearby?lon=${lon}&lat=${lat}&radiusMeters=${radiusMeters}`
    )
    if (!response.ok) throw new Error('查询失败')
    return response.json()
}

/**
 * 查询单个林分详情
 */
export async function fetchStandDetail(standId) {
    const response = await fetch(`${BASE_URL}/stands/${standId}`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
}

/**
 * WFS查询林分几何
 */
export async function fetchStandGeometry(standId) {
    const url = `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&version=1.1.0&request=GetFeature&` +
        `typename=forest:forest_stand&outputFormat=application/json&` +
        `cql_filter=id=${standId}`
    
    const response = await fetch(url)
    if (!response.ok) throw new Error('WFS查询失败')
    return response.json()
}