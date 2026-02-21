/**
 * 智慧林场综合管理平台 - 前端地图核心模块
 * 基于 OpenLayers 8.x + Bootstrap 5
 *
 * 功能：
 * 1. 地图初始化与图层管理
 * 2. WMS/WFS 图层加载
 * 3. 空间查询与筛选
 * 4. 统计图表联动
 * 5. 要素交互与弹窗
 */

// ==================== 全局配置 ====================
const CONFIG = {
    API_BASE: 'http://localhost:8081/api',
    GEOSERVER_URL: 'http://localhost:8081/geoserver',
    CENTER_LON: 118.7,
    CENTER_LAT: 32.1,
    DEFAULT_ZOOM: 12,
    MIN_ZOOM: 10,
    MAX_ZOOM: 18
};

// 树种颜色配置
const SPECIES_COLORS = {
    '马尾松': '#2E7D32',
    '杉木': '#388E3C',
    '湿地松': '#43A047',
    '枫香': '#D32F2F',
    '麻栎': '#795548',
    '香樟': '#00796B',
    '毛竹': '#689F38',
    '杂阔': '#757575'
};

// 全局变量
let map;
let standLayer;
let heatmapLayer;
let baseLayer;
let popupOverlay;
let radiusInteraction = null;
let speciesChart = null;
let currentFilter = {
    species: '',
    origin: '',
    minVolume: 0
};

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', function() {
    console.log('🌲 智慧林场GIS平台初始化...');
    initMap();
    initChart();
    loadAllData();
    setupEventListeners();
});

// ==================== 地图初始化 ====================
function initMap() {
    // 1. 底图图层 (OpenStreetMap)
    baseLayer = new ol.layer.Tile({
        source: new ol.source.OSM({
            url: 'https://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        }),
        name: 'base',
        visible: true
    });

    // 2. 林分WMS图层 (从Geoserver获取)
    standLayer = new ol.layer.Tile({
        source: new ol.source.TileWMS({
            url: CONFIG.GEOSERVER_URL + '/wms',
            params: {
                'LAYERS': 'forest:forest_stand',
                'TILED': true,
                'FORMAT': 'image/png',
                'TRANSPARENT': true,
                'VERSION': '1.1.1'
            },
            serverType: 'geoserver',
            crossOrigin: 'anonymous'
        }),
        name: 'stands',
        visible: true,
        opacity: 0.9
    });

    // 3. 热力图图层 (蓄积量)
    heatmapLayer = new ol.layer.Heatmap({
        source: new ol.source.Vector(),
        blur: 20,
        radius: 15,
        weight: function(feature) {
            const volume = feature.get('volume_per_ha') || 0;
            return Math.min(volume / 200, 1.0); // 归一化到0-1
        },
        name: 'heatmap',
        visible: false,
        gradient: ['#00f', '#0ff', '#0f0', '#ff0', '#f00']
    });

    // 4. 创建地图视图
    const view = new ol.View({
        center: ol.proj.fromLonLat([CONFIG.CENTER_LON, CONFIG.CENTER_LAT]),
        zoom: CONFIG.DEFAULT_ZOOM,
        minZoom: CONFIG.MIN_ZOOM,
        maxZoom: CONFIG.MAX_ZOOM,
        constrainRotation: false
    });

    // 5. 创建地图实例
    map = new ol.Map({
        target: 'map',
        layers: [baseLayer, heatmapLayer, standLayer],
        view: view,
        controls: ol.control.defaults.defaults().extend([
            new ol.control.ScaleLine({ units: 'metric' }),
            new ol.control.FullScreen(),
            new ol.control.ZoomSlider(),
            new ol.control.MousePosition({
                coordinateFormat: ol.coordinate.createStringXY(4),
                projection: 'EPSG:4326',
                className: 'custom-mouse-position'
            })
        ]),
        interactions: ol.interaction.defaults.defaults().extend([
            new ol.interaction.DragRotateAndZoom()
        ])
    });

    // 6. 初始化弹出框
    const popupElement = document.getElementById('popup');
    if (popupElement) {
        popupOverlay = new ol.Overlay({
            element: popupElement,
            positioning: 'bottom-center',
            stopEvent: false,
            offset: [0, -15]
        });
        map.addOverlay(popupOverlay);
    } else {
        console.warn('⚠️ 未找到弹出框元素 #popup');
    }

    // 7. 绑定地图事件
    map.on('click', handleMapClick);
    map.on('pointermove', handlePointerMove);

    console.log('✓ 地图初始化完成');
}

// ==================== 数据加载 ====================
async function loadAllData() {
    try {
        await Promise.all([
            loadStands(),
            loadStatistics(),
            initFilters()
        ]);
        console.log('✓ 所有数据加载完成');
    } catch (error) {
        console.error('❌ 数据加载失败:', error);
        showError('数据加载失败，请检查后端服务是否启动');
    }
}

// 加载林分数据
async function loadStands() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}/stands`);
        if (!response.ok) throw new Error('HTTP ' + response.status);
        const stands = await response.json();

        // 更新统计面板
        updateOverviewStats(stands);

        // 加载热力图数据
        loadHeatmapData(stands);

        // 填充筛选下拉框
        populateSpeciesFilter(stands);

        console.log(`✓ 加载了 ${stands.length} 个林分`);
        return stands;
    } catch (error) {
        console.error('加载林分数据失败:', error);
        throw error;
    }
}

// 加载统计数据
async function loadStatistics() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}/stands/statistics/species`);
        if (!response.ok) throw new Error('HTTP ' + response.status);
        const stats = await response.json();

        updateSpeciesChart(stats);
        generateLegend(stats);

        console.log('✓ 统计数据加载完成');
        return stats;
    } catch (error) {
        console.error('加载统计数据失败:', error);
        throw error;
    }
}

