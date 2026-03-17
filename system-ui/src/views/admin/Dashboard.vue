<template>
  <div class="dashboard">
    <h2 class="title">数据看板</h2>
    <div class="cards">
      <el-card class="card-item">
        <div class="card-num">{{ stats.userCount }}</div>
        <div class="card-label">注册用户数</div>
      </el-card>
      <el-card class="card-item">
        <div class="card-num">{{ stats.orderCount }}</div>
        <div class="card-label">总订单数</div>
      </el-card>
      <el-card class="card-item">
        <div class="card-num">{{ stats.todayOrderCount }}</div>
        <div class="card-label">今日订单数</div>
      </el-card>
    </div>

    <el-card class="chart-card">
      <div slot="header">
        <span>各分类成交金额排行（按品牌）</span>
      </div>
      <div v-if="brandNames.length" class="chart-table">
        <el-table :data="brandSales" border stripe>
          <el-table-column prop="brandName" label="品牌/分类" />
          <el-table-column prop="totalAmount" label="成交金额" />
        </el-table>
      </div>
      <div v-else class="empty-text">
        暂无成交数据
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
import { handleError } from '@/utils/error'

export default {
  name: 'AdminDashboard',
  data () {
    return {
      stats: {
        userCount: 0,
        orderCount: 0,
        todayOrderCount: 0
      },
      brandSales: []
    }
  },
  computed: {
    brandNames () {
      return this.brandSales.map(i => i.brandName)
    }
  },
  created () {
    this.load()
  },
  methods: {
    async load () {
      try {
        const res = await request({ url: '/admin/stats/overview', method: 'get' })
        if (res && res.code === 200 && res.data) {
          this.stats.userCount = res.data.userCount || 0
          this.stats.orderCount = res.data.orderCount || 0
          this.stats.todayOrderCount = res.data.todayOrderCount || 0
          this.brandSales = res.data.brandSales || []
        }
      } catch (e) {
        handleError(this, e, '加载看板数据失败')
      }
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 8px 0 24px;
  text-align: left;
}
.title {
  margin-bottom: 12px;
}
.cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.card-item {
  flex: 1;
  text-align: center;
}
.card-num {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}
.card-label {
  font-size: 13px;
  color: #909399;
}
.chart-card {
  margin-top: 8px;
}
.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style>

