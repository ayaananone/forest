/**
 * 计算工具函数
 */

/**
 * 计算两点距离（米）- 哈弗辛公式
 * @param {number} lon1 - 起点经度
 * @param {number} lat1 - 起点纬度
 * @param {number} lon2 - 终点经度
 * @param {number} lat2 - 终点纬度
 * @returns {number} 距离（米）
 */
export function calculateDistance(lon1, lat1, lon2, lat2) {
    const R = 6371000
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLon = (lon2 - lon1) * Math.PI / 180
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}

/**
 * 计算蓄积量分布区间
 * @param {Array} stands - 林分数据数组
 * @returns {Array} 各区间的数量 [<50, 50-100, 100-150, 150-200, >200]
 */
export function calculateVolumeRanges(stands) {
    const ranges = [0, 0, 0, 0, 0]

    stands.forEach(stand => {
        const volume = stand.volumePerHa || 0
        if (volume < 50) ranges[0]++
        else if (volume < 100) ranges[1]++
        else if (volume < 150) ranges[2]++
        else if (volume < 200) ranges[3]++
        else ranges[4]++
    })

    return ranges
}

/**
 * 计算统计数据
 * @param {Array} stands - 林分数据数组
 * @returns {Object} 统计结果
 */
export function calculateStats(stands) {
    const totalStands = stands.length
    const totalArea = stands.reduce((sum, s) => sum + (s.area || 0), 0)
    const totalVolume = stands.reduce((sum, s) => sum + ((s.volumePerHa || 0) * (s.area || 0)), 0)
    const avgVolume = totalArea > 0 ? totalVolume / totalArea : 0

    return {
        totalStands,
        totalArea,
        totalVolume,
        avgVolume
    }
}

/**
 * 按树种统计
 * @param {Array} stands - 林分数据数组
 * @returns {Array} 树种统计数组
 */
export function calculateSpeciesStats(stands) {
    const stats = {}
    stands.forEach(stand => {
        const species = stand.dominantSpecies || '未知'
        if (!stats[species]) {
            stats[species] = { species, count: 0, totalArea: 0, totalVolume: 0 }
        }
        stats[species].count++
        stats[species].totalArea += (stand.area || 0)
        stats[species].totalVolume += (stand.volumePerHa || 0) * (stand.area || 0)
    })
    return Object.values(stats)
}

/**
 * 按起源统计
 * @param {Array} stands - 林分数据数组
 * @returns {Array} 起源统计数组
 */
export function calculateOriginStats(stands) {
    const stats = {}
    stands.forEach(stand => {
        const origin = stand.origin || '未知'
        if (!stats[origin]) {
            stats[origin] = { origin, count: 0, totalArea: 0 }
        }
        stats[origin].count++
        stats[origin].totalArea += (stand.area || 0)
    })
    return Object.values(stats)
}

/**
 * 计算林分年龄分布
 * @param {Array} stands - 林分数据数组
 * @returns {Array} 年龄区间统计
 */
export function calculateAgeDistribution(stands) {
    const ranges = {
        '幼龄林': { min: 0, max: 10, count: 0 },
        '中龄林': { min: 11, max: 20, count: 0 },
        '近熟林': { min: 21, max: 30, count: 0 },
        '成熟林': { min: 31, max: 50, count: 0 },
        '过熟林': { min: 51, max: Infinity, count: 0 }
    }

    stands.forEach(stand => {
        const age = stand.age || 0
        for (const range of Object.entries(ranges)) {
            if (age >= range.min && age <= range.max) {
                range.count++
                break
            }
        }
    })

    return Object.entries(ranges).map(([name, range]) => ({
        name,
        count: range.count
    }))
}

/**
 * 计算生长趋势数据
 * @param {Array} stands - 林分数据数组
 * @param {number} years - 年数
 * @returns {Array} 逐年蓄积量
 */
export function calculateGrowthTrend(stands, years = 5) {
    const avgVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0), 0) / (stands.length || 1)
    const growthRate = 0.05 // 假设年增长率5%

    const trend = []
    for (let i = 0; i < years; i++) {
        trend.push(Math.round(avgVolume * Math.pow(1 + growthRate, i)))
    }

    return trend
}

/**
 * 计算多边形中心点
 * @param {Array} coordinates - 多边形坐标数组
 * @returns {Object} {lon, lat} 中心点坐标
 */
export function calculateCentroid(coordinates) {
    let sumX = 0, sumY = 0, count = 0

    const flattenCoords = Array.isArray(coordinates[0][0]) ? coordinates.flat() : coordinates

    flattenCoords.forEach(coord => {
        sumX += coord[0]
        sumY += coord[1]
        count++
    })

    return {
        lon: sumX / count,
        lat: sumY / count
    }
}

/**
 * 计算多边形面积（简化版，适用于小范围）
 * @param {Array} coordinates - 多边形坐标数组
 * @returns {number} 面积（平方米）
 */
export function calculatePolygonArea(coordinates) {
    const coords = Array.isArray(coordinates[0][0]) ? coordinates[0] : coordinates
    let area = 0

    for (let i = 0; i < coords.length - 1; i++) {
        const [x1, y1] = coords[i]
        const [x2, y2] = coords[i + 1]
        area += (x1 * y2 - x2 * y1)
    }

    return Math.abs(area) / 2
}

/**
 * 格式化坐标
 * @param {number} value - 坐标值
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的坐标
 */
export function formatCoordinate(value, decimals = 4) {
    return Number(value).toFixed(decimals)
}

/**
 * 判断点是否在多边形内
 * @param {number} lon - 点经度
 * @param {number} lat - 点纬度
 * @param {Array} polygon - 多边形坐标数组
 * @returns {boolean} 是否在多边形内
 */
export function isPointInPolygon(lon, lat, polygon) {
    let inside = false
    const coords = Array.isArray(polygon[0][0]) ? polygon[0] : polygon

    for (let i = 0, j = coords.length - 1; i < coords.length; j = i++) {
        const [xi, yi] = coords[i]
        const [xj, yj] = coords[j]

        const intersect = ((yi > lat) !== (yj > lat)) &&
            (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi)

        if (intersect) inside = !inside
    }

    return inside
}