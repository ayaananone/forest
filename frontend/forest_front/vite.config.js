import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 8082,
    open: true,
    proxy: {
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
      '/geoserver': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})