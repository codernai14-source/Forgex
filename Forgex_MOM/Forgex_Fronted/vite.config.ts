import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    fs: {
      strict: false,
      allow: [
        resolve(__dirname, '../public'),
        resolve(__dirname, '../../doc')
      ]
    },
        proxy: {
          '/api/label': {
            target: 'http://localhost:9000',
            changeOrigin: true
          },
          '/api': {
            target: 'http://localhost:9000',
            changeOrigin: true
          }
        }
  }
})
