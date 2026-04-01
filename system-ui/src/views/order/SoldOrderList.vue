<template>
  <div :class="embedded ? 'order-embed' : 'order-shell'">
    <div class="market-navbar" v-if="!embedded">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input v-model="keyword" placeholder="搜索订单号..." prefix-icon="el-icon-search" clearable />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/orders')">我的订单</el-button>
        <el-button type="text" @click="$router.push('/my/items')">我发布的</el-button>
        <el-button type="text" @click="$router.push('/goods')">商品广场</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="order-page">
      <div class="header">
        <div>
          <h2 class="header-title">我卖出的订单</h2>
          <div class="header-subtitle">管理卖出订单：标记发货、确认取货</div>
        </div>
        <span class="meta-pill">共 <strong>{{ total }}</strong> 条订单</span>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">状态筛选</div>
            <el-select v-model="statusFilter" placeholder="全部状态" clearable size="small" style="width:100%">
              <el-option
                v-for="(opt, key) in orderStatusOptions"
                :key="key"
                :label="opt.label"
                :value="Number(key)"
              />
            </el-select>
          </div>
          <div class="sidebar-section">
            <el-button type="primary" size="small" class="apply-btn" @click="fetchData(1)">刷新列表</el-button>
            <el-button size="small" class="reset-btn" @click="resetFilters">重置筛选</el-button>
          </div>
        </aside>

        <main class="main-content">
          <el-card shadow="never">
            <el-table :data="filteredList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNum" label="订单号" />
      <el-table-column prop="totalPrice" label="总金额" />
      <el-table-column prop="orderStatus" label="状态">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.orderStatus)">
            {{ statusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="addTime" label="下单时间" />
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <el-button type="text" @click="goDetail(scope.row.id)">详情</el-button>
          <el-button
            v-if="scope.row.orderStatus === 1"
            type="text"
            @click="onShip(scope.row)"
          >标记发货</el-button>
          <el-button
            v-if="scope.row.orderStatus === 5"
            type="text"
            style="color:#67c23a"
            @click="onPickup(scope.row)"
          >已取货</el-button>
        </template>
      </el-table-column>
    </el-table>
          </el-card>

          <div class="pager">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="pageSize"
              :current-page.sync="page"
              :total="total"
              @current-change="fetchData"
            />
          </div>
        </main>

        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 订单数据</div>
            <div class="panel-stat"><span>当前页</span><b>{{ page }}</b></div>
            <div class="panel-stat"><span>每页条数</span><b>{{ pageSize }}</b></div>
            <div class="panel-stat"><span>本页展示</span><b>{{ filteredList.length }}</b></div>
            <div class="panel-stat"><span>筛选状态</span><b>{{ statusFilter == null ? '全部' : statusText(statusFilter) }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/my/items')">管理我发布的商品</div>
            <div class="quick-link" @click="$router.push('/orders')">查看我买到的订单</div>
            <div class="quick-link" @click="$router.push('/goods')">继续逛商品</div>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchSoldOrders, shipOrder, pickupOrder } from '@/api/order'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { orderStatusLabel, orderStatusType } from '@/utils/status'

export default {
  name: 'SoldOrderList',
  props: {
    embedded: { type: Boolean, default: false }
  },
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 10,
      total: 0,
      orderStatusOptions: {},
      keyword: '',
      statusFilter: null
    }
  },
  created () {
    this.fetchData(this.page)
    this.loadOrderStatus()
  },
  computed: {
    filteredList () {
      const kw = (this.keyword || '').trim().toLowerCase()
      let arr = Array.isArray(this.list) ? [...this.list] : []
      if (kw) {
        arr = arr.filter(i => String(i.orderNum || '').toLowerCase().includes(kw))
      }
      if (this.statusFilter != null && this.statusFilter !== '') {
        arr = arr.filter(i => Number(i.orderStatus) === Number(this.statusFilter))
      }
      return arr
    }
  },
  methods: {
    async loadOrderStatus () {
      try {
        const res = await fetchDictData('order_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[Number(item.value)] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.orderStatusOptions = map
        }
      } catch (e) {
        handleError(this, e, '加载订单状态字典失败')
      }
    },
    async fetchData (page) {
      this.page = page
      try {
        const res = await fetchSoldOrders({ page: this.page, size: this.pageSize })
        if (res && res.code === 200 && res.data) {
          this.list = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        handleError(this, e, '加载订单列表失败')
      }
    },
    statusText (status) {
      return orderStatusLabel(status, this.orderStatusOptions)
    },
    statusType (status) {
      return orderStatusType(status, this.orderStatusOptions)
    },
    resetFilters () {
      this.keyword = ''
      this.statusFilter = null
    },
    goDetail (id) {
      this.$router.push(`/orders/${id}`)
    },
    async onShip (row) {
      try {
        await shipOrder(row.id)
        this.$message.success('已标记为已发货')
        this.fetchData(this.page)
      } catch (e) {
        handleError(this, e, '标记发货失败')
      }
    },
    async onPickup (row) {
      try {
        await pickupOrder(row.id)
        this.$message.success('已确认取货，订单已取消')
        this.fetchData(this.page)
      } catch (e) {
        handleError(this, e, '确认取货失败')
      }
    }
  }
}
</script>

<style scoped>
.order-shell {
  min-height: calc(100vh - 64px);
  background: var(--bg-color);
  width: 100vw;
  margin-left: calc(50% - 50vw);
}
.order-embed {
  min-height: auto;
  background: transparent;
  width: auto;
  margin-left: 0;
}
.order-embed .order-page {
  padding: 0;
}
.order-embed .header,
.order-embed .sidebar,
.order-embed .right-panel {
  display: none;
}
.order-embed .pager {
  margin-top: 12px;
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
.order-page {
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
.quick-link {
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}
.quick-link + .quick-link {
  margin-top: 8px;
}
.quick-link:hover {
  background: rgba(74, 144, 217, 0.1);
  color: var(--primary-color);
}
.pager {
  margin-top: 16px;
  text-align: right;
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