// 初始化筛选器
async function initFilters() {
    // 绑定筛选事件
    const speciesFilter = document.getElementById('speciesFilter');
    const originFilter = document.getElementById('originFilter');
    const radiusRange = document.getElementById('radiusRange');

    if (speciesFilter) {
        speciesFilter.addEventListener('change', filterBySpecies);
    }
    if (originFilter) {
        originFilter.addEventListener('change', filterByOrigin);
    }
    if (radiusRange) {
        radiusRange.addEventListener('input', updateRadiusDisplay);
    }
}

// ==================== 图层控制 ====================
function toggleLayer(layerName) {
    const layers = map.getLayers().getArray();
    const layer = layers.find(l => l.get('name') === layerName);

    if (layer) {
        const newVisible = !layer.getVisible();
        layer.setVisible(newVisible);
        console.log(`${layerName} 图层: ${newVisible ? '显示' : '隐藏'}`);

        // 如果显示热力图，确保数据已加载
        if (layerName === 'heatmap' && newVisible) {
            refreshHeatmap();
        }
    }
}

// 刷新热力图
function refreshHeatmap() {
    const source = heatmapLayer.getSource();
    if (source.getFeatures().length === 0) {
        loadStands().then(stands => loadHeatmapData(stands));
    }
}

// 加载热力图数据
function loadHeatmapData(stands) {
    const features = stands.map(stand => {
        const feature = new ol.Feature({
            geometry: new ol.geom.Point(ol.proj.fromLonLat([stand.centerLon, stand.centerLat])),
            volume_per_ha: stand.volumePerHa,
            stand_name: stand.standName,
            dominant_species: stand.dominantSpecies
        });
        return feature;
    });

    heatmapLayer.getSource().clear();
    heatmapLayer.getSource().addFeatures(features);
    console.log(`✓ 热力图加载了 ${features.length} 个点`);
}

// ==================== 地图交互 ====================
function handleMapClick(evt) {
    const coordinate = evt.coordinate;
    const lonLat = ol.proj.toLonLat(coordinate);
    const pixel = evt.pixel;

    // 检查是否处于半径查询模式
    if (radiusInteraction && radiusInteraction.active) {
        executeRadiusQuery(lonLat[0], lonLat[1]);
        return;
    }

    // 获取点击的要素
    const feature = map.forEachFeatureAtPixel(pixel, function(feature, layer) {
        return feature;
    });

    if (feature) {
        // 显示矢量要素信息
        showFeaturePopup(feature, coordinate);
    } else {
        // 查询WMS图层
        queryWMSFeatureInfo(pixel, coordinate);
    }
}

