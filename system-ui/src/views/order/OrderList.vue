<template>
  <div class="page order-list">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
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
      <el-table-column label="操作" width="320">
        <template slot-scope="scope">
          <el-button type="text" @click="goDetail(scope.row.id)">详情</el-button>
          <el-button
            v-if="scope.row.orderStatus === 2"
            type="text"
            @click="onComplete(scope.row)"
          >确认收货</el-button>
          <el-button
            v-if="scope.row.orderStatus === 2"
            type="text"
            style="color:#e6a23c"
            @click="openReturnDialog(scope.row)"
          >退回</el-button>
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

    <el-dialog
      title="申请退回"
      :visible.sync="returnDialogVisible"
      width="420px"
    >
      <el-form label-width="80px">
        <el-form-item label="退回原因">
          <el-input
            type="textarea"
            v-model="returnForm.reason"
            :rows="3"
            placeholder="请填写退回原因"
          />
        </el-form-item>
        <el-form-item label="取货地址">
          <el-input
            v-model="returnForm.pickupAddress"
            placeholder="请填写卖家上门取货或快递上门地址"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="returnDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReturn">提 交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { fetchUserOrders, completeOrder, returnOrder } from '@/api/order'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { orderStatusLabel, orderStatusType } from '@/utils/status'

export default {
  name: 'OrderList',
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 10,
      total: 0,
      orderStatusOptions: {},
      returnDialogVisible: false,
      returnForm: {
        orderId: null,
        reason: '',
        pickupAddress: ''
      }
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
        const res = await fetchUserOrders({ page: this.page, size: this.pageSize })
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
    async onComplete (row) {
      try {
        await completeOrder(row.id)
        this.$message.success('订单已完成')
        this.fetchData(this.page)
      } catch (e) {
        handleError(this, e, '确认收货失败')
      }
    },
    openReturnDialog (row) {
      this.returnForm.orderId = row.id
      this.returnForm.reason = ''
      this.returnForm.pickupAddress = ''
      this.returnDialogVisible = true
    },
    async submitReturn () {
      if (!this.returnForm.reason || !this.returnForm.pickupAddress) {
        this.$message.error('请填写退回原因和取货地址')
        return
      }
      try {
        await returnOrder(this.returnForm.orderId, {
          reason: this.returnForm.reason,
          pickupAddress: this.returnForm.pickupAddress
        })
        this.$message.success('退回申请已提交')
        this.returnDialogVisible = false
        this.fetchData(this.page)
      } catch (e) {
        handleError(this, e, '提交退回申请失败')
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

