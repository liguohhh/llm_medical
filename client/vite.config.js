import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '^/api/.*': {
        target: 'http://localhost:8086',  // 后端服务器地址
        changeOrigin: true,               // 允许跨域
        secure: false,                    // 允许不安全的HTTPS请求
        ws: true,                         // 代理WebSocket
        configure: (proxy, options) => {
          // 添加请求头
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // 添加CSRF相关头
            proxyReq.setHeader('X-Requested-With', 'XMLHttpRequest')
            // 确保Cookie被正确传递
            proxyReq.setHeader('Cookie', req.headers.cookie || '')
          })
        }
      }
    }
  }
})