function handlePointerMove(evt) {
    const pixel = map.getEventPixel(evt.originalEvent);
    const hit = map.hasFeatureAtPixel(pixel);
    map.getTargetElement().style.cursor = hit ? 'pointer' : '';
}

// WMS GetFeatureInfo查询
async function queryWMSFeatureInfo(pixel, coordinate) {
    const viewResolution = map.getView().getResolution();
    const url = standLayer.getSource().getFeatureInfoUrl(
        coordinate, viewResolution, 'EPSG:3857',
        {
            'INFO_FORMAT': 'application/json',
            'FEATURE_COUNT': 10
        }
    );

    if (!url) return;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('WMS查询失败');
        const data = await response.json();

        if (data.features && data.features.length > 0) {
            const feature = data.features[0];
            showWMSPopup(feature, coordinate);
        } else {
            closePopup();
        }
    } catch (error) {
        console.error('WMS查询错误:', error);
    }
}

// ==================== 事件监听 ====================
function setupEventListeners() {
    // 图层切换按钮
    document.querySelectorAll('.layer-toggle').forEach(btn => {
        btn.addEventListener('click', function() {
            const layerName = this.dataset.layer;
            if (layerName) {
                toggleLayer(layerName);
                this.classList.toggle('active');
            }
        });
    });

    // 半径查询按钮
    const radiusBtn = document.getElementById('radiusQueryBtn');
    if (radiusBtn) {
        radiusBtn.addEventListener('click', toggleRadiusQuery);
    }

    // 重置筛选按钮
    const resetBtn = document.getElementById('resetFilterBtn');
    if (resetBtn) {
        resetBtn.addEventListener('click', resetFilters);
    }
}

// ==================== 筛选功能 ====================
function filterBySpecies(e) {
    currentFilter.species = e.target.value;
    applyFilters();
}

function filterByOrigin(e) {
    currentFilter.origin = e.target.value;
    applyFilters();
}

function updateRadiusDisplay(e) {
    const value = e.target.value;
    const display = document.getElementById('radiusValue');
    if (display) {
        display.textContent = value + 'm';
    }
}

function applyFilters() {
    // 构建CQL_FILTER
    const conditions = [];
    if (currentFilter.species) {
        conditions.push(`dominant_species='${currentFilter.species}'`);
    }
    if (currentFilter.origin) {
        conditions.push(`origin='${currentFilter.origin}'`);
    }
    if (currentFilter.minVolume > 0) {
        conditions.push(`volume_per_ha>=${currentFilter.minVolume}`);
    }

    const cqlFilter = conditions.length > 0 ? conditions.join(' AND ') : null;

    // 更新WMS图层参数
    const params = standLayer.getSource().getParams();
    if (cqlFilter) {
        params.CQL_FILTER = cqlFilter;
    } else {
        delete params.CQL_FILTER;
    }
    standLayer.getSource().updateParams(params);

    console.log('应用筛选:', cqlFilter || '无');
}

function resetFilters() {
    currentFilter = { species: '', origin: '', minVolume: 0 };

    // 重置UI
    const speciesFilter = document.getElementById('speciesFilter');
    const originFilter = document.getElementById('originFilter');
    if (speciesFilter) speciesFilter.value = '';
    if (originFilter) originFilter.value = '';

    applyFilters();
}

// ==================== 半径查询 ====================
function toggleRadiusQuery() {
    if (!radiusInteraction) {
        radiusInteraction = { active: false, radius: 1000 };
    }

    radiusInteraction.active = !radiusInteraction.active;
    const btn = document.getElementById('radiusQueryBtn');

    if (radiusInteraction.active) {
        btn.classList.add('active');
        btn.textContent = '退出半径查询';
        map.getTargetElement().style.cursor = 'crosshair';
        console.log('🔍 半径查询模式已激活');
    } else {
        btn.classList.remove('active');
        btn.textContent = '半径查询';
        map.getTargetElement().style.cursor = '';
        console.log('🔍 半径查询模式已关闭');
    }
}

