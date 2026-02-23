/**
 * 智慧林场 - 全局配置
 */

// ==================== 服务器配置 ====================

export const CONFIG = {
    // API基础地址
    API_BASE: '/api',
    
    // GeoServer地址
    GEOSERVER_URL: '/geoserver',
    
    // 地图默认中心点（南京）
    CENTER_LON: 118.8,
    CENTER_LAT: 32.07,
    
    // 默认缩放级别
    DEFAULT_ZOOM: 12,
    
    // 最小缩放级别
    MIN_ZOOM: 10,
    
    // 最大缩放级别
    MAX_ZOOM: 18,
    
    // WMS图层名称
    WMS_LAYER: 'forest:forest_stand',
    
    // WFS要素类型
    WFS_TYPENAME: 'forest:forest_stand',
    
    // 坐标系
    PROJECTION: 'EPSG:3857',
    
    // 数据投影
    DATA_PROJECTION: 'EPSG:4326'
}

// ==================== 树种颜色配置 ====================

export const SPECIES_COLORS = {
    '油茶': '#2E7D32',
    '杉木': '#388E3C',
    '樟树': '#43A047',
    '枫香': '#D32F2F',
    '马尾松': '#795548',
    '木荷': '#00796B',
    '毛竹': '#689F38',
    '杂阔': '#757575',
    '未知': '#9E9E9E'
}

// ==================== 起源颜色配置 ====================

export const ORIGIN_COLORS = {
    '人工': '#1976D2',
    '天然': '#388E3C',
    '飞播': '#F57C00',
    '萌生': '#7B1FA2',
    '未知': '#757575'
}

// ==================== 图层配置 ====================

export const LAYER_CONFIG = {
    stands: {
        name: '林分分布',
        type: 'wms',
        layerName: 'forest:forest_stand',
        visible: true,
        opacity: 0.9,
        zIndex: 10
    },
    heatmap: {
        name: '蓄积热力图',
        type: 'heatmap',
        visible: false,
        opacity: 0.8,
        zIndex: 5
    },
    base: {
        name: 'OpenStreetMap',
        type: 'base',
        visible: true,
        zIndex: 1
    },
    satellite: {
        name: '卫星影像',
        type: 'xyz',
        url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
        visible: false,
        zIndex: 2
    },
    terrain: {
        name: '地形图',
        type: 'xyz',
        url: 'https://{a-c}.tile.opentopomap.org/{z}/{x}/{y}.png',
        visible: false,
        zIndex: 3
    }
}

// ==================== 图表配置 ====================

export const CHART_CONFIG = {
    // 默认颜色主题
    colors: [
        '#2E7D32', '#388E3C', '#43A047', '#66BB6A',
        '#D32F2F', '#795548', '#00796B', '#689F38',
        '#757575', '#1976D2', '#F57C00', '#7B1FA2'
    ],
    
    // 饼图配置
    pie: {
        radius: ['40%', '70%'],
        center: ['50%', '50%']
    },
    
    // 柱状图配置
    bar: {
        barWidth: '60%'
    },
    
    // 折线图配置
    line: {
        smooth: true,
        symbolSize: 8
    }
}

// ==================== 地图交互配置 ====================

export const MAP_INTERACTION = {
    // 点击容差（像素）
    hitTolerance: 5,
    
    // 动画持续时间（毫秒）
    animationDuration: 500,
    
    // 最大查询要素数
    maxFeatures: 10,
    
    // 半径查询默认半径（米）
    defaultRadius: 1000,
    
    // 半径查询选项
    radiusOptions: [500, 1000, 2000, 3000, 5000]
}

// ==================== 业务规则配置 ====================

export const BUSINESS_RULES = {
    // 蓄积量等级划分
    volumeLevels: [
        { min: 0, max: 50, label: '低产林', color: '#FFEB3B' },
        { min: 50, max: 100, label: '中产林', color: '#8BC34A' },
        { min: 100, max: 150, label: '高产林', color: '#4CAF50' },
        { min: 150, max: 200, label: '丰产林', color: '#2E7D32' },
        { min: 200, max: Infinity, label: '特丰产林', color: '#1B5E20' }
    ],
    
    // 林龄等级划分
    ageLevels: [
        { min: 0, max: 10, label: '幼龄林' },
        { min: 11, max: 20, label: '中龄林' },
        { min: 21, max: 30, label: '近熟林' },
        { min: 31, max: 50, label: '成熟林' },
        { min: 51, max: Infinity, label: '过熟林' }
    ],
    
    // 郁闭度等级
    densityLevels: [
        { min: 0, max: 0.2, label: '疏林地' },
        { min: 0.2, max: 0.7, label: '中郁闭' },
        { min: 0.7, max: 1.0, label: '高郁闭' }
    ]
}

// ==================== UI配置 ====================

export const UI_CONFIG = {
    // 侧边栏宽度
    sidebarWidth: 320,
    
    // 图表高度
    chartHeight: 200,
    
    // 表格分页大小
    pageSize: 10,
    
    // 消息显示时长（毫秒）
    messageDuration: 3000,
    
    // 加载延迟（毫秒）
    loadingDelay: 200
}

// ==================== 导出所有配置 ====================

export default {
    CONFIG,
    SPECIES_COLORS,
    ORIGIN_COLORS,
    LAYER_CONFIG,
    CHART_CONFIG,
    MAP_INTERACTION,
    BUSINESS_RULES,
    UI_CONFIG
}