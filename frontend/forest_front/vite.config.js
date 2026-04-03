import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import cesium from 'vite-plugin-cesium'  // 新增：Cesium 插件
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
  plugins: [
    vue(),
    cesium({
      rebuildCesium: false,  // 使用预构建版本，加快开发启动速度
      devMinifyCesium: false // 开发环境不压缩，方便调试
    })
  ],
  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      // Cesium 路径别名（可选，vite-plugin-cesium 已自动处理）
      // 'cesium': path.resolve(__dirname, 'node_modules/cesium/Source')
    },
  },
  
  // 定义全局变量，Cesium 静态资源基础路径
  define: {
    CESIUM_BASE_URL: JSON.stringify('/cesium')
  },
  
  server: {
    port: 8082,
    open: true,
    
    // 优化文件系统访问，避免 Cesium 大文件导致的性能问题
    fs: {
      strict: false,
      allow: ['..'] // 允许访问 node_modules 中的 Cesium 资源
    },
    
    proxy: {
      // 保留你现有的后端 API 代理
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode);
          });
        },
      },
      
      // 保留你现有的 GeoServer 代理
      '/geoserver': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 如果需要，可以添加路径重写（根据你的 GeoServer 实际路径调整）
        // rewrite: (path) => path.replace(/^\/geoserver/, '/geoserver')
      },
      
      // 【可选】Cesium Ion 服务代理（如果你使用 Cesium Ion 的影像或地形服务）
      '/cesium-ion': {
        target: 'https://api.cesium.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/cesium-ion/, ''),
        headers: {
          'Origin': 'https://api.cesium.com'
        }
      }
    }
  },
  
  // 构建优化配置
  build: {
    // 提高警告限制，Cesium 体积较大
    chunkSizeWarningLimit: 4000, // 4MB
    
    rollupOptions: {
      output: {
        // 手动分块，将 Cesium 单独打包
        manualChunks(id) {
          if (id.includes('node_modules/cesium')) {
            return 'cesium'
          }
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    },
    
    // 静态资源处理
    assetsDir: 'assets',
    // 确保 Cesium 的 wasm 等文件正确处理
    target: 'esnext'
  },
  
  // 优化依赖预构建
  optimizeDeps: {
    exclude: ['cesium'] // Cesium 体积大，不预构建，使用插件处理
  }
})