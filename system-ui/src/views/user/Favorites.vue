<template>
  <div class="page fav-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的收藏</h2>
        <div class="page-subtitle">收藏的商品会在这里展示</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="搜索商品名/卖家" clearable style="width: 220px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="有效收藏" value="valid" />
          <el-option label="无效收藏" value="invalid" />
        </el-select>
        <el-select v-model="query.sort" placeholder="排序" style="width: 160px">
          <el-option label="收藏时间(新)" value="timeDesc" />
          <el-option label="收藏时间(旧)" value="timeAsc" />
          <el-option label="价格(低->高)" value="priceAsc" />
          <el-option label="价格(高->低)" value="priceDesc" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="applyFilter">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilter">重置</el-button>

        <div style="flex:1"></div>
        <el-button type="danger" plain :disabled="!multipleSelection.length" @click="onBatchRemove">
          批量取消（{{ multipleSelection.length }}）
        </el-button>
        <el-button type="warning" plain icon="el-icon-delete" @click="onCleanupInvalid">
          清理无效收藏
        </el-button>
      </div>

      <el-table
        :data="filteredList"
        border
        stripe
        style="width: 100%"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column label="商品" min-width="320">
          <template slot-scope="scope">
            <div class="goods-cell" @click="goGoods(scope.row.goodsId)">
              <img v-if="scope.row.imageUrl" :src="scope.row.imageUrl" class="thumb" />
              <div v-else class="thumb placeholder">No Image</div>
              <div class="goods-meta">
                <div class="name">{{ scope.row.goodsName || `商品#${scope.row.goodsId}` }}</div>
                <div class="sub">
                  收藏时间：{{ formatDateTime(scope.row.createTime) }}
                  <span v-if="scope.row.brandName" class="dot">·</span>
                  <span v-if="scope.row.brandName">{{ scope.row.brandName }}</span>
                </div>
                <div class="tags">
                  <el-tag v-if="isInvalid(scope.row)" size="mini" type="danger">无效</el-tag>
                  <el-tag v-else size="mini" type="success">有效</el-tag>
                  <el-tag v-if="scope.row.deleteStatus === 1" size="mini" type="danger">已删除</el-tag>
                  <el-tag v-else-if="scope.row.goodsStatus === 0" size="mini" type="info">已下架</el-tag>
                  <el-tag v-else-if="scope.row.auditStatus != null && scope.row.auditStatus !== 1" size="mini" type="warning">未审核</el-tag>
                  <el-tag v-if="Number(scope.row.stock || 0) <= 0" size="mini" type="info">已售罄</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="卖家/信誉" width="220">
          <template slot-scope="scope">
            <div class="seller-cell" @click="goSeller(scope.row.ownerId)">
              <div class="seller-name">{{ scope.row.sellerUsername || `用户${String(scope.row.ownerId || '').slice(-4)}` }}</div>
              <div class="seller-sub">
                信誉 {{ scope.row.sellerRepScore || 0 }}
                <span class="dot">·</span>
                {{ scope.row.sellerRepLevel || '-' }}
                <span class="dot">·</span>
                好评率 {{ scope.row.sellerPositiveRate != null ? `${scope.row.sellerPositiveRate}%` : '0%' }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template slot-scope="scope">￥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="text" style="color:red" @click="onRemove(scope.row.goodsId)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!list.length && !loading" class="empty-text">暂无收藏，去商品页逛逛吧～</div>
    </el-card>
  </div>
</template>

<script>
import { fetchFavorites, removeFavorite, removeFavoriteBatch, cleanupInvalidFavorites } from '@/api/favorite'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'UserFavorites',
  data () {
    return {
      loading: false,
      list: [],
      multipleSelection: [],
      query: {
        keyword: '',
        status: '',
        sort: 'timeDesc'
      }
    }
  },
  created () {
    this.load()
  },
  computed: {
    filteredList () {
      let arr = Array.isArray(this.list) ? [...this.list] : []
      const kw = (this.query.keyword || '').trim().toLowerCase()
      if (kw) {
        arr = arr.filter(i => {
          const name = (i.goodsName || '').toLowerCase()
          const seller = (i.sellerUsername || '').toLowerCase()
          return name.includes(kw) || seller.includes(kw)
        })
      }
      if (this.query.status === 'valid') {
        arr = arr.filter(i => !this.isInvalid(i))
      } else if (this.query.status === 'invalid') {
        arr = arr.filter(i => this.isInvalid(i))
      }
      const sort = this.query.sort
      arr.sort((a, b) => {
        if (sort === 'timeAsc') return new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
        if (sort === 'priceAsc') return Number(a.price || 0) - Number(b.price || 0)
        if (sort === 'priceDesc') return Number(b.price || 0) - Number(a.price || 0)
        // 默认 timeDesc
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      })
      return arr
    }
  },
  methods: {
    formatDateTime,
    isInvalid (row) {
      if (!row) return true
      if (!row.goodsId) return true
      if (row.deleteStatus === 1) return true
      if (row.goodsStatus === 0) return true
      if (row.auditStatus != null && row.auditStatus !== 1) return true
      return false
    },
    async load () {
      this.loading = true
      try {
        const res = await fetchFavorites()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载收藏列表失败')
      } finally {
        this.loading = false
      }
    },
    onSelectionChange (rows) {
      this.multipleSelection = rows || []
    },
    applyFilter () {},
    resetFilter () {
      this.query = { keyword: '', status: '', sort: 'timeDesc' }
    },
    goGoods (id) {
      this.$router.push(`/goods/${id}`)
    },
    goSeller (id) {
      if (!id) return
      this.$router.push(`/seller/${id}`)
    },
    onRemove (goodsId) {
      this.$confirm('确定取消收藏该商品吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await removeFavorite(goodsId)
            this.$message.success('已取消收藏')
            await this.load()
          } catch (e) {
            handleError(this, e, '取消收藏失败')
          }
        })
        .catch(() => {})
    },
    onBatchRemove () {
      const goodsIds = (this.multipleSelection || []).map(i => i.goodsId).filter(Boolean)
      if (!goodsIds.length) return
      this.$confirm(`确定批量取消收藏（${goodsIds.length} 条）吗？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await removeFavoriteBatch(goodsIds)
            if (res && res.code === 200) {
              this.$message.success(`已取消 ${res.data || 0} 条`)
            } else {
              this.$message.success('已取消')
            }
            this.multipleSelection = []
            await this.load()
          } catch (e) {
            handleError(this, e, '批量取消失败')
          }
        })
        .catch(() => {})
    },
    onCleanupInvalid () {
      this.$confirm('确定清理无效收藏吗？（已删除/下架/未审核的商品收藏会被移除）', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await cleanupInvalidFavorites()
            if (res && res.code === 200) {
              this.$message.success(`已清理 ${res.data || 0} 条`)
            } else {
              this.$message.success('已清理')
            }
            await this.load()
          } catch (e) {
            handleError(this, e, '清理失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
  cursor: pointer;
}
.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
}
.thumb.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #c0c4cc;
}
.name {
  font-weight: 600;
  color: #303133;
}
.sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.tags {
  margin-top: 6px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.seller-cell {
  cursor: pointer;
}
.seller-name {
  font-weight: 600;
  color: #303133;
}
.seller-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.empty-text {
  text-align: center;
  padding: 16px 0;
  color: #909399;
  font-size: 13px;
}
</style>

