import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      'ant-design-vue/lib/style/index.less': resolve(__dirname, './src/styles/vendor/ant-design-vue/lib/style/index.less'),
      'ant-design-vue/lib/style/themes/default.less': resolve(__dirname, './src/styles/vendor/ant-design-vue/lib/style/themes/default.less'),
      'ant-design-vue/lib/input/style/mixin.less': resolve(__dirname, './src/styles/vendor/ant-design-vue/lib/input/style/mixin.less')
    }
  },
  server: {
    host: '0.0.0.0',
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
  },
  build: {
    target: 'es2018',
    cssCodeSplit: true,
    sourcemap: false,
    chunkSizeWarningLimit: 1500,
    reportCompressedSize: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia', 'vue-i18n'],
          'vendor-antd': ['ant-design-vue', '@ant-design/icons-vue'],
          'vendor-echarts': ['echarts', 'vue-echarts', 'apexcharts', 'vue3-apexcharts'],
          'vendor-editor': ['@wangeditor/editor', '@wangeditor/editor-for-vue'],
          'vendor-form': ['@form-create/ant-design-vue', '@formily/core', '@formily/vue', '@formily/antdv-x3'],
          'vendor-three': ['three', '@tresjs/core', '@tresjs/cientos'],
          'vendor-flow': ['@vue-flow/core', '@vue-flow/background', '@vue-flow/controls', '@vue-flow/minimap'],
          'vendor-xlsx': ['xlsx']
        }
      }
    }
  }
})
