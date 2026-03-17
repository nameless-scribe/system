<template>
  <div class="page cart-page">
    <div class="page-header">
      <h2 class="page-title">我的购物车</h2>
    </div>
    <el-table
      :data="list"
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

    <div class="actions">
      <div class="left">
        <el-input
          v-model="address"
          placeholder="请输入收货地址"
          style="width: 320px; margin-right: 16px;"
        />
      </div>
      <div class="right">
        <div class="total">
          已选 {{ selected.length }} 件商品，合计：
          <span class="total-amount">￥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <el-button
          type="primary"
          @click="createOrder"
          :disabled="!selected.length || !address"
        >
          去结算
        </el-button>
        <el-button @click="clear" :disabled="!list.length">清空购物车</el-button>
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
      address: '',
      selected: []
    }
  },
  computed: {
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
.cart-page {
}
.actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.actions .right {
  display: flex;
  align-items: center;
}
.total {
  margin-right: 16px;
  font-size: 14px;
}
.total-amount {
  color: #f56c6c;
  font-weight: 600;
}
</style>

