import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    open: true, // ✅ 修正：将 open 移到 server 对象下
    proxy: {
      '/api': {
        target: 'http://localhost:6086',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
  },
});
