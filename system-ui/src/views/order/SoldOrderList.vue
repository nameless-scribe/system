<template>
  <div class="page order-list">
    <div class="page-header">
      <h2 class="page-title">我卖出的订单</h2>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
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
    <div class="pager">
      <el-pagination
        layout="prev, pager, next"
        :page-size="pageSize"
        :current-page.sync="page"
        :total="total"
        @current-change="fetchData"
      />
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
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 10,
      total: 0,
      orderStatusOptions: {}
    }
  },
  created () {
    this.fetchData(this.page)
    this.loadOrderStatus()
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
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>

