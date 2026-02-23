// vue.config.js

const path = require('path')

module.exports = {
  // 前端服务端口
  devServer: {
    port: 8082,
    client: {
      overlay: false
    },
    // 代理配置
    proxy: {
      // 代理 GeoServer 请求
      '/geoserver': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        timeout: 30000,
        proxyTimeout: 30000,
        onProxyReq: (proxyReq, req) => {
          console.log('Proxying:', req.method, req.url)
        }
      },
      // 代理 API 请求
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  
  // 路径别名
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
}