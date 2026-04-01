<template>
  <div class="seller-shell">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input :value="shop.username || ''" prefix-icon="el-icon-user-solid" disabled placeholder="卖家店铺" />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/goods')">返回商品广场</el-button>
        <el-button type="text" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-button type="text" @click="$router.push('/cart')">购物车</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="seller-page" v-loading="loading">
      <div class="header">
        <div>
          <h2 class="header-title">{{ shop.username || '卖家店铺' }}</h2>
          <div class="header-subtitle">在售商品（{{ shop.onSaleCount || 0 }}）· 浏览并联系卖家完成交易</div>
        </div>
        <span class="meta-pill">好评率 <strong>{{ rep && rep.positiveRate != null ? `${rep.positiveRate}%` : '0%' }}</strong></span>
      </div>

      <div class="layout">
        <!-- 左侧：卖家档案 -->
        <aside class="left-panel">
          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-user-solid" /> 卖家档案</div>
            <div class="seller-header">
              <el-avatar :size="56" :src="sellerAvatar" icon="el-icon-user-solid" />
              <div class="seller-meta">
                <div class="seller-name">
                  {{ shop.username || '卖家' }}
                  <el-tag v-if="rep && rep.level" size="mini" style="margin-left:8px">
                    {{ rep.level }}
                  </el-tag>
                </div>
                <div class="seller-sub">
                  来到平台 {{ shop.joinedDays || 0 }} 天
                  <span class="dot">·</span>
                  信誉分 {{ rep ? (rep.score || 0) : 0 }}
                </div>
              </div>
            </div>
            <div class="seller-intro">{{ profile.intro || '这个卖家很神秘，还没有填写简介。' }}</div>
            <div class="side-actions">
              <el-button type="primary" size="mini" @click="$router.push('/buy-requests')">去求购广场</el-button>
              <el-button size="mini" @click="$router.push('/orders')">我的订单</el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-warning-outline" /> 交易提示</div>
            <ul class="tips-list">
              <li>优先当面交易，选择公共区域</li>
              <li>确认商品成色、配件与功能</li>
              <li>保留聊天记录与付款凭证</li>
            </ul>
          </el-card>
        </aside>

        <!-- 中间：在售商品 -->
        <main class="main-content">
          <el-card shadow="never" class="main-card">
            <div slot="header" class="card-header">
              <span><i class="el-icon-goods" /> 在售商品（{{ shop.onSaleCount || 0 }}）</span>
            </div>

            <div class="goods-grid" v-if="(shop.onSaleGoods || []).length">
              <div class="goods-grid-item" v-for="item in shop.onSaleGoods || []" :key="item.id">
                <el-card class="goods-card" shadow="hover" @click.native="goGoods(item.id)">
                  <div class="img-wrapper">
                    <img v-if="item.imageUrl" :src="item.imageUrl" class="goods-img">
                    <div v-else class="img-placeholder">暂无图片</div>
                  </div>
                  <div class="goods-info">
                    <h3 class="name" :title="item.name">{{ item.name }}</h3>
                    <div class="price-row">
                      <span class="price">￥{{ item.price }}</span>
                      <span class="mini-meta">库存 {{ item.stock }}</span>
                    </div>
                    <div class="meta">
                      <span>浏览 {{ item.viewCount || 0 }}</span>
                    </div>
                  </div>
                </el-card>
              </div>
            </div>
            <div v-else class="empty-text">暂无在售商品</div>
          </el-card>
        </main>

        <!-- 右侧：数据与入口 -->
        <aside class="right-panel">
          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 店铺数据</div>
            <div class="panel-stat"><span>在售数量</span><b>{{ shop.onSaleCount || 0 }}</b></div>
            <div class="panel-stat"><span>信誉分</span><b>{{ rep ? (rep.score || 0) : 0 }}</b></div>
            <div class="panel-stat"><span>好评率</span><b>{{ rep && rep.positiveRate != null ? `${rep.positiveRate}%` : '0%' }}</b></div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/goods')">继续逛商品</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看收藏</div>
            <div class="quick-link" @click="$router.push('/cart')">去购物车</div>
          </el-card>

          <el-card shadow="never" class="side-card">
            <div class="panel-title"><i class="el-icon-info" /> 说明</div>
            <div class="mini-tip">后续可在此接入“店铺公告/最近上新/联系卖家”等能力。</div>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchSellerShopInfo } from '@/api/goods'
import { handleError } from '@/utils/error'

export default {
  name: 'SellerShop',
  data () {
    return {
      loading: false,
      shop: {},
      profile: {},
      rep: null
    }
  },
  computed: {
    sellerAvatar () {
      return this.profile && this.profile.avatar ? this.profile.avatar : ''
    }
  },
  created () {
    this.load()
  },
  watch: {
    '$route.params.id' () {
      this.load()
    }
  },
  methods: {
    async load () {
      const sellerId = this.$route.params.id
      this.loading = true
      try {
        const res = await fetchSellerShopInfo(sellerId, 12)
        if (res && res.code === 200) {
          this.shop = res.data || {}
          this.profile = (this.shop && this.shop.profile) || {}
          this.rep = (this.shop && this.shop.reputation) || null
        }
      } catch (e) {
        handleError(this, e, '加载卖家店铺信息失败')
      } finally {
        this.loading = false
      }
    },
    goGoods (id) {
      this.$router.push(`/goods/${id}`)
    }
  }
}
</script>

<style scoped>
.seller-shell {
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
.seller-page {
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
.main-content {
  flex: 1;
  min-width: 0;
}
.side-card,
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
.seller-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}
.seller-meta {
  flex: 1;
  min-width: 0;
}
.seller-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.seller-sub {
  margin-top: 6px;
  font-size: 13px;
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
.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
.goods-grid-item {
  min-width: 0;
}
.goods-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}
.goods-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.14);
  transform: translateY(-4px);
}
.img-wrapper {
  height: 180px;
  background: #f7f9fc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.goods-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
}
.img-placeholder {
  font-size: 13px;
  color: var(--text-secondary);
}
.goods-info {
  padding: 10px 12px 12px;
}
.goods-info .name {
  font-size: 14px;
  margin: 0 0 8px;
  color: var(--text-primary);
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 10px;
}
.price {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent-color);
}
.mini-meta {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.meta {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}
.empty-text {
  text-align: center;
  padding: 18px 0;
  color: var(--text-secondary);
  font-size: 13px;
}
.card-header i {
  color: var(--primary-color);
  margin-right: 6px;
}
@media (max-width: 1200px) {
  .goods-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 960px) {
  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 1100px) {
  .layout {
    flex-direction: column;
  }
  .left-panel,
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

