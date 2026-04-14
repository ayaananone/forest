/**
 * 地图核心组合式函数
 */
import { ref, onUnmounted, nextTick } from 'vue'
import { Map, View } from 'ol'
import { fromLonLat, toLonLat } from 'ol/proj'
import { defaults as defaultControls, ScaleLine, FullScreen, ZoomSlider, MousePosition, OverviewMap } from 'ol/control'
import { defaults as defaultInteractions, DragRotateAndZoom } from 'ol/interaction'
import { createStringXY } from 'ol/coordinate'
import { XYZ } from 'ol/source'
import { Style, Stroke, Fill, Circle as CircleStyle } from 'ol/style'

import { CONFIG } from '@/config'
import { fetchNearbyStands } from '@/api/forest'
import { useLayers } from './useLayers'
import { usePopup } from './usePopup'

const baseSource = new XYZ({
    url: 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}',
    attributions: '&copy; 高德地图',
    maxZoom: 18,
    crossOrigin: 'anonymous'
})

export function useMap(targetId, options = {}) {
    const map = ref(null)
    const view = ref(null)
    const isInitialized = ref(false)
    const error = ref(null)
    const radiusQueryActive = ref(false)
    const radiusQueryRadius = ref(1000)

    // 存储当前筛选条件
    const currentFilters = ref({
        species: null,
        origin: null,
        minVolume: null
    })

    const {
        layerInstances,
        initializeLayers,
        getLayerByName,
        toggleLayer,
        setLayerOpacity,
        clearHighlight,
        applyCQLFilter,
        loadHeatmapFeatures,
        showRadiusCircle
    } = useLayers(map)

    const {
        popupContent,
        popupVisible,
        initPopup,
        showPopup,
        closePopup,
        showLoading,
        showError
    } = usePopup(map)

    const initMap = async () => {
        try {
            await nextTick()
            
            const container = document.getElementById(targetId)
            if (!container) {
                throw new Error(`地图容器 #${targetId} 不存在`)
            }
            
            let retries = 50
            while (retries > 0) {
                const rect = container.getBoundingClientRect()
                if (rect.width > 0 && rect.height > 0) {
                    break
                }
                await new Promise(resolve => setTimeout(resolve, 100))
                retries--
            }

            const finalRect = container.getBoundingClientRect()
            if (finalRect.width === 0 || finalRect.height === 0) {
                throw new Error('地图容器尺寸为 0')
            }

            view.value = new View({
                center: fromLonLat([CONFIG.CENTER_LON, CONFIG.CENTER_LAT]),
                zoom: CONFIG.DEFAULT_ZOOM,
                minZoom: CONFIG.MIN_ZOOM,
                maxZoom: CONFIG.MAX_ZOOM,
                constrainRotation: false
            })

            map.value = new Map({
                target: container,
                view: view.value,
                controls: [
                    // 比例尺
                    new ScaleLine({ units: 'metric' }),
                    // 全屏
                    new FullScreen(),
                    // 鼠标位置
                    new MousePosition({
                        coordinateFormat: createStringXY(4),
                        projection: 'EPSG:4326',
                        className: 'custom-mouse-position'
                    })
                ],
                interactions: defaultInteractions().extend([
                    new DragRotateAndZoom()
                ])
            })

            // 延迟更新尺寸
            setTimeout(() => {
                map.value?.updateSize()
                console.log('✓ 强制更新地图尺寸')
            }, 200)

            initializeLayers(baseSource)
            initPopup()

            // 绑定点击事件
            map.value.on('click', handleMapClick)
            map.value.on('pointermove', handlePointerMove)

            setTimeout(() => {
                map.value?.updateSize()
            }, 200)

            window.addEventListener('resize', () => {
                setTimeout(() => map.value?.updateSize(), 100)
            })

            // ==================== 修复 willReadFrequently 警告 ====================
            setTimeout(() => {
                try {
                    const canvas = container.querySelector('canvas')
                    if (canvas && !canvas.dataset.optimized) {
                        const ctx = canvas.getContext('2d', { willReadFrequently: true })
                        if (ctx) {
                            canvas.dataset.optimized = 'true'
                            console.log('✓ Canvas 已优化 willReadFrequently')
                        }
                    }
                } catch (e) {
                    console.warn('Canvas 优化失败:', e)
                }
            }, 300)

            // ==================== 移动端性能优化 ====================
            if (window.innerWidth <= 768) {
                map.value.setPixelRatio(1)
                console.log('✓ 移动端像素比已优化为 1')
            }

            isInitialized.value = true
            console.log('✓ 地图初始化完成')

        } catch (err) {
            error.value = err.message
            console.error('❌ 地图初始化失败:', err)
            throw err
        }
    }

    // ==================== 点击事件处理 ====================
    const handleMapClick = async (evt) => {
        const coordinate = evt.coordinate
        const lonLat = toLonLat(coordinate)

        if (options.radiusQuery?.active?.value || radiusQueryActive.value) {
            const radius = options.radiusQuery?.radius?.value || radiusQueryRadius.value || 1000
            
            console.log('半径查询模式，半径:', radius)
            
            try {
                showLoading(coordinate)

                const stands = await fetchNearbyStands(lonLat[0], lonLat[1], radius)
                
                // 计算统计数据
                const totalVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0) * (s.areaHa || 0), 0)
                const totalArea = stands.reduce((sum, s) => sum + (s.areaHa || 0), 0)
                
                // 显示查询圆圈
                showRadiusCircle(lonLat[0], lonLat[1], radius)
                
                // 显示结果
                showPopup({
                    type: 'radius',
                    stands,
                    lon: lonLat[0],
                    lat: lonLat[1],
                    radius,
                    totalVolume,
                    totalArea
                }, coordinate)
                
                // 调用回调通知 MapContainer
                options.onRadiusQueryResult?.(stands, lonLat[0], lonLat[1], radius)
                
                return
                
            } catch (err) {
                console.error('半径查询失败:', err)
                showError('查询失败: ' + err.message, coordinate)
                return
            }
        }

        // 优先检测点击的矢量要素（林场标记）
        const clickedFeature = getClickedFeature(evt)
        
        if (clickedFeature) {
            // 点击了林场标记，显示详细信息
            showStandDetailPopup(clickedFeature, coordinate)
        } else {
            // 点击空白处，执行 WFS 查询
            await queryWFSFeature(coordinate)
        }
    }

    // 获取点击的矢量要素
    const getClickedFeature = (evt) => {
        if (!map.value) return null

        const pixel = map.value.getEventPixel(evt.originalEvent)
        
        // 检测所有矢量图层
        const vectorLayers = ['stands_markers', 'stands_vector']
        
        for (const layerName of vectorLayers) {
            const layer = getLayerByName(layerName)
            if (!layer || !layer.getVisible()) continue

            const features = map.value.getFeaturesAtPixel(pixel, {
                layerFilter: (l) => l === layer,
                hitTolerance: 10
            })

            if (features && features.length > 0) {
                return features[0]
            }
        }
        
        return null
    }

    // 显示林场详细信息弹窗
    const showStandDetailPopup = (feature, coordinate) => {
        if (!feature || !feature.getProperties) {
            console.error('无效的 feature:', feature)
            return
        }

        const props = feature.getProperties()

        // 健壮的属性获取函数 - 检查多种可能的来源
        const getProp = (...names) => {
            // 1. 首先检查 feature 的直接属性
            for (const name of names) {
                if (name === undefined) continue
                const value = props[name]
                if (value !== undefined && value !== null && value !== '') {
                    return value
                }
            }
            
            // 2. 检查 _raw 原始数据（API返回的完整数据）
            if (props._raw) {
                for (const name of names) {
                    if (name === undefined) continue
                    const rawValue = props._raw[name]
                    if (rawValue !== undefined && rawValue !== null && rawValue !== '') {
                        return rawValue
                    }
                }
            }
            
            // 3. 检查 feature 的 ID
            if (names.includes('standId') || names.includes('stand_id')) {
                const fid = feature.getId()
                if (fid) return fid
            }
            
            return null
        }

        // 获取 standId - 优先级：standId > stand_id > id > zone_id > feature.getId()
        let standId = getProp('standId', 'stand_id', 'id', 'zone_id')
        if (!standId) {
            console.warn('无法获取 standId，尝试备用方案')
            standId = feature.getId() || '-'
        }

        // 获取小班号 - 优先级：xiaoBanCode > xiao_ban_code > standNo
        let xiaoBanCode = getProp('xiaoBanCode', 'xiao_ban_code', 'standNo', 'stand_no')
        
        // 如果还是获取不到，尝试从 _raw 中深度查找
        if (!xiaoBanCode && props._raw) {
            const raw = props._raw
            xiaoBanCode = raw.xiaoBanCode || raw.xiao_ban_code || raw.xiaoBan || raw.xiao_ban
        }
        
        // 最后的兜底：尝试从其他字段推断
        if (!xiaoBanCode) {
            const name = getProp('standName', 'stand_name', 'name')
            if (name && typeof name === 'string' && name.includes('班')) {
                const match = name.match(/(\d+班)/)
                if (match) xiaoBanCode = match[1]
            }
        }

        console.log('【调试】解析结果 - standId:', standId, 'xiaoBanCode:', xiaoBanCode || '-')

        // 面积和蓄积量计算
        const area = parseFloat(getProp('areaHa', 'area_ha', 'area') || 0) || 0
        const volumePerHa = parseFloat(getProp('volumePerHa', 'volume_per_ha', 'volume') || 0) || 0
        let totalVolume = parseFloat(getProp('totalVolume', 'total_volume', 'total') || 0) || 0
        
        if (totalVolume === 0 && area > 0 && volumePerHa > 0) {
            totalVolume = area * volumePerHa
        }

        const data = {
            type: 'stand_detail',
            id: standId || '-',
            xiaoBanCode: xiaoBanCode || '-',
            name: getProp('standName', 'stand_name', 'name') || '未命名林分',
            downloadId: standId,
            displayNo: xiaoBanCode || standId || '-',
            species: getProp('dominantSpecies', 'dominant_species', 'species') || '未知',
            origin: getProp('origin', 'forest_origin', 'type') || '未知',
            area: Math.round(area * 100) / 100,
            volumePerHa: Math.round(volumePerHa * 100) / 100,
            totalVolume: Math.round(totalVolume * 100) / 100,
            age: getProp('standAge', 'stand_age', 'age') || '-',
            density: getProp('canopyDensity', 'canopy_density', 'density') || '-',
            aspect: getProp('aspect', 'direction') || '未知',
            slope: getProp('slope', 'gradient') || '-',
            altitude: getProp('elevation', 'altitude', 'height') || '-'
        }
        
        console.log('【调试】Popup data prepared:', data)
        showPopup(data, coordinate)
        
        // 高亮该林场
        if (standId && standId !== '-') {
            highlightStand(feature)
        }
    }

    // 高亮林场
    const highlightStand = (featureOrId) => {
        const highlightSource = getLayerByName('highlight')?.getSource()
        if (!highlightSource) return

        highlightSource.clear()
        
        // 如果传入的是 feature 对象
        if (featureOrId && typeof featureOrId === 'object' && featureOrId.getGeometry) {
            const feature = featureOrId
            
            // 添加脉冲高亮效果
            const highlightFeature = feature.clone()
            highlightFeature.setStyle(new Style({
                image: new CircleStyle({
                    radius: 25,
                    fill: new Fill({
                        color: 'rgba(255, 87, 34, 0.2)'
                    }),
                    stroke: new Stroke({
                        color: '#FF5722',
                        width: 4
                    })
                })
            }))
            highlightSource.addFeature(highlightFeature)
            
            // 3秒后移除高亮
            setTimeout(() => {
                highlightSource.removeFeature(highlightFeature)
            }, 3000)
            
            return
        }
        
        // 如果传入的是 ID
        const standId = featureOrId
        
        // 从标记图层获取
        const markerLayer = getLayerByName('stands_markers')
        const markers = markerLayer?.getSource().getFeatures() || []
        
        const marker = markers.find(f => {
            const fid = f.get('standId') || f.get('stand_id') || f.get('id') || f.get('zone_id')
            return String(fid) === String(standId)
        })
        
        if (marker) {
            // 添加脉冲高亮效果
            const highlightFeature = marker.clone()
            highlightFeature.setStyle(new Style({
                image: new CircleStyle({
                    radius: 25,
                    fill: new Fill({
                        color: 'rgba(255, 87, 34, 0.2)'
                    }),
                    stroke: new Stroke({
                        color: '#FF5722',
                        width: 4
                    })
                })
            }))
            highlightSource.addFeature(highlightFeature)
            
            // 3秒后移除高亮
            setTimeout(() => {
                highlightSource.removeFeature(highlightFeature)
            }, 3000)
        }
    }

    let isPointerMoving = false
    const handlePointerMove = (evt) => {
      if (!map.value) return
      
      if (isPointerMoving) return
      isPointerMoving = true
      setTimeout(() => { isPointerMoving = false }, 50)

    const pixel = map.value.getEventPixel(evt.originalEvent)
        
        // 检测是否悬停在矢量要素上
        const hit = map.value.hasFeatureAtPixel(pixel, {
            layerFilter: (layer) => {
                const name = layer.get('name')
                return name === 'stands_markers' || name === 'stands_vector'
            },
            hitTolerance: 5
        })
        
        // 改变鼠标样式
        map.value.getTargetElement().style.cursor = hit ? 'pointer' : ''
    }

    // ==================== WFS查询（点击空白处）====================
    const queryWFSFeature = async (coordinate) => {
        try {
            showLoading(coordinate)
            
            const resolution = view.value.getResolution()
            const buffer = 50 * resolution
            
            const minX = coordinate[0] - buffer
            const minY = coordinate[1] - buffer
            const maxX = coordinate[0] + buffer
            const maxY = coordinate[1] + buffer
            const bbox = `${minX},${minY},${maxX},${maxY}`
            
            const propertyName = 'zone_id,xiao_ban_code,stand_name,dominant_species,volume_per_ha,area_ha,origin,stand_age,canopy_density,center_lon,center_lat'
            
            const url = `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&version=1.1.0&request=GetFeature&` +
                `typename=forest:forest_stand&outputFormat=application/json&` +
                `srsname=EPSG:3857&bbox=${bbox},EPSG:3857&` +
                `propertyName=${propertyName}`

            console.log('WFS 查询 URL:', url)

            const response = await fetch(url, {
                headers: { 
                    'Accept': 'application/json, text/plain, */*'
                }
            })

            const contentType = response.headers.get('content-type') || ''
            let features = []

            if (contentType.includes('application/json')) {
                const data = await response.json()
                features = data.features || []
            } else {
                const text = await response.text()
                features = parseGMLFeatures(text)
            }

            features = features.filter(f => f.geometry)
            
            if (features.length === 0) {
                closePopup()
                return
            }

            // 找到最近的要素
            let nearestFeature = features[0]
            let minDistance = Infinity
            
            for (const f of features) {
                const geom = f.geometry
                if (geom.type === 'Point') {
                    const dx = geom.coordinates[0] - coordinate[0]
                    const dy = geom.coordinates[1] - coordinate[1]
                    const dist = Math.sqrt(dx*dx + dy*dy)
                    if (dist < minDistance) {
                        minDistance = dist
                        nearestFeature = f
                    }
                }
            }

            showWMSPopup(nearestFeature, coordinate)

        } catch (err) {
            console.error('WFS 查询失败:', err)
            showError('查询失败: ' + err.message, coordinate)
            setTimeout(closePopup, 2000)
        }
    }

    const parseGMLFeatures = (xmlString) => {
        const parser = new DOMParser()
        const xmlDoc = parser.parseFromString(xmlString, 'text/xml')
        
        const parseError = xmlDoc.getElementsByTagName('parsererror')
        if (parseError.length > 0) {
            console.error('XML 解析错误:', parseError[0].textContent)
            return []
        }
        
        const exceptions = xmlDoc.getElementsByTagName('ServiceException')
        if (exceptions.length > 0) {
            console.error('GeoServer 错误:', exceptions[0].textContent)
            return []
        }
        
        const features = []
        const featureNodes = xmlDoc.getElementsByTagName('gml:featureMember')
        
        for (let i = 0; i < featureNodes.length; i++) {
            const node = featureNodes[i].firstElementChild
            if (!node) continue
            
            const properties = {}
            let geometry = null
            
            for (let child of node.children) {
                const tagName = child.tagName.split(':').pop()
                
                if (tagName.toLowerCase().includes('geom') || 
                    tagName === 'the_geom' ||
                    tagName === 'geometry') {
                    geometry = extractGeometry(child)
                    continue
                }
                
                if (child.children.length === 0) {
                    properties[tagName] = child.textContent
                }
            }
            
            const fid = node.getAttribute('fid') || node.getAttribute('gml:id')
            
            features.push({
                type: 'Feature',
                id: fid,
                properties: properties,
                geometry: geometry
            })
        }
        
        return features
    }

    const extractGeometry = (geomNode) => {
        const point = geomNode.getElementsByTagName('gml:Point')[0]
        if (point) {
            const pos = point.getElementsByTagName('gml:pos')[0] || 
                       point.getElementsByTagName('gml:coordinates')[0]
            if (pos) {
                const coords = pos.textContent.trim().split(/[\s,]+/).map(Number)
                return {
                    type: 'Point',
                    coordinates: coords
                }
            }
        }
        return null
    }

    // WFS 查询结果显示
    const showWMSPopup = (feature, coordinate) => {
        const props = feature.properties || {}
        
        //适配可能的下划线命名（来自WFS）
        const area = parseFloat(props.area_ha) || parseFloat(props.areaHa) || 0
        const volumePerHa = parseFloat(props.volume_per_ha) || parseFloat(props.volumePerHa) || 0
        const totalVolume = parseFloat(props.total_volume) || parseFloat(props.totalVolume) || (area * volumePerHa) || 0
        
        // 尝试获取standId，WFS可能返回不同字段
        const standId = props.stand_id || props.standId || props.zone_id || props.id
        
        const data = {
            type: 'stand_detail',
            id: standId,
            xiaoBanCode: props.xiao_ban_code || props.xiaoBanCode || '-',
            name: props.stand_name || props.standName || '未命名林分',
            downloadId: standId,
            displayNo: props.xiao_ban_code || props.xiaoBanCode || '-',
            species: props.dominant_species || props.dominantSpecies || '未知',
            origin: props.origin || '未知',
            area: props.areaHa ||Math.round(area * 100) / 100,
            volumePerHa: Math.round(volumePerHa * 100) / 100,
            totalVolume: Math.round(totalVolume * 100) / 100,
            age: props.stand_age || props.standAge || '-',
            density: props.canopy_density || props.canopyDensity || '-'
        }
        
        showPopup(data, coordinate)
    }

    // ==================== 半径查询 ====================
    const setRadiusQueryActive = (active) => {
        radiusQueryActive.value = active
        if (map.value) {
            map.value.getTargetElement().style.cursor = active ? 'crosshair' : ''
        }
    }

    const setRadiusQueryRadius = (radius) => {
        radiusQueryRadius.value = radius
    }

    const executeRadiusQuery = async (lon, lat) => {
        const radius = radiusQueryRadius.value

        try {
            const stands = await fetchNearbyStands(lon, lat, radius)

            showRadiusCircle(lon, lat, radius)

            showPopup({
                type: 'radius',
                stands,
                lon,
                lat,
                radius
            }, fromLonLat([lon, lat]))

            options.onRadiusQueryResult?.(stands, lon, lat, radius)

        } catch (err) {
            console.error('半径查询失败:', err)
            showError('查询失败: ' + err.message, fromLonLat([lon, lat]))
        }
    }

    // ==================== 筛选条件 ====================
    const buildCQLFilter = () => {
        const conditions = []
        
        if (currentFilters.value.species) {
            conditions.push(`dominant_species='${currentFilters.value.species}'`)
        }
        
        if (currentFilters.value.origin) {
            conditions.push(`origin='${currentFilters.value.origin}'`)
        }
        
        if (currentFilters.value.minVolume > 0) {
            conditions.push(`volume_per_ha>=${currentFilters.value.minVolume}`)
        }
        
        const cqlFilter = conditions.length > 0 ? conditions.join(' AND ') : null
        console.log('构建的 CQL 条件:', cqlFilter)
        return cqlFilter
    }

    const applyFilters = (filters) => {
        if (filters.species !== undefined) {
            currentFilters.value.species = filters.species || null
        }
        if (filters.origin !== undefined) {
            currentFilters.value.origin = filters.origin || null
        }
        if (filters.minVolume !== undefined) {
            currentFilters.value.minVolume = filters.minVolume || null
        }
        
        const cqlFilter = buildCQLFilter()
        applyCQLFilter(cqlFilter, currentFilters.value)
    }

    const resetFilters = () => {
        currentFilters.value = {
            species: null,
            origin: null,
            minVolume: null
        }
        applyCQLFilter(null, currentFilters.value)
        clearHighlight()
    }

    // 居中显示（用于"定位"按钮）
    const zoomToCoordinate = (lon, lat, zoom = 15) => {
        if (!view.value) return
        view.value.animate({
            center: fromLonLat([lon, lat]),
            zoom: zoom,
            duration: 500
        })
    }

    const destroyMap = () => {
        closePopup()
        
        if (map.value) {
            map.value.setTarget(null)
            map.value.dispose()
            map.value = null
        }
        
        isInitialized.value = false
    }

    onUnmounted(() => {
        destroyMap()
    })

    return {
        map,
        view,
        isInitialized,
        error,
        radiusQueryActive,
        radiusQueryRadius,
        popupContent,
        popupVisible,
        layerInstances,
        currentFilters,
        getLayerByName,
        toggleLayer,
        setLayerOpacity,
        highlightStand,
        clearHighlight,
        loadHeatmapFeatures,
        applyFilters,
        resetFilters,
        setRadiusQueryActive,
        setRadiusQueryRadius,
        executeRadiusQuery,
        zoomToCoordinate,
        showPopup,
        closePopup,
        initMap,
        destroyMap
    }
}