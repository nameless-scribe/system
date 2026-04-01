<template>
  <div class="favorites-page">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input
          v-model="query.keyword"
          placeholder="搜索收藏商品名/卖家..."
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/goods')">商品广场</el-button>
        <el-button type="text" @click="$router.push('/buy-requests')">求购广场</el-button>
        <el-button type="primary" @click="$router.push('/my/items')">发布闲置</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="fav-content">
      <div class="header">
        <div>
          <h2 class="header-title">我的收藏</h2>
          <p class="header-subtitle">收藏的商品会在这里展示，可筛选、批量取消与清理无效收藏</p>
        </div>
        <div class="header-meta">
          <span class="meta-pill">共 <strong>{{ filteredList.length }}</strong> 条收藏</span>
        </div>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">状态筛选</div>
            <el-select v-model="query.status" placeholder="状态" clearable size="small" style="width:100%">
              <el-option label="有效收藏" value="valid" />
              <el-option label="无效收藏" value="invalid" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <div class="sidebar-title">排序方式</div>
            <el-select v-model="query.sort" placeholder="排序" size="small" style="width:100%">
              <el-option label="收藏时间(新)" value="timeDesc" />
              <el-option label="收藏时间(旧)" value="timeAsc" />
              <el-option label="价格(低->高)" value="priceAsc" />
              <el-option label="价格(高->低)" value="priceDesc" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <el-button type="primary" size="small" class="apply-btn" @click="applyFilter">查询</el-button>
            <el-button size="small" class="reset-btn" @click="resetFilter">重置</el-button>
          </div>
          <div class="sidebar-section">
            <el-button type="danger" plain size="small" class="apply-btn" :disabled="!multipleSelection.length" @click="onBatchRemove">
              批量取消（{{ multipleSelection.length }}）
            </el-button>
            <el-button type="warning" plain size="small" class="reset-btn" @click="onCleanupInvalid">
              清理无效收藏
            </el-button>
          </div>
        </aside>

        <main class="main-content">
          <el-card shadow="never" v-loading="loading">
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
        </main>

        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 收藏数据</div>
            <div class="panel-stat"><span>总收藏</span><b>{{ list.length }}</b></div>
            <div class="panel-stat"><span>有效收藏</span><b>{{ validCount }}</b></div>
            <div class="panel-stat"><span>无效收藏</span><b>{{ invalidCount }}</b></div>
            <div class="panel-stat"><span>已选择</span><b>{{ multipleSelection.length }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/goods')">去商品广场继续逛</div>
            <div class="quick-link" @click="$router.push('/cart')">查看购物车</div>
            <div class="quick-link" @click="$router.push('/orders')">查看我的订单</div>
            <div class="quick-link" @click="$router.push('/buy-requests')">浏览求购广场</div>
          </el-card>
        </aside>
      </div>
    </div>
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
    },
    validCount () {
      return (this.list || []).filter(i => !this.isInvalid(i)).length
    },
    invalidCount () {
      return (this.list || []).filter(i => this.isInvalid(i)).length
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
.favorites-page {
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
.fav-content {
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
  box-sizing: border-box;
}
.reset-btn {
  margin-top: 8px;
}
/* 覆盖 Element 默认的 .el-button + .el-button 左间距，避免同列按钮出现“看起来变窄” */
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

