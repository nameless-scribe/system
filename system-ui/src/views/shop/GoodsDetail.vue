<template>
  <div class="page goods-detail" v-if="goods">
    <el-card shadow="never">
      <div class="detail-layout">
        <div class="img-box">
          <img v-if="goods.imageUrl" :src="goods.imageUrl" class="goods-img">
          <div v-else class="img-placeholder">No Image</div>
        </div>
        <div class="info-box">
          <h2 class="title">{{ goods.name }}</h2>
          <p class="price">￥{{ goods.price }}</p>
          <p class="meta">
            <span v-if="brandMap[goods.brandId]">品牌：{{ brandMap[goods.brandId] }}</span>
            <span v-if="goods.conditionLevel" class="divider">|</span>
            <span v-if="goods.conditionLevel">成色：{{ conditionText(goods.conditionLevel) }}</span>
            <span v-if="goods.stock != null" class="divider">|</span>
            <span>库存：{{ goods.stock }}</span>
            <span v-if="goods.viewCount != null" class="divider">|</span>
            <span v-if="goods.viewCount != null">浏览：{{ goods.viewCount }}</span>
          </p>
          <p class="desc">{{ goods.description || '暂无商品描述' }}</p>
          <div class="actions">
            <span class="label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="maxBuyCount"
              :disabled="isSoldOut || isOwnGoods"
              size="small"
            />
            <el-button
              type="primary"
              size="small"
              :disabled="isSoldOut || isOwnGoods"
              @click="add"
              style="margin-left: 16px;"
            >
              {{ isSoldOut ? '已售罄' : (isOwnGoods ? '我的商品' : '加入购物车') }}
            </el-button>
            <el-button
              :type="isFavorited ? 'warning' : 'default'"
              size="small"
              :icon="isFavorited ? 'el-icon-star-on' : 'el-icon-star-off'"
              @click="toggleFavorite"
              style="margin-left: 10px;"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="text" size="small" @click="openReport" style="margin-left: 12px;">
              举报该商品
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" style="margin-top: 12px;">
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>卖家信息</span>
          </div>
          <div class="seller-card" v-loading="sellerLoading">
            <el-avatar :size="48" :src="sellerAvatar" icon="el-icon-user-solid" />
            <div class="seller-info">
              <div class="seller-name">
                {{ seller.username || '卖家' }}
                <el-tag v-if="rep && rep.level" size="mini" style="margin-left:8px">{{ rep.level }}</el-tag>
              </div>
              <div class="seller-sub">
                来到平台 {{ seller.joinedDays || 0 }} 天
                <span class="dot">·</span>
                信誉分 {{ rep ? (rep.score || 0) : 0 }}
                <span class="dot">·</span>
                好评率 {{ rep && rep.positiveRate != null ? `${rep.positiveRate}%` : '0%' }}
              </div>
              <div class="seller-intro">{{ profile.intro || '这个卖家还没有填写简介。' }}</div>
              <div class="seller-actions">
                <el-button type="primary" size="mini" @click="goSellerShop">进店逛逛</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>历史成交评论</span>
          </div>

          <div v-loading="commentLoading">
            <div v-if="comments.length" class="comment-list">
              <div class="comment-item" v-for="(c, idx) in comments" :key="idx">
                <div class="comment-top">
                  <span class="comment-user">{{ c.fromUsername || maskUser(c.fromUserId) }}</span>
                  <el-rate :value="c.score" disabled show-score text-color="#ff9900" score-template="{value}" />
                  <span class="comment-time">{{ formatDateTime(c.createdTime) }}</span>
                </div>
                <div class="comment-content">{{ c.comment }}</div>
              </div>
            </div>
            <div v-else class="empty-text">暂无成交评论</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="举报该商品" :visible.sync="reportDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="原因">
          <el-input
            type="textarea"
            v-model="reportReason"
            :rows="4"
            placeholder="请填写具体举报原因"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reportDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitReport">提 交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
 import { fetchShopGoodsDetail, fetchGoodsComments, fetchSellerShopInfo } from '@/api/goods'
 import { addFavorite, removeFavorite, favoriteExists } from '@/api/favorite'
