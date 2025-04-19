module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:12345',
        changeOrigin: true
        // 移除 pathRewrite
      }
    }
  }
}