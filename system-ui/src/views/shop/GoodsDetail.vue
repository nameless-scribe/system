<template>
  <div class="detail-shell" v-if="goods">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input :value="goods.name" prefix-icon="el-icon-goods" disabled />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/goods')">返回商品广场</el-button>
        <el-button type="text" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-button type="text" @click="$router.push('/cart')">购物车</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="detail-page">
      <div class="header">
        <div>
          <h2 class="header-title">{{ goods.name }}</h2>
          <div class="header-subtitle">支持收藏、加入购物车与举报；下单前请确认成色与库存</div>
        </div>
        <span class="meta-pill">浏览 <strong>{{ goods.viewCount || 0 }}</strong></span>
      </div>

      <div class="layout">
        <!-- 左侧：卖家摘要（更强调人） -->
        <aside class="left-panel">
          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-user-solid" /> 卖家摘要</div>
            <div class="seller-card" v-loading="sellerLoading">
              <el-avatar :size="52" :src="sellerAvatar" icon="el-icon-user-solid" />
              <div class="seller-info">
                <div class="seller-name">
                  {{ seller.username || '卖家' }}
                  <el-tag v-if="rep && rep.level" size="mini" style="margin-left:8px">{{ rep.level }}</el-tag>
                </div>
                <div class="seller-sub">
                  平台 {{ seller.joinedDays || 0 }} 天
                  <span class="dot">·</span>
                  信誉 {{ rep ? (rep.score || 0) : 0 }}
                  <span class="dot">·</span>
                  好评率 {{ rep && rep.positiveRate != null ? `${rep.positiveRate}%` : '0%' }}
                </div>
              </div>
            </div>
            <div class="seller-intro">{{ profile.intro || '这个卖家还没有填写简介。' }}</div>
            <div class="side-actions">
              <el-button type="primary" size="mini" @click="goSellerShop">进店逛逛</el-button>
              <el-button size="mini" @click="$router.push('/buy-requests')">去求购广场</el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-warning-outline" /> 交易提醒</div>
            <ul class="tips-list">
              <li>优先当面交易，选择公共区域</li>
              <li>确认成色、配件、功能与价格</li>
              <li>如有异常可先举报并保留证据</li>
            </ul>
          </el-card>
        </aside>

        <!-- 中间：商品核心（更强调货） -->
        <main class="main-content">
          <el-card shadow="never" class="main-card">
            <div class="detail-layout">
              <div class="img-box">
                <el-carousel
                  v-if="getImages(goods).length > 1"
                  height="380px"
                  indicator-position="outside"
                  :interval="3500"
                >
                  <el-carousel-item v-for="(src, idx) in getImages(goods)" :key="idx">
                    <img :src="src" class="goods-img" @error="onImgError($event)">
                  </el-carousel-item>
                </el-carousel>
                <img v-else-if="getImages(goods).length === 1" :src="getImages(goods)[0]" class="goods-img" @error="onImgError($event)">
                <div v-else class="img-placeholder">暂无图片</div>
              </div>
              <div class="info-box">
                <div class="price">￥{{ goods.price }}</div>
                <div class="meta">
                  <span v-if="brandMap[goods.brandId]">分类：{{ brandMap[goods.brandId] }}</span>
                  <span v-if="goods.conditionLevel" class="divider">|</span>
                  <span v-if="goods.conditionLevel">成色：{{ conditionText(goods.conditionLevel) }}</span>
                  <span v-if="goods.stock != null" class="divider">|</span>
                  <span>库存：{{ goods.stock }}</span>
                </div>
                <div class="desc">{{ goods.description || '暂无商品描述' }}</div>

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

          <el-card shadow="never" class="main-card" style="margin-top:12px;">
            <div slot="header" class="card-header">
              <span><i class="el-icon-chat-line-round" /> 历史成交评论</span>
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
        </main>

        <!-- 右侧：工具与推荐（更强调行动） -->
        <aside class="right-panel">
          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 商品信息</div>
            <div class="panel-stat"><span>库存</span><b>{{ goods.stock }}</b></div>
            <div class="panel-stat"><span>浏览</span><b>{{ goods.viewCount || 0 }}</b></div>
            <div class="panel-stat"><span>成色</span><b>{{ goods.conditionLevel ? conditionText(goods.conditionLevel) : '—' }}</b></div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/cart')">去购物车结算</div>
            <div class="quick-link" @click="$router.push('/orders')">查看我的订单</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看我的收藏</div>
            <div class="quick-link" @click="$router.push('/goods')">继续逛商品</div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-collection-tag" /> 你可能也喜欢</div>
            <div class="mini-tip">暂未接入推荐接口，可后续根据分类/热度推荐</div>
          </el-card>
        </aside>
      </div>
    </div>

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
    getImages (item) {
      const url = item && item.imageUrl ? String(item.imageUrl).trim() : ''
      if (!url) return []
      const parts = url.split(/[,;]/).map(s => s.trim()).filter(Boolean)
      return parts.length ? parts : [url]
    },
    onImgError (e) {
      const img = e && e.target
      if (!img) return
      img.onerror = null
      img.src = 'data:image/svg+xml;utf8,' + encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400">
          <rect width="100%" height="100%" fill="#F5F7FA"/>
          <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#6C757D" font-size="18">暂无图片</text>
        </svg>`
      )
    },
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
.detail-shell {
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
.detail-page {
  padding: 12px 18px 32px;
  text-align: left;
}
.header {
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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
  white-space: nowrap;
}
.layout {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}
.left-panel,
.right-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.side-card {
  border-radius: 12px;
}
.main-content {
  flex: 1;
  min-width: 0;
}
.main-card {
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
.tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 13px;
}
.mini-tip {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.7;
}
.seller-card {
  display: flex;
  gap: 12px;
  align-items: center;
}
.seller-info {
  flex: 1;
  min-width: 0;
}
.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.seller-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.seller-intro {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}
.side-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}
.detail-layout {
  display: flex;
  gap: 18px;
}
.img-box {
  flex: 0 0 520px;
  background: #f7f9fc;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.goods-img {
  width: 100%;
  height: 380px;
  object-fit: cover;
  display: block;
}
.img-placeholder {
  font-size: 13px;
  color: var(--text-secondary);
}
.info-box {
  flex: 1;
  min-width: 0;
}
.price {
  font-size: 28px;
  color: var(--accent-color);
  font-weight: 700;
  margin-bottom: 10px;
}
.meta {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.divider {
  margin: 0 4px;
  color: var(--border-color);
}
.desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 6px;
  margin-bottom: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
}
.actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.label {
  margin-right: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.card-header i {
  color: var(--primary-color);
  margin-right: 6px;
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
  color: var(--text-secondary);
}
@media (max-width: 1100px) {
  .layout {
    flex-direction: column;
  }
  .left-panel,
  .right-panel {
    width: 100%;
  }
  .img-box {
    flex: 1;
  }
  .market-navbar {
    padding: 0 12px;
  }
  .nav-search {
    margin: 0 10px;
    max-width: none;
  }
  .detail-layout {
    flex-direction: column;
  }
  .goods-img {
    height: 320px;
  }
}
</style>

