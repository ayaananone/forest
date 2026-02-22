/**
 * 格式化工具函数
 */

/**
 * 十六进制转RGBA
 * @param {string} hex - 十六进制颜色值 (#RRGGBB)
 * @param {number} alpha - 透明度 (0-1)
 * @returns {string} RGBA颜色值
 */
export function hexToRgba(hex, alpha) {
    const r = parseInt(hex.slice(1, 3), 16)
    const g = parseInt(hex.slice(3, 5), 16)
    const b = parseInt(hex.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/**
 * RGBA转十六进制
 * @param {string} rgba - RGBA颜色值
 * @returns {string} 十六进制颜色值
 */
export function rgbaToHex(rgba) {
    const parts = rgba.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/)
    if (!parts) return '#000000'
    
    const r = parseInt(parts[1]).toString(16).padStart(2, '0')
    const g = parseInt(parts[2]).toString(16).padStart(2, '0')
    const b = parseInt(parts[3]).toString(16).padStart(2, '0')
    
    return `#${r}${g}${b}`.toUpperCase()
}

/**
 * 格式化数字为千分位
 * @param {number} num - 数字
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的字符串
 */
export function formatNumber(num, decimals = 0) {
    if (num === undefined || num === null || isNaN(num)) return '-'
    return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
    })
}

/**
 * 格式化面积
 * @param {number} area - 面积值
 * @returns {string} 格式化后的面积字符串
 */
export function formatArea(area) {
    if (!area || isNaN(area)) return '-'
    if (area >= 10000) {
        return `${(area / 10000).toFixed(2)} 万ha`
    }
    return `${Number(area).toFixed(2)} ha`
}

/**
 * 格式化蓄积量
 * @param {number} volume - 蓄积量值
 * @returns {string} 格式化后的蓄积量字符串
 */
export function formatVolume(volume) {
    if (!volume || isNaN(volume)) return '-'
    if (volume >= 100000000) {
        return `${(volume / 100000000).toFixed(2)} 亿m³`
    }
    if (volume >= 10000) {
        return `${(volume / 10000).toFixed(2)} 万m³`
    }
    return `${Number(volume).toFixed(2)} m³`
}

/**
 * 格式化百分比
 * @param {number} value - 数值
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的百分比
 */
export function formatPercent(value, decimals = 1) {
    if (value === undefined || value === null || isNaN(value)) return '-'
    return `${Number(value).toFixed(decimals)}%`
}

/**
 * 格式化日期
 * @param {string|Date} date - 日期
 * @param {string} format - 格式
 * @returns {string} 格式化后的日期
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
    if (!date) return '-'
    
    const d = new Date(date)
    if (isNaN(d.getTime())) return '-'
    
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hour = String(d.getHours()).padStart(2, '0')
    const minute = String(d.getMinutes()).padStart(2, '0')
    const second = String(d.getSeconds()).padStart(2, '0')
    
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hour)
        .replace('mm', minute)
        .replace('ss', second)
}

/**
 * 格式化坐标
 * @param {number} coord - 坐标值
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的坐标
 */
export function formatCoordinate(coord, decimals = 6) {
    if (coord === undefined || coord === null || isNaN(coord)) return '-'
    return Number(coord).toFixed(decimals)
}

/**
 * 格式化经纬度坐标对
 * @param {number} lon - 经度
 * @param {number} lat - 纬度
 * @param {number} decimals - 小数位数
 * @returns {string} 格式化后的坐标字符串
 */
export function formatLatLon(lon, lat, decimals = 4) {
    return `${formatCoordinate(lon, decimals)}, ${formatCoordinate(lat, decimals)}`
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
    if (bytes === 0) return '0 B'
    
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    const k = 1024
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`
}

/**
 * 格式化时长
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时长
 */
export function formatDuration(seconds) {
    if (!seconds || seconds < 0) return '-'
    
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = Math.floor(seconds % 60)
    
    if (hours > 0) {
        return `${hours}小时${minutes}分钟`
    } else if (minutes > 0) {
        return `${minutes}分钟${secs}秒`
    } else {
        return `${secs}秒`
    }
}

/**
 * 格式化树种名称（添加图标）
 * @param {string} species - 树种名称
 * @returns {string} 带图标的树种名称
 */
export function formatSpecies(species) {
    const icons = {
        '马尾松': '🌲',
        '杉木': '🌲',
        '湿地松': '🌲',
        '枫香': '🍁',
        '麻栎': '🌳',
        '香樟': '🌿',
        '毛竹': '🎋',
        '杂阔': '🌳'
    }
    const icon = icons[species] || '🌲'
    return `${icon} ${species || '未知'}`
}

/**
 * 格式化起源类型
 * @param {string} origin - 起源
 * @returns {string} 格式化后的起源
 */
export function formatOrigin(origin) {
    const origins = {
        '人工': '👷 人工林',
        '天然': '🌲 天然林',
        '飞播': '✈️ 飞播林',
        '萌生': '🌱 萌生林'
    }
    return origins[origin] || origin || '-'
}

/**
 * 截断文本
 * @param {string} text - 文本
 * @param {number} length - 最大长度
 * @param {string} suffix - 后缀
 * @returns {string} 截断后的文本
 */
export function truncateText(text, length = 20, suffix = '...') {
    if (!text) return ''
    if (text.length <= length) return text
    return text.substring(0, length) + suffix
}

/**
 * 首字母大写
 * @param {string} str - 字符串
 * @returns {string} 首字母大写的字符串
 */
export function capitalize(str) {
    if (!str) return ''
    return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 转换为中文数字
 * @param {number} num - 数字
 * @returns {string} 中文数字
 */
export function toChineseNumber(num) {
    const chineseNums = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
    if (num <= 10) return chineseNums[num]
    return num.toString()
}