<template>
  <div class="cart-shell">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input
          v-model="keyword"
          placeholder="搜索购物车商品..."
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/goods')">商品广场</el-button>
        <el-button type="text" @click="$router.push('/buy-requests')">求购广场</el-button>
        <el-button type="text" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="cart-page">
      <div class="header">
        <div>
          <h2 class="header-title">我的购物车</h2>
          <div class="header-subtitle">支持批量选择结算，数量变更自动保存</div>
        </div>
        <span class="meta-pill">共 <strong>{{ filteredList.length }}</strong> 件商品</span>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">收货地址</div>
            <el-input v-model="address" placeholder="请输入收货地址" size="small" />
          </div>
          <div class="sidebar-section">
            <el-button
              type="primary"
              size="small"
              class="apply-btn"
              :disabled="!selected.length || !address"
              @click="createOrder"
            >
              去结算
            </el-button>
            <el-button size="small" class="reset-btn" @click="$router.push('/goods')">继续逛逛</el-button>
          </div>
          <div class="sidebar-section">
            <el-button type="danger" plain size="small" class="apply-btn" :disabled="!list.length" @click="clear">
              清空购物车
            </el-button>
          </div>
        </aside>

        <main class="main-content">
          <el-card shadow="never" v-loading="loading">
            <el-table
              :data="filteredList"
              border
              stripe
              style="width: 100%"
              @selection-change="handleSelectionChange"
            >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="brandName" label="品牌" width="140">
        <template slot-scope="scope">
          {{ scope.row.brandName || '——' }}
        </template>
      </el-table-column>
      <el-table-column prop="price" label="单价" width="120">
        <template slot-scope="scope">
          ￥{{ (scope.row.price || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100">
        <template slot-scope="scope">
          {{ scope.row.stock != null ? scope.row.stock : '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="160">
        <template slot-scope="scope">
          <el-input-number
            v-model="scope.row.quantity"
            :min="1"
            :max="scope.row.stock || 9999"
            @change="onQuantityChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="140">
        <template slot-scope="scope">
          ￥{{ (scope.row.price * scope.row.quantity || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button type="text" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
            </el-table>
            <div v-if="!list.length && !loading" class="empty-text">购物车还是空的，去商品页逛逛吧～</div>
          </el-card>

          <div class="actions-bar">
            <div class="total">
              已选 {{ selected.length }} 件商品，合计：
              <span class="total-amount">￥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </main>

        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 结算汇总</div>
            <div class="panel-stat"><span>已选商品</span><b>{{ selected.length }}</b></div>
            <div class="panel-stat"><span>合计金额</span><b class="accent">￥{{ totalAmount.toFixed(2) }}</b></div>
            <div class="panel-stat"><span>地址</span><b>{{ address ? '已填写' : '未填写' }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-warning-outline" /> 温馨提示</div>
            <ul class="tips-list">
              <li>请确认地址与联系方式，避免交易失败</li>
              <li>库存实时变化，结算失败请刷新重试</li>
              <li>下单后可在“我的订单”查看进度</li>
            </ul>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchCart, updateCartItem, removeCartItem, clearCart } from '@/api/cart'
import { createOrder } from '@/api/order'
import { useUserStore } from '@/store'
import { handleError } from '@/utils/error'

export default {
  name: 'CartPage',
  data () {
    return {
      list: [],
      keyword: '',
      address: '',
      selected: []
    }
  },
  computed: {
    filteredList () {
      const kw = (this.keyword || '').trim().toLowerCase()
      let arr = Array.isArray(this.list) ? [...this.list] : []
      if (kw) {
        arr = arr.filter(i => (i.goodsName || '').toLowerCase().includes(kw))
      }
      return arr
    },
    totalAmount () {
      return this.selected.reduce((sum, item) => {
        const price = item.price || 0
        const qty = item.quantity || 0
        return sum + price * qty
      }, 0)
    }
  },
  created () {
    this.load()
  },
  methods: {
    async load () {
      const store = useUserStore()
      if (!store.user) {
        await store.fetchCurrentUser()
      }
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        const res = await fetchCart()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载购物车失败')
      }
    },
    handleSelectionChange (rows) {
      this.selected = rows || []
    },
    async onQuantityChange (row) {
      try {
        await updateCartItem(row.id, row)
        this.$message.success('数量已更新')
      } catch (e) {
        handleError(this, e, '更新数量失败')
        // 重新加载，避免前端数量与后端不一致
        this.load()
      }
    },
    async remove (id) {
      this.$confirm('确定要删除该商品吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await removeCartItem(id)
          this.$message.success('已删除')
          this.load()
        } catch (e) {
          handleError(this, e, '删除失败')
        }
      }).catch(() => {})
    },
    async clear () {
      this.$confirm('确定要清空购物车中的所有商品吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await clearCart()
          this.$message.success('购物车已清空')
          this.load()
        } catch (e) {
          handleError(this, e, '清空购物车失败')
        }
      }).catch(() => {})
    },
    async createOrder () {
      if (!this.address) {
        this.$message.error('请先填写收货地址')
        return
      }
      if (!this.selected.length) {
        this.$message.error('请先选择要结算的商品')
        return
      }
      const cartItemIds = this.selected.map(i => i.id)
      try {
        await createOrder({
          address: this.address,
          cartItemIds
        })
        this.$message.success('订单创建成功')
        this.$router.push('/orders')
      } catch (e) {
        // 后端 IllegalArgumentException/RuntimeException 文案会通过 e.message 传递到这里
        handleError(this, e, '创建订单失败')
        // 可选：重新加载购物车，防止库存变化造成前端数据不一致
        this.load()
      }
    }
  }
}
</script>

<style scoped>
.cart-shell {
  min-height: calc(100vh - 64px);
  background: var(--bg-color);
  width: 100vw;
  margin-left: calc(50% - 50vw);
}
.market-navbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: var(--card-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
}
.nav-logo {
  font-weight: 700;
  font-size: 18px;
  color: var(--primary-color);
  cursor: pointer;
}
.nav-search {
  flex: 1;
  max-width: 640px;
  margin: 0 40px;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cart-page {
  padding: 12px 18px 32px;
}
.header {
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.header-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}
.meta-pill {
  padding: 6px 12px;
  border-radius: 16px;
  background: rgba(74, 144, 217, 0.08);
  color: var(--primary-color);
}
.layout {
  display: flex;
  gap: 18px;
}
.sidebar {
  width: 280px;
  background: var(--card-bg);
  border-radius: 12px;
  padding: 16px 14px 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.sidebar-section + .sidebar-section {
  margin-top: 16px;
}
.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.apply-btn,
.reset-btn {
  width: 100%;
  display: block;
}
.reset-btn {
  margin-top: 8px;
}
.sidebar .el-button + .el-button {
  margin-left: 0;
}
.main-content {
  flex: 1;
  min-width: 0;
}
.actions-bar {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.total {
  font-size: 14px;
  color: var(--text-secondary);
}
.total-amount {
  color: var(--accent-color);
  font-weight: 600;
}
.right-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.right-card {
  border-radius: 12px;
}
.panel-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.panel-title i {
  color: var(--primary-color);
  margin-right: 6px;
}
.panel-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed var(--border-color);
  color: var(--text-secondary);
  font-size: 13px;
}
.panel-stat:last-child {
  border-bottom: none;
}
.panel-stat b {
  color: var(--primary-color);
}
.panel-stat b.accent {
  color: var(--accent-color);
}
.tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 13px;
}
.empty-text {
  text-align: center;
  padding: 16px 0;
  color: var(--text-secondary);
  font-size: 13px;
}
@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }
  .sidebar,
  .right-panel {
    width: 100%;
  }
  .market-navbar {
    padding: 0 12px;
  }
  .nav-search {
    margin: 0 10px;
    max-width: none;
  }
}
</style>

