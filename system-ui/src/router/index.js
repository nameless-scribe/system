import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue')
  },
  {
    path: '/admin/goods',
    name: 'adminGoods',
    component: () => import('../views/admin/GoodsList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/dashboard',
    name: 'adminDashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/brands',
    name: 'adminBrands',
    component: () => import('../views/admin/BrandList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/dicts',
    name: 'adminDicts',
    component: () => import('../views/admin/DictManage.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/users',
    name: 'adminUsers',
    component: () => import('../views/admin/UserList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/announcements',
    name: 'adminAnnouncements',
    component: () => import('../views/admin/AnnouncementList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/orders',
    name: 'adminOrders',
    component: () => import('../views/admin/AdminOrderList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/reports',
    name: 'adminReports',
    component: () => import('../views/admin/ReportList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/buy-requests',
    name: 'adminBuyRequests',
    component: () => import('../views/admin/BuyRequestManage.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/audit-logs',
    name: 'adminAuditLogs',
    component: () => import('../views/admin/AuditLogList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/admin/rbac',
    name: 'adminRbac',
    component: () => import('../views/admin/RbacManage.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'], layout: 'admin' }
  },
  {
    path: '/orders',
    name: 'orderList',
    component: () => import('../views/order/OrderList.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/orders/:id',
    name: 'orderDetail',
    component: () => import('../views/order/OrderDetail.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('../views/cart/Cart.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('../views/user/Favorites.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/buy-requests',
    name: 'buyRequestSquare',
    component: () => import('../views/buy/BuyRequestSquare.vue')
  },
  {
    path: '/buy-requests/:id',
    name: 'buyRequestDetail',
    component: () => import('../views/buy/BuyRequestDetail.vue')
  },
  {
    path: '/goods',
    name: 'shopGoods',
    component: () => import('../views/shop/GoodsList.vue')
  },
  {
    path: '/goods/:id',
    name: 'goodsDetail',
    component: () => import('../views/shop/GoodsDetail.vue')
  },
  {
    path: '/seller/:id',
    name: 'sellerShop',
    component: () => import('../views/shop/SellerShop.vue')
  },
  {
    path: '/profile',
    name: 'userCenter',
    component: () => import('../views/user/UserCenter.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/my/items',
    name: 'myItems',
    component: () => import('../views/user/MyItems.vue'),
    meta: { requiresAuth: true, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/user/Login.vue'),
    meta: { layout: 'auth' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/user/Register.vue'),
    meta: { layout: 'auth' }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach(async (to, from, next) => {
  const store = useUserStore()
  if (!store.user) {
    await store.fetchCurrentUser()
  }

  if (to.meta && to.meta.requiresAuth) {
    if (!store.isLogin) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
    if (to.meta.roles && !to.meta.roles.includes(store.role)) {
      // 没权限，返回首页
      return next('/')
    }
  }
  next()
})

export default router
