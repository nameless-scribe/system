const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    open: false,    
    proxy: {
      '/api': {
        target: 'http://localhost:8097',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          '^/api': '/api'
        }
      },
      // 静态图片访问转发到后端，确保 /img/** 的图片路径前端也能正常显示
      '/img': {
        target: 'http://localhost:8097',
        changeOrigin: true
      }
    }
  }
})
