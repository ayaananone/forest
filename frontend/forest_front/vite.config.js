import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import cesium from 'vite-plugin-cesium'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
  plugins: [
    vue(),
    cesium({
      rebuildCesium: false,
      devMinifyCesium: false 
    })
  ],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
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
      },
      
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
    chunkSizeWarningLimit: 4000, // 4MB
    
    rollupOptions: {
      output: {
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
      include: [
      'cesium',
      'mersenne-twister'
    ]
  }
})