import Vue from 'vue'
// @ts-ignore
import App from '@/App.vue'
import './registerServiceWorker'
import router from './router/.invoke/router'
import store from './store'
import iView from 'iview'
import 'iview/dist/styles/iview.css'

Vue.use(iView)

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
