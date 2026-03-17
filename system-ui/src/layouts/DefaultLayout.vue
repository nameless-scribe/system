<template>
  <div class="default-layout">
    <header class="top-bar">
      <div class="left">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-mark">🛒</span>
          <span class="logo-text">电商系统</span>
        </div>
        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/goods">商品</router-link>
          <router-link to="/favorites">我的收藏</router-link>
          <router-link to="/buy-requests">求购广场</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">我的订单</router-link>
        </nav>
      </div>
      <div class="auth-links">
        <template v-if="!isLogin">
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
        <template v-else>
          <el-dropdown trigger="hover">
            <span class="el-dropdown-link">
              欢迎，{{ user.username }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="$router.push('/my/items')">我发布的</el-dropdown-item>
              <el-dropdown-item @click.native="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click.native="$router.push('/orders')">我的订单</el-dropdown-item>
              <el-dropdown-item divided @click.native="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </div>
    </header>
    <main class="page-main">
      <div class="page-container">
        <slot />
      </div>
    </main>
    <footer class="footer">
      © {{ new Date().getFullYear() }} 电商管理系统 · 基于 Spring Boot & Vue
    </footer>
  </div>
</template>

<script>
import { useUserStore } from '@/store'

export default {
  name: 'DefaultLayout',
  computed: {
    user () {
      const store = useUserStore()
      return store.user || {}
    },
    isLogin () {
      const store = useUserStore()
      return store.isLogin
    }
  },
  methods: {
    async handleLogout () {
      const store = useUserStore()
      await store.logout()
      // 如果当前就在首页，就不用再跳转，避免冗余导航警告
      if (this.$route.path !== '/') {
        this.$router.push('/').catch(() => {})
      }
    }
  }
}
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}
.top-bar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
  background: linear-gradient(90deg, #ffffff, #f9fbff);
}
.left {
  display: flex;
  align-items: center;
}
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 24px;
}
.logo-mark {
  font-size: 20px;
  margin-right: 6px;
}
.logo-text {
  font-weight: 600;
  font-size: 18px;
  color: #409eff;
}
.nav-links a,
.auth-links a {
  margin: 0 8px;
  color: #606266;
  text-decoration: none;
  font-weight: 500;
}
.nav-links a.router-link-exact-active,
.auth-links a.router-link-exact-active {
  color: #409eff;
}
.auth-links {
  font-size: 14px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #606266;
}
.el-dropdown-link:hover {
  color: #409eff;
}
.page-main {
  flex: 1;
  padding: 16px 0 32px;
}
.page-container {
  width: 1200px;
  max-width: 100%;
  margin: 0 auto;
}
.footer {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #a0a0a0;
  background: #f0f2f5;
  border-top: 1px solid #ebeef5;
}
</style>

