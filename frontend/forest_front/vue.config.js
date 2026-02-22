module.exports = {
  // 前端服务端口（避免与GeoServer 8080冲突）
  devServer: {
    port: 8082,
     client: {
      overlay: false  // 完全禁用错误覆盖层
    },
    // 代理配置
    proxy: {
      // 代理GeoServer请求
      '/geoserver': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/geoserver': '/geoserver'
        },
        timeout: 30000,  // 30秒超时
        proxyTimeout: 30000
      },
      // 代理API请求
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  },
  
  // 路径别名
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src')
      }
    }
  }
}

