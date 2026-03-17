<template>
  <div class="page ratings-page">
    <div class="page-header">
      <h2 class="page-title">我收到的评价</h2>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column label="评分" width="160">
        <template slot-scope="scope">
          <el-rate :value="scope.row.score" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评语" />
      <el-table-column label="时间" width="180">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.createdTime) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { fetchReceivedRatings } from '@/api/rating'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'ReceivedRatings',
  data () {
    return {
      list: []
    }
  },
  created () {
    this.load()
  },
  methods: {
    formatDateTime,
    async load () {
      try {
        const res = await fetchReceivedRatings()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载评价列表失败')
      }
    }
  }
}
</script>

<style scoped>
.ratings-page {
  padding: 8px 0 24px;
}
</style>

