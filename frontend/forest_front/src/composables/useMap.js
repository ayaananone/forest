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

    const {
        layerInstances,
        initializeLayers,
        getLayerByName,
        toggleLayer,
        setLayerOpacity,
        highlightStand,
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

    const handleMapClick = async (evt) => {
        const coordinate = evt.coordinate
        const lonLat = toLonLat(coordinate)

        if (radiusQueryActive.value) {
            executeRadiusQuery(lonLat[0], lonLat[1])
            return
        }

        await queryWFSFeature(coordinate)
    }

    const handlePointerMove = (evt) => {
        if (!map.value) return
        const pixel = map.value.getEventPixel(evt.originalEvent)
        const hit = map.value.hasFeatureAtPixel(pixel, {
            layerFilter: (layer) => layer.get('name') !== 'heatmap'
        })
        map.value.getTargetElement().style.cursor = hit ? 'pointer' : ''
    }

    // ==================== WFS查询 ====================

    const queryWFSFeature = async (coordinate) => {
        const layer = getLayerByName('stands')
        if (!layer || !layer.getVisible()) {
            closePopup()
            return
        }

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
            console.log('WFS 响应类型:', contentType)

            let features = []

            if (contentType.includes('application/json')) {
                const data = await response.json()
                features = data.features || []
            } else if (contentType.includes('xml') || contentType.includes('text/xml')) {
                const text = await response.text()
                console.warn('收到 XML 响应:', text.substring(0, 500))
                features = parseGMLFeatures(text)
            } else {
                const text = await response.text()
                try {
                    const data = JSON.parse(text)
                    features = data.features || []
                } catch {
                    features = parseGMLFeatures(text)
                }
            }

            features = features.filter(f => {
                if (!f.geometry) return false
                const geom = f.geometry
                if (geom.type === 'Point') {
                    const dx = geom.coordinates[0] - coordinate[0]
                    const dy = geom.coordinates[1] - coordinate[1]
                    return Math.sqrt(dx*dx + dy*dy) < buffer * 2
                }
                return true
            })

            if (features.length > 0) {
                if (features.length === 1) {
                    showWMSPopup(features[0], coordinate)
                } else {
                    showFeatureListPopup(features, coordinate)
                }
            } else {
                closePopup()
            }
            
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
        
        const polygon = geomNode.getElementsByTagName('gml:Polygon')[0]
        if (polygon) {
            return { type: 'Polygon' }
        }
        
        return null
    }

    const showWMSPopup = (feature, coordinate) => {
        const props = feature.properties || {}
        
        const data = {
            type: 'detail',
            id: props.zone_id,
            name: props.stand_name || '未命名林分',
            species: props.dominant_species || '未知',
            volume: props.volume_per_ha || 0,
            area: props.area_ha || 0,
            origin: props.origin || '未知',
            age: props.stand_age || '-',
            density: props.canopy_density || '-',  
            standNo: props.xiao_ban_code || '-'
        }
        showPopup(data, coordinate)
    }

    const showFeatureListPopup = (features, coordinate) => {
        showPopup({
            type: 'list',
            features: features.map(f => ({
                id: f.properties?.zone_id || f.id,
                name: f.properties?.stand_name || '未命名林分',
                species: f.properties?.dominant_species || '未知',
                standNo: f.properties?.xiao_ban_code || '-',  
                ...f.properties
            }))
        }, coordinate)
    }

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

    const applyFilters = (filters) => {
        const conditions = []
        if (filters.species) {
            conditions.push(`dominant_species='${filters.species}'`)
        }
        if (filters.origin) {
            conditions.push(`origin='${filters.origin}'`)
        }
        if (filters.minVolume > 0) {
            conditions.push(`volume_per_ha>=${filters.minVolume}`)
        }

        const cqlFilter = conditions.length > 0 ? conditions.join(' AND ') : null
        applyCQLFilter(cqlFilter)
    }

    const resetFilters = () => {
        applyCQLFilter(null)
        clearHighlight()
    }

    const zoomToExtent = (extent) => {
        if (!view.value) return
        view.value.fit(extent, {
            padding: [100, 100, 100, 100],
            duration: 500
        })
    }

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
        zoomToExtent,
        zoomToCoordinate,
        showPopup,
        closePopup,
        initMap,
        destroyMap
    }
}