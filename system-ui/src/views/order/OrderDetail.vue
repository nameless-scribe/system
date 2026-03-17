<template>
  <div class="page order-detail">
    <div class="page-header">
      <h2 class="page-title">订单详情</h2>
    </div>
    <el-card v-if="order" class="base-info" shadow="never">
      <p><strong>订单号：</strong>{{ order.orderNum }}</p>
      <p><strong>收货地址：</strong>{{ order.address }}</p>
      <p><strong>总金额：</strong>{{ order.totalPrice }}</p>
      <p><strong>状态：</strong>{{ statusText(order.orderStatus) }}</p>
      <p><strong>下单时间：</strong>{{ order.addTime }}</p>
    </el-card>

    <h3 class="sub-title">商品列表</h3>
    <el-table :data="details" border stripe style="width: 100%">
      <el-table-column prop="goodsName" label="商品名称" />
      <el-table-column prop="count" label="数量" width="100" />
      <el-table-column prop="price" label="单价" width="120" />
    </el-table>

    <div class="rate-block" v-if="order">
      <el-button type="primary" size="small" @click="openRateDialog">评价交易对象</el-button>
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
import { addRating } from '@/api/rating'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { orderStatusLabel } from '@/utils/status'

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
      const id = this.$route.params.id
      try {
        const res = await fetchOrderDetail(id)
        if (res && res.code === 200 && res.data) {
          this.order = res.data.order
          this.details = res.data.details || []
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
        await addRating({
          orderId: this.order.id,
          toUserId: this.order.userId, // 简化：评价买家，后续可扩展为切换角色
          score: this.rateForm.score,
          comment: this.rateForm.comment
        })
        this.$message.success('评价已提交')
        this.rateDialogVisible = false
      } catch (e) {
        handleError(this, e, '提交评价失败')
      }
    }
  }
}
</script>

<style scoped>
.base-info {
  margin-bottom: 16px;
}
.sub-title {
  margin: 8px 0;
}
.rate-block {
  margin-top: 16px;
}
</style>