import { fetchPublicBrands } from '@/api/brand'
import { addToCart } from '@/api/cart'
import { submitReport } from '@/api/report'
import { useUserStore } from '@/store'
import { conditionText } from '@/utils/display'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'GoodsDetail',
  data () {
    return {
      goods: null,
      quantity: 1,
      reportDialogVisible: false,
      reportReason: '',
      brandMap: {},
      comments: [],
      commentLoading: false,
      sellerLoading: false,
      seller: {},
      profile: {},
      rep: null
      ,
      isFavorited: false
    }
  },
  created () {
    this.loadBrands()
    this.load()
  },
  computed: {
    isSoldOut () {
      return !this.goods || Number(this.goods.stock || 0) <= 0
    },
    isOwnGoods () {
      const store = useUserStore()
      const userId = store && store.user ? Number(store.user.id) : null
      const ownerId = this.goods ? Number(this.goods.ownerId) : null
      return userId != null && ownerId != null && userId === ownerId
    },
    maxBuyCount () {
      const stock = this.goods ? Number(this.goods.stock || 0) : 0
      return stock > 0 ? stock : 1
    },
    sellerAvatar () {
      return this.profile && this.profile.avatar ? this.profile.avatar : ''
    }
  },
  methods: {
    async loadBrands () {
      try {
        const res = await fetchPublicBrands()
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(b => {
            map[b.id] = b.name
          })
          this.brandMap = map
        }
      } catch (e) {
        handleError(this, e, '加载品牌信息失败')
      }
    },
    async load () {
      const id = this.$route.params.id
      try {
        const res = await fetchShopGoodsDetail(id)
        if (res && res.code === 200) {
          this.goods = res.data
          this.loadComments()
          this.loadSeller()
          this.loadFavoriteState()
        }
      } catch (e) {
        handleError(this, e, '加载商品详情失败')
      }
    },
    async loadFavoriteState () {
      const store = useUserStore()
      if (!store.user) {
        await store.fetchCurrentUser()
      }
      if (!store.user || !this.goods || !this.goods.id) {
        this.isFavorited = false
        return
      }
      try {
        const res = await favoriteExists(this.goods.id)
        if (res && res.code === 200) {
          this.isFavorited = !!res.data
        }
      } catch (e) {
        this.isFavorited = false
      }
    },
    async loadComments () {
      if (!this.goods || !this.goods.id) return
      this.commentLoading = true
      try {
        const res = await fetchGoodsComments(this.goods.id, 10)
        if (res && res.code === 200) {
          this.comments = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载成交评论失败')
      } finally {
        this.commentLoading = false
      }
    },
    async loadSeller () {
      const ownerId = this.goods ? this.goods.ownerId : null
      if (!ownerId) return
      this.sellerLoading = true
      try {
        const res = await fetchSellerShopInfo(ownerId, 6)
        if (res && res.code === 200) {
          const data = res.data || {}
          this.seller = { sellerId: data.sellerId, username: data.username, joinedDays: data.joinedDays }
          this.profile = data.profile || {}
          this.rep = data.reputation || null
        }
      } catch (e) {
        handleError(this, e, '加载卖家信息失败')
      } finally {
        this.sellerLoading = false
      }
    },
    conditionText,
    formatDateTime,
    maskUser (uid) {
      if (!uid) return '用户'
      const s = String(uid)
      return `用户${s.slice(-4)}`
    },
    goSellerShop () {
      const ownerId = this.goods ? this.goods.ownerId : null
      if (!ownerId) return
      this.$router.push(`/seller/${ownerId}`)
    },
    async add () {
      const store = useUserStore()
      if (!store.user) {
        await store.fetchCurrentUser()
      }
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      if (this.goods && this.goods.ownerId && Number(this.goods.ownerId) === Number(store.user.id)) {
        this.$message.error('不能购买自己发布的商品')
        return
      }
      if (!this.goods || !this.goods.id) {
        this.$message.error('商品信息异常')
        return
      }
      const stock = Number(this.goods.stock || 0)
      if (stock <= 0) {
        this.$message.error('该商品已售罄')
        return
      }
      if (!this.quantity || this.quantity <= 0) {
        this.$message.error('购买数量必须大于 0')
        return
      }
      if (stock > 0 && this.quantity > stock) {
        this.$message.error(`购买数量不能超过库存（当前库存：${stock}）`)
        return
      }
      try {
        await addToCart({
          goodsId: this.goods.id,
          quantity: this.quantity
        })
        this.$message.success('已加入购物车')
      } catch (e) {
        handleError(this, e, '加入购物车失败')
      }
    },
    async toggleFavorite () {
      const store = useUserStore()
      if (!store.user) {
        await store.fetchCurrentUser()
      }
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.goods || !this.goods.id) {
        this.$message.error('商品信息异常')
        return
      }
      try {
        if (this.isFavorited) {
          await removeFavorite(this.goods.id)
          this.isFavorited = false
          this.$message.success('已取消收藏')
        } else {
          await addFavorite(this.goods.id)
          this.isFavorited = true
          this.$message.success('已收藏')
        }
      } catch (e) {
        handleError(this, e, this.isFavorited ? '取消收藏失败' : '收藏失败')
      }
    },
    openReport () {
      this.reportReason = ''
      this.reportDialogVisible = true
    },
    async submitReport () {
      const store = useUserStore()
      if (!store.user) {
        await store.fetchCurrentUser()
      }
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.reportReason) {
        this.$message.error('请填写举报原因')
        return
      }
      try {
        await submitReport({
          targetType: 'GOODS',
          targetId: this.goods.id,
          reason: this.reportReason
        })
        this.$message.success('举报已提交')
        this.reportDialogVisible = false
      } catch (e) {
        handleError(this, e, '提交举报失败')
      }
    }
  }
}
</script>

<style scoped>
.goods-detail {
  padding: 8px 0 24px;
  text-align: left;
}
.detail-layout {
  display: flex;
  gap: 24px;
}
.img-box {
  width: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.goods-img {
  width: 100%;
  height: 320px;
  object-fit: cover;
}
.img-placeholder {
  font-size: 13px;
  color: #c0c4cc;
}
.info-box {
  flex: 1;
}
.title {
  font-size: 22px;
  margin-bottom: 10px;
}
.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 600;
  margin-bottom: 8px;
}
.desc {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
  margin-bottom: 10px;
}
.meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.divider {
  margin: 0 4px;
  color: #e4e7ed;
}
.actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
}
.label {
  margin-right: 8px;
  font-size: 13px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.seller-card {
  display: flex;
  gap: 12px;
}
.seller-info {
  flex: 1;
}
.seller-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.seller-sub {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.seller-intro {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
.seller-actions {
  margin-top: 10px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}
.comment-top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.comment-user {
  font-weight: 600;
  color: #303133;
}
.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}
.comment-content {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
.empty-text {
  padding: 16px 0;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
</style>