function executeRadiusQuery(lon, lat) {
    const radius = parseInt(document.getElementById('radiusRange')?.value || 1000);
    console.log(`📍 执行半径查询: 中心点 [${lon.toFixed(4)}, ${lat.toFixed(4)}], 半径 ${radius}m`);

    // 这里可以添加具体的半径查询逻辑
    // 例如：发送请求到后端进行空间查询
}

// ==================== 弹出框功能 ====================
function showFeaturePopup(feature, coordinate) {
    if (!popupOverlay) return;

    const props = feature.getProperties();
    const content = document.getElementById('popup-content');
    const title = document.getElementById('popup-title');

    if (title) title.textContent = props.stand_name || '林分信息';
    if (content) {
        content.innerHTML = `
            <p><strong>优势树种:</strong> ${props.dominant_species || '未知'}</p>
            <p><strong>蓄积量:</strong> ${props.volume_per_ha || 0} m³/ha</p>
            <p><strong>面积:</strong> ${props.area || '未知'} ha</p>
        `;
    }

    popupOverlay.setPosition(coordinate);
}

function showWMSPopup(feature, coordinate) {
    if (!popupOverlay) return;

    const props = feature.properties || {};
    const content = document.getElementById('popup-content');
    const title = document.getElementById('popup-title');

    if (title) title.textContent = props.stand_name || '林分信息';
    if (content) {
        content.innerHTML = `
            <p><strong>优势树种:</strong> ${props.dominant_species || '未知'}</p>
            <p><strong>蓄积量:</strong> ${props.volume_per_ha || 0} m³/ha</p>
            <p><strong>林分号:</strong> ${props.stand_no || '未知'}</p>
        `;
    }

    popupOverlay.setPosition(coordinate);
}

function closePopup() {
    if (popupOverlay) {
        popupOverlay.setPosition(undefined);
    }
}

// ==================== 统计图表 ====================
function initChart() {
    // 初始化图表容器，实际图表库（如Chart.js）初始化在这里进行
    console.log('✓ 图表初始化完成');
}

function updateSpeciesChart(stats) {
    // 更新树种分布图表
    console.log('更新树种统计图表:', stats);
}

function generateLegend(stats) {
    // 生成图例
    const legendContainer = document.getElementById('speciesLegend');
    if (!legendContainer) return;

    legendContainer.innerHTML = '';
    Object.entries(SPECIES_COLORS).forEach(([species, color]) => {
        const item = document.createElement('div');
        item.className = 'legend-item';
        item.innerHTML = `
            <span class="legend-color" style="background-color: ${color}"></span>
            <span class="legend-text">${species}</span>
        `;
        legendContainer.appendChild(item);
    });
}

// ==================== 工具函数 ====================
function updateOverviewStats(stands) {
    const totalStands = stands.length;
    const totalVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0), 0);
    const avgVolume = totalStands > 0 ? totalVolume / totalStands : 0;

    // 更新DOM
    const totalEl = document.getElementById('totalStands');
    const volumeEl = document.getElementById('totalVolume');
    const avgEl = document.getElementById('avgVolume');

    if (totalEl) totalEl.textContent = totalStands;
    if (volumeEl) volumeEl.textContent = totalVolume.toFixed(2);
    if (avgEl) avgEl.textContent = avgVolume.toFixed(2);
}

function populateSpeciesFilter(stands) {
    const select = document.getElementById('speciesFilter');
    if (!select) return;

    // 获取唯一树种列表
    const species = [...new Set(stands.map(s => s.dominantSpecies).filter(Boolean))];

    // 保留第一个选项（"全部"）
    const firstOption = select.options[0];
    select.innerHTML = '';
    if (firstOption) select.appendChild(firstOption);

    // 添加树种选项
    species.forEach(s => {
        const option = document.createElement('option');
        option.value = s;
        option.textContent = s;
        select.appendChild(option);
    });
}

function showError(message) {
    console.error(message);
    // 可以添加UI错误提示
    const errorContainer = document.getElementById('errorContainer');
    if (errorContainer) {
        errorContainer.textContent = message;
        errorContainer.style.display = 'block';
        setTimeout(() => {
            errorContainer.style.display = 'none';
        }, 5000);
    }
}

// 导出模块（如果使用ES6模块）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        CONFIG,
        map,
        toggleLayer,
        refreshHeatmap,
        resetFilters
    };
}