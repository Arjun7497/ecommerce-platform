import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.js',
    coverage: {
      reporter: ['text', 'json', 'html'],
      thresholds: {
        lines: 80,
        functions: 80,
        branches: 70,
        statements: 80,
      },
    },
  },
  server: {
    proxy: {
      '/api/users': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/products': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/orders': { target: 'http://localhost:8083', changeOrigin: true },
    },
  },
})
