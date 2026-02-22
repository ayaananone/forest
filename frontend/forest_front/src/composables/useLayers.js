/**
 * 图层管理组合式函数
 */
import { ref } from 'vue'
import { Tile as TileLayer, Vector as VectorLayer, Heatmap as HeatmapLayer } from 'ol/layer'
import { XYZ, TileWMS, Vector as VectorSource } from 'ol/source'
import { GeoJSON } from 'ol/format'
import { fromLonLat } from 'ol/proj'
import { bbox as bboxStrategy } from 'ol/loadingstrategy'
import { Style, Stroke, Fill, Text } from 'ol/style'
import Feature from 'ol/Feature'
import Point from 'ol/geom/Point'
import Circle from 'ol/geom/Circle'

import { CONFIG, SPECIES_COLORS } from '@/config'
import { hexToRgba } from '@/utils/formatters'

export function useLayers(map) {
    const layerInstances = ref({})
    const wfsSource = ref(null)
    const currentFilter = ref('')
    const highlightSource = ref(null)

    // ==================== 图层创建 ====================
    
    const createBaseLayer = () => {
        return new TileLayer({
            source: new XYZ({
                // 使用国内 OSM 镜像
                url: 'https://osm.open.cn/{z}/{x}/{y}.png',
                attributions: '© OpenStreetMap contributors',
                crossOrigin: 'anonymous',
                maxZoom: 19
            }),
            name: 'base',
            visible: true,
            zIndex: 1
        })
    }


    const createSatelliteLayer = () => {
        return new TileLayer({
            source: new XYZ({
                url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
                attributions: '© Esri'
            }),
            name: 'satellite',
            visible: false,
            zIndex: 2
        })
    }


    const createStandsWMSLayer = () => {
        return new TileLayer({
            source: new TileWMS({
                url: `${CONFIG.GEOSERVER_URL}/wms`,
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
            opacity: 0.9,
            zIndex: 10
        })
    }

    const createStandsWFSLayer = () => {
        wfsSource.value = new VectorSource({
            format: new GeoJSON(),
            url: (extent) => {
                return `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&` +
                    `version=1.1.0&request=GetFeature&typename=forest:forest_stand&` +
                    `outputFormat=application/json&srsname=EPSG:3857&` +
                    `bbox=${extent.join(',')},EPSG:3857`
            },
            strategy: bboxStrategy
        })

        return new VectorLayer({
            source: wfsSource.value,
            name: 'stands_wfs',
            visible: false,
            style: standsVectorStyle,
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
                    color: 'rgba(255, 87, 34, 0.1)'
                })
            }),
            zIndex: 20
        })
    }

    // WFS矢量图层样式
    const standsVectorStyle = (feature) => {
        const species = feature.get('dominant_species') || '未知'
        const color = SPECIES_COLORS[species] || '#757575'
        const volume = feature.get('volume_per_ha') || 0
        const opacity = Math.max(0.4, Math.min(0.9, volume / 300))

        return new Style({
            fill: new Fill({
                color: hexToRgba(color, opacity)
            }),
            stroke: new Stroke({
                color: color,
                width: 2
            }),
            text: new Text({
                text: feature.get('stand_no') || '',
                font: '12px sans-serif',
                fill: new Fill({ color: '#fff' }),
                stroke: new Stroke({
                    color: '#000',
                    width: 3
                })
            })
        })
    }

    // ==================== 图层控制 ====================

    const initializeLayers = () => {
        if (!map.value) return

        const layers = [
            createBaseLayer(),
            createSatelliteLayer(),
            createHeatmapLayer(),
            createStandsWMSLayer(),
            createStandsWFSLayer(),
            createHighlightLayer()
        ]

        layers.forEach(layer => {
            map.value.addLayer(layer)
            layerInstances.value[layer.get('name')] = layer
        })
    }

    const getLayerByName = (name) => {
        return layerInstances.value[name] || null
    }

    const toggleLayer = (layerName, visible) => {
        const layer = getLayerByName(layerName)
        if (!layer) {
            console.warn(`未找到图层: ${layerName}`)
            return
        }

        const newVisible = visible !== undefined ? visible : !layer.getVisible()
        layer.setVisible(newVisible)

        // 如果开启热力图，刷新数据
        if (layerName === 'heatmap' && newVisible) {
            refreshHeatmapData()
        }

        return newVisible
    }

    const setLayerOpacity = (layerName, opacity) => {
        const layer = getLayerByName(layerName)
        if (layer) {
            layer.setOpacity(opacity)
        }
    }

    const getWMSLayerSource = () => {
        const layer = getLayerByName('stands')
        return layer ? layer.getSource() : null
    }

    // ==================== 数据加载 ====================

    const refreshHeatmapData = async () => {
        const layer = getLayerByName('heatmap')
        if (!layer) return

        const source = layer.getSource()
        if (source.getFeatures().length > 0) return

        try {
            const response = await fetch(`${CONFIG.API_BASE}/stands`)
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

        try {
            const url = `${CONFIG.GEOSERVER_URL}/wfs?service=WFS&version=1.1.0&request=GetFeature&` +
                `typename=forest:forest_stand&outputFormat=application/json&` +
                `cql_filter=id=${standId}`

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

        setTimeout(() => {
            highlightSource.value.removeFeature(feature)
        }, 5000)
    }

    // ==================== CQL筛选 ====================

    const applyCQLFilter = (cqlFilter) => {
        const source = getWMSLayerSource()
        if (!source) return

        const params = source.getParams()
        if (cqlFilter) {
            params.CQL_FILTER = cqlFilter
        } else {
            delete params.CQL_FILTER
        }
        source.updateParams(params)
        currentFilter.value = cqlFilter || ''
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
        initializeLayers,
        getLayerByName,
        toggleLayer,
        setLayerOpacity,
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