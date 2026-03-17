import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    perms: []
  }),
  getters: {
    isLogin: state => !!state.user,
    role: state => state.user?.role || 'USER',
    hasPerm: state => (permCode) => {
      if (!permCode) return true
      // 超级管理员默认拥有全部权限
      if (state.user && state.user.role === 'SUPER_ADMIN') return true
      return Array.isArray(state.perms) && state.perms.includes(permCode)
    }
  },
  actions: {
    async fetchCurrentUser () {
      try {
        const res = await request({
          url: '/auth/me',
          method: 'get'
        })
        if (res && res.code === 200) {
          this.user = res.data
          await this.fetchMyPerms()
        }
      } catch (e) {
        this.user = null
        this.perms = []
      }
    },
    async fetchMyPerms () {
      // 未登录无需拉取
      if (!localStorage.getItem('token')) {
        this.perms = []
        return
      }
      try {
        const res = await request({
          url: '/auth/myPerms',
          method: 'get'
        })
        if (res && res.code === 200) {
          this.perms = res.data || []
        }
      } catch (e) {
        // RBAC 未启用/接口异常时，保持空数组，菜单回退到 role 展示
        this.perms = []
      }
    },
    async login (form) {
      const res = await request({
        url: '/auth/login',
        method: 'post',
        params: {
          username: form.username,
          password: form.password
        }
      })
      if (res && res.code === 200 && res.data) {
        // 后端返回 { token, user }
        const { token, user } = res.data
        if (token) {
          localStorage.setItem('token', token)
        }
        this.user = user || null
        await this.fetchMyPerms()
      }
      return res
    },
    async logout () {
      try {
        await request({
          url: '/auth/logout',
          method: 'post'
        })
      } catch (e) {
        // 忽略登出异常
      }
      this.user = null
      this.perms = []
      localStorage.removeItem('token')
    }
  }
})

