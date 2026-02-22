/**
 * 导出工具函数
 */

/**
 * 转换为CSV
 * @param {Array} data - 数据数组
 * @returns {string} CSV字符串
 */
export function convertToCSV(data) {
    if (!data || data.length === 0) return ''

    const headers = Object.keys(data[0])
    const rows = data.map(obj =>
        headers.map(h => {
            const val = obj[h]
            if (val === null || val === undefined) return ''
            if (typeof val === 'string' && (val.includes(',') || val.includes('"') || val.includes('\n'))) {
                return `"${val.replace(/"/g, '""')}"`
            }
            return val
        }).join(',')
    )

    // 添加BOM以支持中文
    const bom = '\uFEFF'
    return bom + [headers.join(','), ...rows].join('\n')
}

/**
 * 下载文件
 * @param {string} content - 文件内容
 * @param {string} filename - 文件名
 * @param {string} type - MIME类型
 */
export function downloadFile(content, filename, type = 'text/csv;charset=utf-8') {
    const blob = new Blob([content], { type })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
}

/**
 * 导出半径查询结果
 * @param {Array} data - 查询结果数据
 * @param {string} filename - 文件名
 */
export function exportRadiusQueryResult(data, filename = 'radius_query_result.csv') {
    if (!data || data.length === 0) {
        console.warn('没有数据可导出')
        return
    }

    // 格式化数据
    const formattedData = data.map(item => ({
        '林分编号': item.id || '',
        '林分名称': item.standName || '',
        '优势树种': item.dominantSpecies || '',
        '起源': item.origin || '',
        '面积(ha)': item.area || 0,
        '蓄积量(m³/ha)': item.volumePerHa || 0,
        '总蓄积(m³)': (item.volumePerHa || 0) * (item.area || 0),
        '林龄': item.age || '',
        '郁闭度': item.density || '',
        '经度': item.centerLon || '',
        '纬度': item.centerLat || ''
    }))

    const csv = convertToCSV(formattedData)
    downloadFile(csv, filename)
}

/**
 * 导出林分列表
 * @param {Array} stands - 林分数据
 * @param {string} filename - 文件名
 */
export function exportStandsList(stands, filename = 'stands_list.csv') {
    if (!stands || stands.length === 0) {
        console.warn('没有数据可导出')
        return
    }

    const formattedData = stands.map(item => ({
        '林分编号': item.id || '',
        '林分名称': item.standName || '',
        '优势树种': item.dominantSpecies || '',
        '起源': item.origin || '',
        '面积(ha)': item.area || 0,
        '蓄积量(m³/ha)': item.volumePerHa || 0,
        '总蓄积(m³)': (item.volumePerHa || 0) * (item.area || 0),
        '林龄': item.age || '',
        '郁闭度(%)': item.density || ''
    }))

    const csv = convertToCSV(formattedData)
    downloadFile(csv, filename)
}

/**
 * 导出统计报表
 * @param {Object} stats - 统计数据
 * @param {string} filename - 文件名
 */
export function exportStatisticsReport(stats, filename = 'statistics_report.csv') {
    const data = [
        { '指标': '小班总数', '数值': stats.totalStands || 0, '单位': '个' },
        { '指标': '总面积', '数值': (stats.totalArea || 0).toFixed(2), '单位': 'ha' },
        { '指标': '总蓄积', '数值': (stats.totalVolume || 0).toFixed(2), '单位': 'm³' },
        { '指标': '平均蓄积', '数值': (stats.avgVolume || 0).toFixed(2), '单位': 'm³/ha' }
    ]

    const csv = convertToCSV(data)
    downloadFile(csv, filename)
}

/**
 * 导出树种统计
 * @param {Array} speciesStats - 树种统计数据
 * @param {string} filename - 文件名
 */
export function exportSpeciesStats(speciesStats, filename = 'species_statistics.csv') {
    if (!speciesStats || speciesStats.length === 0) {
        console.warn('没有数据可导出')
        return
    }

    const formattedData = speciesStats.map(item => ({
        '树种': item.species || '',
        '小班数量': item.count || 0,
        '总面积(ha)': (item.totalArea || 0).toFixed(2),
        '总蓄积(m³)': (item.totalVolume || 0).toFixed(2),
        '占比(%)': item.percentage || 0
    }))

    const csv = convertToCSV(formattedData)
    downloadFile(csv, filename)
}

/**
 * 导出为JSON
 * @param {Object|Array} data - 数据
 * @param {string} filename - 文件名
 */
export function exportToJSON(data, filename = 'data.json') {
    const content = JSON.stringify(data, null, 2)
    downloadFile(content, filename, 'application/json;charset=utf-8')
}

/**
 * 导出为GeoJSON
 * @param {Array} features - 地理要素数组
 * @param {string} filename - 文件名
 */
export function exportToGeoJSON(features, filename = 'data.geojson') {
    const geojson = {
        type: 'FeatureCollection',
        features: features.map(f => ({
            type: 'Feature',
            properties: f.properties || {},
            geometry: f.geometry || null
        }))
    }

    const content = JSON.stringify(geojson, null, 2)
    downloadFile(content, filename, 'application/geo+json;charset=utf-8')
}

/**
 * 打印报表
 * @param {string} title - 标题
 * @param {Array} data - 数据
 */
export function printReport(title, data) {
    const printWindow = window.open('', '_blank')
    if (!printWindow) return

    const html = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>${title}</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 20px; }
                h1 { color: #2E7D32; border-bottom: 2px solid #2E7D32; padding-bottom: 10px; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #2E7D32; color: white; }
                tr:nth-child(even) { background-color: #f2f2f2; }
                .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: right; }
            </style>
        </head>
        <body>
            <h1>${title}</h1>
            <table>
                <thead>
                    <tr>${Object.keys(data[0] || {}).map(k => `<th>${k}</th>`).join('')}</tr>
                </thead>
                <tbody>
                    ${data.map(row => `
                        <tr>${Object.values(row).map(v => `<td>${v}</td>`).join('')}</tr>
                    `).join('')}
                </tbody>
            </table>
            <div class="footer">生成时间: ${new Date().toLocaleString('zh-CN')}</div>
        </body>
        </html>
    `

    printWindow.document.write(html)
    printWindow.document.close()
    printWindow.print()
}