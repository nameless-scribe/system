<template>
  <div class="admin-layout">
    <aside class="sider">
      <div class="logo">
        <span class="logo-mark">SYS</span>
        <span class="logo-text">管理后台</span>
      </div>

      <el-menu
        class="sider-menu"
        :default-active="activeMenu"
        background-color="#001529"
        text-color="#c0c4cc"
        active-text-color="#ffffff"
        :collapse="collapsed"
        :unique-opened="true"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-data-analysis" />
          <span slot="title">数据看板</span>
        </el-menu-item>

        <el-menu-item index="/admin/goods">
          <i class="el-icon-goods" />
          <span slot="title">商品管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/brands">
          <i class="el-icon-collection-tag" />
          <span slot="title">品牌管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/dicts">
          <i class="el-icon-notebook-2" />
          <span slot="title">字典管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/orders">
          <i class="el-icon-s-order" />
          <span slot="title">订单管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/users">
          <i class="el-icon-user-solid" />
          <span slot="title">用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/announcements">
          <i class="el-icon-bell" />
          <span slot="title">公告管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/reports">
          <i class="el-icon-warning-outline" />
          <span slot="title">举报管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/buy-requests">
          <i class="el-icon-chat-dot-round" />
          <span slot="title">求购管理</span>
        </el-menu-item>

        <el-submenu v-if="showSystemMenu" index="sys">
          <template slot="title">
            <i class="el-icon-setting" />
            <span>系统管理</span>
          </template>

          <el-menu-item v-if="canShowAudit" index="/admin/audit-logs">
            <i class="el-icon-document" />
            <span slot="title">系统日志</span>
          </el-menu-item>

          <el-menu-item v-if="canShowRbac" index="/admin/rbac">
            <i class="el-icon-lock" />
            <span slot="title">权限管理</span>
          </el-menu-item>
        </el-submenu>
      </el-menu>
    </aside>
    <section class="content">
      <header class="admin-header">
        <div class="header-left">
          <i class="el-icon-s-fold header-icon" @click="toggleCollapsed" />
          <span class="system-title">后台管理</span>
        </div>
        <div class="header-right">
          <template v-if="!isLogin">
            <span class="welcome">管理员未登录</span>
          </template>
          <template v-else>
            <el-dropdown trigger="hover">
              <span class="el-dropdown-link">
                欢迎，{{ user.username }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item divided @click.native="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </div>
      </header>
      <main class="admin-main">
        <slot />
      </main>
    </section>
  </div>
</template>

<script>
import { useUserStore } from '@/store'

export default {
  name: 'AdminLayout',
  data () {
    return {
      collapsed: false
    }
  },
  computed: {
    user () {
      const store = useUserStore()
      return store.user || {}
    },
    isLogin () {
      const store = useUserStore()
      return store.isLogin
    },
    activeMenu () {
      return this.$route && this.$route.path
    },
    showSystemMenu () {
      return this.canShowAudit || this.canShowRbac
    },
    canShowAudit () {
      const store = useUserStore()
      // RBAC 优先：ADMIN:AUDIT:READ；回退：管理员都显示
      if (store.hasPerm && store.hasPerm('ADMIN:AUDIT:READ')) return true
      return store.role === 'ADMIN' || store.role === 'SUPER_ADMIN'
    },
    canShowRbac () {
      const store = useUserStore()
      // RBAC 优先：ADMIN:RBAC:WRITE；回退：仅 SUPER_ADMIN 展示
      if (store.hasPerm && store.hasPerm('ADMIN:RBAC:WRITE')) return true
      return store.role === 'SUPER_ADMIN'
    }
  },
  methods: {
    toggleCollapsed () {
      this.collapsed = !this.collapsed
    },
    async handleLogout () {
      const store = useUserStore()
      await store.logout()
      // 退出后台后统一回到前台首页，避免冗余导航错误
      if (this.$route.path !== '/') {
        this.$router.push('/').catch(() => {})
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}
.sider {
  width: 240px;
  background: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-mark {
  margin-right: 8px;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(24, 144, 255, 0.95);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  letter-spacing: 1px;
}
.logo-text {
  font-size: 16px;
}
.sider-menu {
  flex: 1;
  border-right: none;
}
.sider-menu:not(.el-menu--collapse) {
  width: 240px;
}
.content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.admin-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.12);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-icon {
  cursor: pointer;
  color: #606266;
  font-size: 18px;
}
.header-icon:hover {
  color: #409eff;
}
.system-title {
  font-size: 16px;
  font-weight: 500;
}
.welcome {
  font-size: 13px;
  color: #909399;
}
.el-dropdown-link {
  cursor: pointer;
  color: #606266;
  font-size: 13px;
}
.el-dropdown-link:hover {
  color: #409eff;
}
.admin-main {
  padding: 16px 24px 24px;
  overflow-y: auto;
}
</style>

