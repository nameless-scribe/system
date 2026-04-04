import axios from 'axios'
import router from '@/router'
import { Message } from 'element-ui'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 基础路径，会被代理到 http://localhost:8097
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    // 可以在这里添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    const res = response.data
    const silent = response && response.config && response.config.headers && response.config.headers['X-Silent-Error']

    // 如果返回的状态码不是200，则视为错误，同时给出可见提示
    if (res.code && res.code !== 200) {
      console.error('业务错误:', res.message || '请求失败')
      if (!silent) {
        Message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  error => {
    // 对响应错误做点什么
    console.error('响应错误:', error)

    const cfg = (error && (error.config || (error.response && error.response.config))) || {}
    const silent = cfg.headers && cfg.headers['X-Silent-Error']

    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 未登录或登录过期：清理登录状态并静默跳转到登录页，不再弹窗
        localStorage.removeItem('token')
        const currentPath = router.currentRoute && router.currentRoute.fullPath
        router.push({
          path: '/login',
          query: currentPath && currentPath !== '/login' ? { redirect: currentPath } : {}
        }).catch(() => {})
      } else {
        // 其它错误：记录日志并提示
        console.error('HTTP 错误状态码:', status, error.response.data)
        const apiMsg = (error.response.data && (error.response.data.message || error.response.data.msg)) || ''
        if (!silent) {
          Message.error(apiMsg || `请求出错（${status}）`)
        }
      }
    } else {
      // 网络错误：给出提示
      console.error('网络连接失败，请检查网络')
      if (!silent) {
        Message.error('网络连接失败，请检查网络')
      }
    }

    return Promise.reject(error)
  }
)

export default service
