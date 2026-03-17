<template>
  <div class="page order-page">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
    </div>
    <div class="page-toolbar">
      <el-select v-model="status" placeholder="按状态筛选" clearable style="width: 260px" @change="load">
        <el-option label="待支付" :value="0" />
        <el-option label="待发货" :value="1" />
        <el-option label="已发货" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
        <el-option label="已退回" :value="5" />
      </el-select>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="orderNum" label="订单号" />
      <el-table-column prop="totalPrice" label="总金额" width="120" />
      <el-table-column prop="orderStatus" label="状态" width="140">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.orderStatus)">
            {{ statusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="addTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <el-button type="text" @click="viewDetail(scope.row.id)">详情</el-button>
          <el-dropdown @command="cmd => changeStatus(scope.row, cmd)">
            <span class="el-dropdown-link">
              修改状态<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="0">待支付</el-dropdown-item>
                <el-dropdown-item :command="1">待发货</el-dropdown-item>
                <el-dropdown-item :command="2">已发货</el-dropdown-item>
                <el-dropdown-item :command="3">已完成</el-dropdown-item>
                <el-dropdown-item :command="4">已取消</el-dropdown-item>
                <el-dropdown-item :command="5">已退回</el-dropdown-item>
              </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        layout="prev, pager, next"
        :page-size="pageSize"
        :current-page.sync="page"
        :total="total"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script>
import { fetchAdminOrders, updateOrderStatus } from '@/api/adminOrder'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { orderStatusLabel, orderStatusType } from '@/utils/status'

export default {
  name: 'AdminOrderList',
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 10,
      total: 0,
      status: null,
      orderStatusOptions: {}
    }
  },
  created () {
    this.load()
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
    async load () {
      try {
        const res = await fetchAdminOrders({
          page: this.page,
          size: this.pageSize,
          status: this.status
        })
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
    viewDetail (id) {
      this.$router.push(`/orders/${id}`)
    },
    async changeStatus (row, newStatus) {
      try {
        await updateOrderStatus(row.id, newStatus)
        this.$message.success('状态已更新')
        this.load()
      } catch (e) {
        handleError(this, e, '更新订单状态失败')
      }
    }
  }
}
</script>

<style scoped>
.order-page {
  padding: 8px 0 24px;
  text-align: left;
}
.title {
  margin-bottom: 12px;
}
.toolbar {
  margin-bottom: 10px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>

