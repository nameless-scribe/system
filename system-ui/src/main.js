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

// 全局兜底错误提示：组件运行时错误
Vue.config.errorHandler = (err, vm, info) => {
  try {
    if (vm && vm.$message) {
      vm.$message.error((err && err.message) || '页面出错了')
    }
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('errorHandler message error:', e)
  }
  // eslint-disable-next-line no-console
  console.error(err, info)
}

// 全局兜底：未捕获的 Promise 拒绝
window.addEventListener('unhandledrejection', e => {
  const reason = e && e.reason
  const msg = (reason && reason.message) || '操作失败'
  try {
    if (Vue.prototype && Vue.prototype.$message) {
      Vue.prototype.$message.error(msg)
    }
  } catch (e2) {
    // eslint-disable-next-line no-console
    console.error('unhandledrejection message error:', e2)
  }
  // eslint-disable-next-line no-console
  console.error('unhandledrejection:', e)
})

new Vue({
  router,
  pinia,
  render: h => h(App)
}).$mount('#app')
