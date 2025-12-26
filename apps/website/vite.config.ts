import path from 'path'
import process from 'process'
import vue from '@vitejs/plugin-vue'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  return {
    plugins: [
      vue(),
    ],

    server: {
      port: 5173,
      host: true
    },

    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
  }
})
