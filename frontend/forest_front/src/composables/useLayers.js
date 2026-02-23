/**
 * 图层管理组合式函数 - 修复 WMS 加载问题
 */
import { ref } from 'vue'
import { Tile as TileLayer, Vector as VectorLayer, Heatmap as HeatmapLayer } from 'ol/layer'
import { XYZ, TileWMS, Vector as VectorSource } from 'ol/source'
import { GeoJSON } from 'ol/format'
import { fromLonLat } from 'ol/proj'
import { Style, Stroke, Fill, Text, Circle as CircleStyle } from 'ol/style'
import Feature from 'ol/Feature'
import Point from 'ol/geom/Point'
import Circle from 'ol/geom/Circle'

import { CONFIG, SPECIES_COLORS } from '@/config'
import { hexToRgba } from '@/utils/formatters'

export function useLayers(map) {
    const layerInstances = ref({})
    const currentFilter = ref('')
    const highlightSource = ref(null)
    const standsVisible = ref(true)
    const wmsError = ref(false) // 追踪 WMS 是否加载失败

    // ==================== 图层创建 ====================
    
    const createBaseLayer = (source) => {
        return new TileLayer({
            source: source,
            name: 'base',
            visible: true,
            zIndex: 1
        })
    }

    const createSatelliteLayer = () => {
        return new TileLayer({
            source: new XYZ({
                url: 'https://webst01.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}',
                attributions: '&copy; 高德地图',
                maxZoom: 18,
                crossOrigin: 'anonymous'
            }),
            name: 'satellite',
            visible: false,
            zIndex: 2
        })
    }

    /**
     * 创建林分 WMS 图层 - 添加详细错误处理
     */
    const createStandsWMSLayer = () => {
        // 检查配置
        if (!CONFIG.GEOSERVER_URL || CONFIG.GEOSERVER_URL === 'http://localhost:8080/geoserver') {
            console.warn('⚠️ GeoServer URL 未配置或使用的是默认值，WMS 可能无法加载')
        }

        const source = new TileWMS({
            url: `${CONFIG.GEOSERVER_URL}/wms`,
            params: {
                'LAYERS': 'forest:forest_stand',
                'TILED': true,
                'FORMAT': 'image/png',
                'TRANSPARENT': true,
                'VERSION': '1.1.1',
                'SRS': 'EPSG:3857'
            },
            serverType: 'geoserver',
            crossOrigin: 'anonymous',
            // 添加超时设置
            timeout: 10000
        })

        // 详细的错误处理
        source.on('tileloaderror', (event) => {
            wmsError.value = true
            console.error('❌ WMS 瓦片加载失败:', {
                url: event.tile.src_,
                layer: 'forest:forest_stand',
                message: '请检查 GeoServer 是否启动，或图层名称是否正确'
            })
            
            // 显示用户友好的提示
            if (map.value) {
                console.info('💡 提示：将使用备选矢量标记图层显示林分')
            }
        })

        source.on('tileloadstart', () => {
            // console.log('WMS 瓦片开始加载...')
        })

        source.on('tileloadend', () => {
            wmsError.value = false
            // console.log('✓ WMS 瓦片加载成功')
        })

        return new TileLayer({
            source: source,
            name: 'stands',
            visible: true,
            opacity: 0.9,
            zIndex: 10
        })
    }

    /**
     * 创建林分矢量标记图层（WMS 失败时的备选）
     */
    const createStandsMarkerLayer = () => {
        const source = new VectorSource()
        
        return new VectorLayer({
            source: source,
            name: 'stands_markers',
            visible: true,
            style: (feature) => {
                const volume = feature.get('volume_per_ha') || 0
                const species = feature.get('dominant_species') || '未知'
                const color = SPECIES_COLORS[species] || '#2E7D32'
                
                // 根据蓄积量调整大小
                const radius = Math.max(8, Math.min(20, volume / 15))
                
                return new Style({
                    image: new CircleStyle({
                        radius: radius,
                        fill: new Fill({
                            color: hexToRgba(color, 0.8)
                        }),
                        stroke: new Stroke({
                            color: '#fff',
                            width: 2
                        })
                    }),
                    text: new Text({
                        text: feature.get('stand_name') || feature.get('xiao_ban_code') || '',
                        font: 'bold 11px sans-serif',
                        fill: new Fill({ color: '#000' }),
                        stroke: new Stroke({
                            color: '#fff',
                            width: 3
                        }),
                        offsetY: -radius - 8
                    })
                })
            },
            zIndex: 12
        })
    }

    /**
     * 创建林分边界矢量图层（用于高亮和详细信息）
     */
    const createStandsVectorLayer = () => {
        const source = new VectorSource({
            format: new GeoJSON()
        })

        return new VectorLayer({
            source: source,
            name: 'stands_vector',
            visible: false, // 默认隐藏，用于高亮
            style: (feature) => {
                const species = feature.get('dominant_species') || '未知'
                const color = SPECIES_COLORS[species] || '#2E7D32'
                
                return new Style({
                    fill: new Fill({
                        color: hexToRgba(color, 0.4)
                    }),
                    stroke: new Stroke({
                        color: color,
                        width: 2
                    })
                })
            },
            zIndex: 11
        })
    }

    const createHeatmapLayer = () => {
        return new HeatmapLayer({
            source: new VectorSource(),
            blur: 25,
            radius: 20,
            weight: (feature) => {
                const volume = feature.get('volume_per_ha') || 0
                return Math.min(volume / 200, 1.0)
            },
            name: 'heatmap',
            visible: false,
            gradient: ['#00f', '#0ff', '#0f0', '#ff0', '#f00'],
            zIndex: 5
        })
    }

    const createHighlightLayer = () => {
        highlightSource.value = new VectorSource()
        return new VectorLayer({
            source: highlightSource.value,
            name: 'highlight',
            visible: true,
            style: new Style({
                stroke: new Stroke({
                    color: '#FF5722',
                    width: 3
                }),
                fill: new Fill({
                    color: 'rgba(255, 87, 34, 0.2)'
                })
            }),
            zIndex: 20
        })
    }

    // ==================== 图层控制 ====================

    const initializeLayers = (baseSource) => {
        if (!map.value) return

        const layers = [
            createBaseLayer(baseSource),
            createSatelliteLayer(),
            createHeatmapLayer(),
            createStandsWMSLayer(),      // WMS 图层（可能失败）
            createStandsVectorLayer(),
            createStandsMarkerLayer(),    // 备选标记图层
            createHighlightLayer()
        ]

        layers.forEach(layer => {
            map.value.addLayer(layer)
            layerInstances.value[layer.get('name')] = layer
        })

        // 立即加载标记数据（作为 WMS 的备选）
        loadStandsMarkers()
        
        // 测试 WMS 是否可用
        testWMSConnection()
    }

    /**
     * 测试 WMS 连接
     */
    const testWMSConnection = async () => {
        try {
            const testUrl = `${CONFIG.GEOSERVER_URL}/wms?service=WMS&version=1.1.1&request=GetCapabilities`
            await fetch(testUrl, { 
                method: 'HEAD',
                mode: 'no-cors' // 先尝试无跨域模式
            })
            console.log('✓ GeoServer WMS 服务可访问')
        } catch (error) {
            console.warn('⚠️ GeoServer WMS 服务可能未启动或不可访问:', error.message)
            console.info('💡 系统将使用矢量标记图层作为备选方案')
            
            // 如果 WMS 不可用，确保标记图层可见
            const markerLayer = getLayerByName('stands_markers')
            if (markerLayer) {
                markerLayer.setVisible(true)
            }
        }
    }

    const getLayerByName = (name) => {
        return layerInstances.value[name] || null
    }

    /**
     * 切换图层可见性
     */
    const toggleLayer = (layerName, visible) => {
        if (layerName === 'stands') {
            const wmsLayer = getLayerByName('stands')
            const markerLayer = getLayerByName('stands_markers')
            const vectorLayer = getLayerByName('stands_vector')
            
            const newVisible = visible !== undefined ? visible : !(wmsLayer?.getVisible() ?? true)
            
            standsVisible.value = newVisible
            
            // 如果 WMS 失败，优先使用标记图层
            if (wmsError.value && markerLayer) {
                markerLayer.setVisible(newVisible)
                if (wmsLayer) wmsLayer.setVisible(false) // WMS 失败时隐藏
            } else {
                if (wmsLayer) wmsLayer.setVisible(newVisible)
                if (markerLayer) markerLayer.setVisible(newVisible)
            }
            
            if (vectorLayer) vectorLayer.setVisible(false) // 矢量层默认隐藏
            
            console.log(`林分图层切换: ${newVisible ? '显示' : '隐藏'}${wmsError.value ? ' (使用备选标记)' : ''}`)
            return newVisible
        }
        
        const layer = getLayerByName(layerName)
        if (!layer) {
            console.warn(`未找到图层: ${layerName}`)
            return
        }

        const newVisible = visible !== undefined ? visible : !layer.getVisible()
        layer.setVisible(newVisible)

        if (layerName === 'heatmap' && newVisible) {
            refreshHeatmapData()
        }

        return newVisible
    }

    const setLayerOpacity = (layerName, opacity) => {
        if (layerName === 'stands') {
            const wmsLayer = getLayerByName('stands')
            const markerLayer = getLayerByName('stands_markers')
            if (wmsLayer && !wmsError.value) wmsLayer.setOpacity(opacity)
            if (markerLayer) markerLayer.setOpacity(opacity)
        } else {
            const layer = getLayerByName(layerName)
            if (layer) {
                layer.setOpacity(opacity)
            }
        }
    }

    // ==================== 数据加载 ====================

    /**
     * 从 API 加载林分标记数据
     */
    const loadStandsMarkers = async () => {
        const markerLayer = getLayerByName('stands_markers')
        if (!markerLayer) {
            console.warn('标记图层未找到')
            return
        }

        try {
            console.log('正在加载林分标记数据...')
            
            // 使用绝对路径或配置的基础 URL
            const apiUrl = CONFIG.API_BASE ? `${CONFIG.API_BASE}/stands` : '/api/stands'
            
            const response = await fetch(apiUrl, {
                headers: {
                    'Accept': 'application/json'
                }
            })
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }
            
            const stands = await response.json()
            
            if (!Array.isArray(stands) || stands.length === 0) {
                console.warn('返回的林分数据为空')
                return
            }

            const features = stands.map(stand => {
                // 检查坐标是否存在
                if (!stand.centerLon || !stand.centerLat) {
                    console.warn(`林分 ${stand.id} 缺少坐标`)
                    return null
                }
                
                return new Feature({
                    geometry: new Point(fromLonLat([stand.centerLon, stand.centerLat])),
                    id: stand.id,
                    stand_name: stand.standName || stand.stand_name,
                    xiao_ban_code: stand.xiaoBanCode || stand.xiao_ban_code,
                    dominant_species: stand.dominantSpecies || stand.dominant_species,
                    volume_per_ha: stand.volumePerHa || stand.volume_per_ha,
                    area: stand.area,
                    origin: stand.origin,
                    ...stand
                })
            }).filter(f => f !== null)

            markerLayer.getSource().clear()
            markerLayer.getSource().addFeatures(features)
            
            console.log(`✓ 加载了 ${features.length} 个林分标记`)
            
            // 如果 WMS 失败，确保标记图层可见
            if (wmsError.value) {
                markerLayer.setVisible(true)
            }
            
        } catch (error) {
            console.error('❌ 加载林分标记失败:', error.message)
            console.info('💡 请检查：1. API 服务是否启动 2. 网络连接是否正常')
        }
    }

    const refreshHeatmapData = async () => {
        const layer = getLayerByName('heatmap')
        if (!layer) return

        const source = layer.getSource()
        if (source.getFeatures().length > 0) return

        try {
            const apiUrl = CONFIG.API_BASE ? `${CONFIG.API_BASE}/stands` : '/api/stands'
            const response = await fetch(apiUrl)
            const stands = await response.json()
            loadHeatmapFeatures(stands)
        } catch (error) {
            console.error('刷新热力图失败:', error)
        }
    }

    const loadHeatmapFeatures = (stands) => {
        const layer = getLayerByName('heatmap')
        if (!layer) return

        const features = stands.map(stand => {
            return new Feature({
                geometry: new Point(fromLonLat([stand.centerLon, stand.centerLat])),
                volume_per_ha: stand.volumePerHa,
                stand_name: stand.standName,
                dominant_species: stand.dominantSpecies,
                stand_id: stand.id
            })
        })

        layer.getSource().clear()
        layer.getSource().addFeatures(features)
    }

    // ==================== 高亮与筛选 ====================

    const highlightStand = async (standId) => {
        if (!highlightSource.value) return

        highlightSource.value.clear()

        // 先尝试从标记图层获取
        const markerLayer = getLayerByName('stands_markers')
        const markers = markerLayer?.getSource().getFeatures() || []
        
        console.log('Looking for standId:', standId, 'in', markers.length, 'markers') // 调试
        
        // 尝试多种 ID 字段匹配
        const marker = markers.find(f => {
            const fid = f.get('id') || f.get('zone_id') || f.get('stand_id') || f.getId()
            console.log('Checking marker:', fid) // 调试
            return String(fid) === String(standId)
        })
        
        if (marker) {
            console.log('Found marker:', marker.getProperties()) // 调试
            const geom = marker.getGeometry()
            if (map.value && geom) {
                map.value.getView().fit(geom.getExtent(), {
                    padding: [100, 100, 100, 100],
                    duration: 500,
                    maxZoom: 16
                })
            }
            
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
            highlightSource.value.addFeature(highlightFeature)
            
            // 3秒后移除高亮
            setTimeout(() => {
                highlightSource.value.removeFeature(highlightFeature)
            }, 3000)
            
            return
        }
        
        console.warn('Marker not found for standId:', standId) // 调试
        
        // 回退到 WFS 查询
        try {
            const url = `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&version=1.1.0&request=GetFeature&` +
                `typename=forest:forest_stand&outputFormat=application/json&` +
                `cql_filter=zone_id=${standId}`

            const response = await fetch(url)
            const geojson = await response.json()

            const features = new GeoJSON().readFeatures(geojson, {
                featureProjection: 'EPSG:3857'
            })

            highlightSource.value.addFeatures(features)

            if (features.length > 0 && map.value) {
                map.value.getView().fit(features[0].getGeometry().getExtent(), {
                    padding: [100, 100, 100, 100],
                    duration: 500
                })
            }
        } catch (error) {
            console.error('高亮失败:', error)
        }
    }

    const clearHighlight = () => {
        if (highlightSource.value) {
            highlightSource.value.clear()
        }
    }

    const showRadiusCircle = (lon, lat, radius) => {
        if (!highlightSource.value) return

        const center = fromLonLat([lon, lat])
        const circle = new Circle(center, radius)

        const feature = new Feature({
            geometry: circle,
            radius: radius
        })

        feature.setStyle(new Style({
            fill: new Fill({
                color: 'rgba(56, 142, 60, 0.1)'
            }),
            stroke: new Stroke({
                color: '#388E3C',
                width: 2,
                lineDash: [5, 5]
            })
        }))

        highlightSource.value.addFeature(feature)

        // 5秒后自动移除圆圈
        setTimeout(() => {
            if (highlightSource.value) {
                highlightSource.value.removeFeature(feature)
            }
        }, 5000)
    }

    // ==================== CQL筛选 ====================

    const applyCQLFilter = (cqlFilter, filterObj = null) => {
    // 应用到 WMS 图层
    const wmsLayer = getLayerByName('stands')
        if (wmsLayer && !wmsError.value) {
            const source = wmsLayer.getSource()
            const params = source.getParams()
            
            if (cqlFilter) {
                params.CQL_FILTER = cqlFilter
            } else {
                delete params.CQL_FILTER
            }
            
            source.updateParams(params)
            console.log('WMS CQL_FILTER 已更新:', cqlFilter || '清除')
        }
        
        // 应用到矢量标记图层（客户端筛选）
        filterMarkers(filterObj || { species: null, origin: null, minVolume: null })
    }

    // 矢量标记筛选（修复：支持多条件组合）
    const filterMarkers = (filters) => {
        const markerLayer = getLayerByName('stands_markers')
        if (!markerLayer) return

        const features = markerLayer.getSource().getFeatures()
        
        // 如果没有筛选条件，显示全部
        const hasFilter = filters.species || filters.origin || (filters.minVolume > 0)
        if (!hasFilter) {
            features.forEach(feature => feature.setStyle(undefined))
            return
        }

        // 逐个评估条件
        features.forEach(feature => {
            let visible = true
            
            // 树种条件
            if (filters.species) {
                visible = visible && feature.get('dominant_species') === filters.species
            }
            
            // 起源条件
            if (filters.origin) {
                visible = visible && feature.get('origin') === filters.origin
            }
            
            // 蓄积量条件
            if (filters.minVolume > 0) {
                visible = visible && (feature.get('volume_per_ha') || 0) >= filters.minVolume
            }
            
            // 应用样式（可见/不可见）
            feature.setStyle(visible ? undefined : new Style({}))
        })
    }

    const filterBySpecies = (species) => {
        const conditions = []
        if (species) {
            conditions.push(`dominant_species='${species}'`)
        }
        if (currentFilter.value && !currentFilter.value.includes('dominant_species')) {
            conditions.push(currentFilter.value)
        }
        applyCQLFilter(conditions.length > 0 ? conditions.join(' AND ') : null)
    }

    const filterByOrigin = (origin) => {
        const conditions = []
        if (origin) {
            conditions.push(`origin='${origin}'`)
        }
        applyCQLFilter(conditions.length > 0 ? conditions.join(' AND ') : null)
    }

    const filterByVolumeRange = (min, max) => {
        let condition = ''
        if (min !== undefined && max !== undefined) {
            condition = `volume_per_ha>=${min} AND volume_per_ha<=${max}`
        } else if (min !== undefined) {
            condition = `volume_per_ha>=${min}`
        } else if (max !== undefined) {
            condition = `volume_per_ha<=${max}`
        }
        applyCQLFilter(condition || null)
    }

    const getCurrentCQLFilter = () => currentFilter.value

    return {
        layerInstances,
        standsVisible,
        wmsError,
        initializeLayers,
        getLayerByName,
        toggleLayer,
        setLayerOpacity,
        loadStandsMarkers,
        loadHeatmapFeatures,
        highlightStand,
        clearHighlight,
        showRadiusCircle,
        applyCQLFilter,
        filterBySpecies,
        filterByOrigin,
        filterByVolumeRange,
        getCurrentCQLFilter
    }
}