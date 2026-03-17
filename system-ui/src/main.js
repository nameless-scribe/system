import Vue from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia, PiniaVuePlugin, setActivePinia } from 'pinia'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/styles.css'

Vue.use(PiniaVuePlugin)
const pinia = createPinia()
// 让在组件外（如路由守卫）使用 store 时也能拿到 active pinia
setActivePinia(pinia)

Vue.use(ElementUI)

Vue.config.productionTip = false

new Vue({
  router,
  pinia,
  render: h => h(App)
}).$mount('#app')
