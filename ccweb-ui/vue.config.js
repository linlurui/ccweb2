/* eslint-disable */
const path = require('path')
const VueRouterInvokeWebpackPlugin = require('vue-router-invoke-webpack-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')

function resolve(dir) {
  return path.join(__dirname, dir)
}
module.exports = {
  publicPath: "./",
  chainWebpack: (config) => {
    config.resolve.alias
        .set('@', resolve('src'))
        .set('assets', resolve('src/assets'))
        .set('styles', resolve('src/styles'))
        .set('common', resolve('src/common'))
        .set('components', resolve('src/components'))
        .set('router', resolve('src/router'))
        .set('store', resolve('src/store'))
        .set('views', resolve('src/views'))
        .set('pages', resolve('src/pages'))
        .set('static', resolve('static'))
  },
  productionSourceMap: true,
  configureWebpack: {
    devtool: 'cheap-module-eval-source-map',
    optimization: {
      splitChunks: {
        chunks: 'async', // 仅提取按需载入的module
        minSize: 30000, // 提取出的新chunk在两次压缩(打包压缩和服务器压缩)之前要大于30kb
        maxSize: 0, // 提取出的新chunk在两次压缩之前要小于多少kb，默认为0，即不做限制
        minChunks: 1, // 被提取的chunk最少需要被多少chunks共同引入
        maxAsyncRequests: 5, // 最大按需载入chunks提取数
        maxInitialRequests: 3, // 最大初始同步chunks提取数
        automaticNameDelimiter: '~', // 默认的命名规则（使用~进行连接）
        name: true,
        cacheGroups: { // 缓存组配置，默认有vendors和default
          vendors: {
            test: /[\\/]node_modules[\\/]/,
            priority: -10
          },
          default: {
            minChunks: 2,
            priority: -20,
            reuseExistingChunk: true
          }
        }
      }
    },
    plugins: [
      new VueRouterInvokeWebpackPlugin({
        // 必须设置dir配置的别名
        'dir': 'src/pages',
        'alias': '@/pages',
        'routerDir': 'src/router',
        'language': 'typescript',
        'mode': 'hash',
        'redirect': [{
          redirect: '/home',
          path: '/'
        }]
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'package.json',
            to: 'package.json'
          }
        ]
      })
    ]
  }
}
