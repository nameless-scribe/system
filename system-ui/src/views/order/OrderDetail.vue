<template>
  <div class="order-shell">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input :value="order ? order.orderNum : ''" placeholder="订单号" prefix-icon="el-icon-document" disabled />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/orders')">返回订单列表</el-button>
        <el-button type="text" @click="$router.push('/goods')">商品广场</el-button>
        <el-button type="text" @click="$router.push('/cart')">购物车</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="order-page">
      <div class="header">
        <div>
          <h2 class="header-title">订单详情</h2>
          <div class="header-subtitle">查看订单信息与商品明细</div>
        </div>
      </div>

      <div class="layout">
        <main class="main-content">
          <el-card v-if="order" class="base-info" shadow="never">
            <div class="info-grid">
              <div class="info-item"><span>订单号</span><b>{{ order.orderNum }}</b></div>
              <div class="info-item"><span>状态</span><b>{{ statusText(order.orderStatus) }}</b></div>
              <div class="info-item"><span>总金额</span><b class="accent">￥{{ order.totalPrice }}</b></div>
              <div class="info-item"><span>下单时间</span><b>{{ formatTime(order.addTime) }}</b></div>
              <div class="info-item full"><span>收货地址</span><b>{{ order.address }}</b></div>
            </div>
          </el-card>

          <el-card shadow="never" style="margin-top:12px;">
            <div slot="header" class="card-header">
              <span><i class="el-icon-goods" /> 商品列表</span>
            </div>
            <el-table :data="details" border stripe style="width: 100%">
              <el-table-column prop="goodsName" label="商品名称" />
              <el-table-column prop="count" label="数量" width="100" />
              <el-table-column prop="price" label="单价" width="120" />
            </el-table>
          </el-card>

          <div class="rate-block" v-if="order">
            <el-button type="primary" size="small" @click="openRateDialog">{{ rateBtnText }}</el-button>
          </div>
        </main>

        <aside class="right-panel">
          <el-card shadow="never" class="right-card" v-if="order">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 订单摘要</div>
            <div class="panel-stat"><span>商品条目</span><b>{{ details.length }}</b></div>
            <div class="panel-stat"><span>总数量</span><b>{{ totalCount }}</b></div>
            <div class="panel-stat"><span>总金额</span><b class="accent">￥{{ order.totalPrice }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/orders')">回到订单列表</div>
            <div class="quick-link" @click="$router.push('/goods')">继续逛商品</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看收藏</div>
          </el-card>
        </aside>
      </div>
    </div>

    <el-dialog title="评价交易对象" :visible.sync="rateDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="rateForm.score" :max="5" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input type="textarea" v-model="rateForm.comment" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rateDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitRate">提 交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { fetchOrderDetail } from '@/api/order'
import { addRating, updateMyRating, getMyRatingByOrder } from '@/api/rating'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { orderStatusLabel } from '@/utils/status'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'OrderDetail',
  data () {
    return {
      order: null,
      details: [],
      rateDialogVisible: false,
      rateForm: {
        score: 5,
        comment: ''
      },
      myRating: null,
      orderStatusOptions: {}
    }
  },
  computed: {
    totalCount () {
      return (this.details || []).reduce((sum, d) => sum + Number(d.count || 0), 0)
    },
    rateBtnText () {
      return this.myRating ? '编辑评价' : '评价交易对象'
    }
  },
  created () {
    this.load()
    this.loadOrderStatus()
  },
  methods: {
    formatTime (v) {
      return formatDateTime(v)
    },
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
    async load () {
      const id = this.$route.params.id
      try {
        const res = await fetchOrderDetail(id)
        if (res && res.code === 200 && res.data) {
          this.order = res.data.order
          this.details = res.data.details || []
          // 查询我是否已评价该订单
          try {
            const r = await getMyRatingByOrder(this.order.id)
            if (r && r.code === 200) {
              this.myRating = r.data || null
              if (this.myRating) {
                this.rateForm.score = this.myRating.score || 5
                this.rateForm.comment = this.myRating.comment || ''
              }
            }
          } catch (e) {
            // 静默
          }
        }
      } catch (e) {
        handleError(this, e, '加载订单详情失败')
      }
    },
    statusText (status) {
      return orderStatusLabel(status, this.orderStatusOptions)
    },
    openRateDialog () {
      this.rateDialogVisible = true
    },
    async submitRate () {
      if (!this.rateForm.score) {
        this.$message.error('请先评分')
        return
      }
      try {
        if (this.myRating) {
          await updateMyRating({
            orderId: this.order.id,
            score: this.rateForm.score,
            comment: this.rateForm.comment
          })
          this.$message.success('评价已更新')
        } else {
          await addRating({
            orderId: this.order.id,
            toUserId: this.order.userId, // 简化：评价买家，后续可扩展为切换角色
            score: this.rateForm.score,
            comment: this.rateForm.comment
          })
          this.$message.success('评价已提交')
        }
        this.rateDialogVisible = false
        // 刷新 myRating 状态
        const r = await getMyRatingByOrder(this.order.id)
        this.myRating = r && r.code === 200 ? (r.data || null) : null
      } catch (e) {
        handleError(this, e, '提交评价失败')
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
.layout {
  display: flex;
  gap: 18px;
}
.main-content {
  flex: 1;
  min-width: 0;
}
.base-info {
  border-radius: 12px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #f8fbff;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}
.info-item.full {
  grid-column: 1 / -1;
}
.info-item span {
  flex-shrink: 0;
}
.info-item b {
  color: var(--text-primary);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info-item b.accent {
  color: var(--accent-color);
}
.card-header i {
  color: var(--primary-color);
  margin-right: 6px;
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
.rate-block {
  margin-top: 12px;
}
@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }
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
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

