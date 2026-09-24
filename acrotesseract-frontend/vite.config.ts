/// <reference types='vitest/config' />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  root: import.meta.dirname,
  cacheDir: '../node_modules/.vite/acrotesseract-frontend',
  server: {
    port: 4200,
    host: 'localhost',
    // The backend's local server (`nx serve acrotesseract-backend`) listens on :8080.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  preview: {
    port: 4200,
    host: 'localhost',
  },
  plugins: [react()],
  build: {
    outDir: './dist',
    emptyOutDir: true,
    reportCompressedSize: true,
    // The lazily loaded graph chunk is ~520 kB, almost all of it Cytoscape.
    chunkSizeWarningLimit: 600,
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reportsDirectory: './test-output/vitest/coverage',
    },
  },
});
