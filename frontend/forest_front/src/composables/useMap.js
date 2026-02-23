/**
 * 地图核心组合式函数
 */
import { ref, onUnmounted, nextTick } from 'vue'
import { Map, View } from 'ol'
import { fromLonLat, toLonLat } from 'ol/proj'
import { defaults as defaultControls, ScaleLine, FullScreen, ZoomSlider, MousePosition, OverviewMap } from 'ol/control'
import { defaults as defaultInteractions, DragRotateAndZoom } from 'ol/interaction'
import { createStringXY } from 'ol/coordinate'
import { Tile as TileLayer } from 'ol/layer'
import { XYZ } from 'ol/source'
import { Style, Stroke, Fill, Circle as CircleStyle } from 'ol/style'

import { CONFIG } from '@/config'
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
                controls: defaultControls().extend([
                    new ScaleLine({ units: 'metric' }),
                    new FullScreen(),
                    new ZoomSlider(),
                    new MousePosition({
                        coordinateFormat: createStringXY(4),
                        projection: 'EPSG:4326',
                        className: 'custom-mouse-position'
                    }),
                    new OverviewMap({
                        collapsed: true,
                        layers: [new TileLayer({ source: baseSource })]
                    })
                ]),
                interactions: defaultInteractions().extend([
                    new DragRotateAndZoom()
                ])
            })

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
                
                const { fetchNearbyStands } = await import('@/api/forest')
                const stands = await fetchNearbyStands(lonLat[0], lonLat[1], radius)
                
                // 计算统计数据
                const totalVolume = stands.reduce((sum, s) => sum + (s.volumePerHa || 0) * (s.area || 0), 0)
                const totalArea = stands.reduce((sum, s) => sum + (s.area || 0), 0)
                
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
                
                return // 半径查询模式下不执行其他逻辑
                
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

    // 显示林场详细信息弹窗（修复：不移除视图，只显示弹窗和高亮）
    const showStandDetailPopup = (feature, coordinate) => {
        const props = feature.getProperties()
        console.log('showStandDetailPopup props:', props)
        
        // 尝试多种可能的 ID 字段名
        const standId = props.id || props.zone_id || props.stand_id || props.objectid || props.OBJECTID || props.fid || props.xiao_ban_code || 'unknown'
        
        // 计算总面积（公顷）
        const area = parseFloat(props.area) || parseFloat(props.area_ha) || parseFloat(props.areaHa) || 0
        
        // 计算每公顷蓄积量
        const volumePerHa = parseFloat(props.volume_per_ha) || parseFloat(props.volumePerHa) || parseFloat(props.volume) || 0
        
        // 计算总蓄积量 = 每公顷蓄积 × 面积
        const totalVolume = volumePerHa * area
        
        const data = {
            type: 'stand_detail',
            id: standId,
            name: props.stand_name || props.standName || props.name || '未命名林分',
            standNo: props.xiao_ban_code || props.xiaoBanCode || props.stand_no || '-',
            species: props.dominant_species || props.dominantSpecies || props.species || '未知',
            origin: props.origin || props.forest_origin || '未知',
            
            // 关键数据
            area: Math.round(area * 100) / 100,
            volumePerHa: Math.round(volumePerHa * 100) / 100,
            totalVolume: Math.round(totalVolume * 100) / 100,
            
            // 其他信息
            age: props.stand_age || props.standAge || props.age || '-',
            density: props.canopy_density || props.canopyDensity || props.density || '-',
            aspect: props.aspect || '未知',
            slope: props.slope || '-',
            altitude: props.altitude || props.elevation || '-'
        }
        
        console.log('Popup data prepared:', data)
        
        // 显示弹窗
        showPopup(data, coordinate)
        
        // 自动居中显示（关键修改）
        if (view.value && feature.getGeometry) {
            const geom = feature.getGeometry()
            const center = geom.getCoordinates()
            
            console.log('自动居中到坐标:', center)
            
            // 平滑移动到中心，保持当前缩放级别
            view.value.animate({
                center: center,
                duration: 500
            })
        }
        
        // 高亮该林场
        if (standId !== 'unknown') {
            highlightStand(feature)
        }
    }

    // 高亮林场（修复：不移除视图，只添加高亮圈）
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
        
        // 如果传入的是 ID（兼容旧代码）
        const standId = featureOrId
        
        // 从标记图层获取
        const markerLayer = getLayerByName('stands_markers')
        const markers = markerLayer?.getSource().getFeatures() || []
        
        const marker = markers.find(f => {
            const fid = f.get('id') || f.get('zone_id') || f.get('stand_id') || f.get('xiao_ban_code')
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

    const handlePointerMove = (evt) => {
        if (!map.value) return
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
        
        const area = parseFloat(props.area_ha) || 0
        const volumePerHa = parseFloat(props.volume_per_ha) || 0
        const totalVolume = volumePerHa * area
        
        const data = {
            type: 'stand_detail',
            id: props.zone_id || props.xiao_ban_code,
            name: props.stand_name || '未命名林分',
            standNo: props.xiao_ban_code || '-',
            species: props.dominant_species || '未知',
            origin: props.origin || '未知',
            area: Math.round(area * 100) / 100,
            volumePerHa: Math.round(volumePerHa * 100) / 100,
            totalVolume: Math.round(totalVolume * 100) / 100,
            age: props.stand_age || '-',
            density: props.canopy_density || '-'
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
            const { fetchNearbyStands } = await import('@/api/forest')
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