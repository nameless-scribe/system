<template>
  <div class="home-hero">
    <div class="hero-content">
      <div class="home-layout">
        <aside class="side-panel left-panel">
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-menu icon-inline" /> 快捷导航</div>
            <div class="side-links">
              <div class="side-link" @click="$router.push('/goods')">商品广场</div>
              <div class="side-link" @click="$router.push('/buy-requests')">求购广场</div>
              <div class="side-link" @click="$router.push('/favorites')">我的收藏</div>
              <div class="side-link" @click="$router.push('/cart')">购物车</div>
              <div class="side-link" @click="$router.push('/orders')">我的订单</div>
            </div>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-collection-tag icon-inline" /> 热门分类</div>
            <div class="tag-wrap">
              <el-tag size="mini">教材书籍</el-tag>
              <el-tag size="mini" type="success">电子产品</el-tag>
              <el-tag size="mini" type="warning">生活用品</el-tag>
              <el-tag size="mini" type="danger">交通工具</el-tag>
            </div>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-guide icon-inline" /> 交易流程</div>
            <ol class="step-list">
              <li>浏览商品并收藏关注</li>
              <li>加入购物车并提交订单</li>
              <li>线下当面验货并完成交易</li>
            </ol>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-info icon-inline" /> 使用说明</div>
            <ul class="tips-list compact">
              <li>发布闲置需先审核通过</li>
              <li>求购信息可点赞互动</li>
              <li>支持在个人中心管理订单</li>
            </ul>
          </el-card>
        </aside>

        <main class="center-content">
      <h1 class="hero-title">校园闲置交易平台</h1>
      <p class="hero-subtitle">
        发布、浏览与管理校园闲置物品，支持前台交易与后台审核管理。
      </p>
      <div class="hero-actions">
        <el-button type="primary" size="medium" @click="$router.push('/goods')">
          立即逛一逛
        </el-button>
        <el-button
          v-if="!isLogin"
          size="medium"
          @click="$router.push('/login')"
        >
          登录 / 注册
        </el-button>
      </div>

          <div class="quick-entries">
        <div class="entry-card" @click="$router.push('/goods')">
          <div class="entry-icon"><i class="el-icon-goods" /></div>
          <div class="entry-title">商品广场</div>
          <div class="entry-desc">浏览校园闲置，按分类筛选</div>
        </div>
        <div class="entry-card" @click="$router.push('/buy-requests')">
          <div class="entry-icon"><i class="el-icon-s-order" /></div>
          <div class="entry-title">求购广场</div>
          <div class="entry-desc">看看大家在求什么，快速对接</div>
        </div>
        <div class="entry-card" @click="$router.push('/favorites')">
          <div class="entry-icon"><i class="el-icon-star-on" /></div>
          <div class="entry-title">我的收藏</div>
          <div class="entry-desc">关注心仪商品，随时回看</div>
        </div>
        <div class="entry-card" @click="$router.push('/my/items')">
          <div class="entry-icon"><i class="el-icon-edit-outline" /></div>
          <div class="entry-title">发布闲置</div>
          <div class="entry-desc">一键发布，等待审核上架</div>
        </div>
          </div>

          <el-card class="buy-pinned-card" v-if="pinnedBuyRequests.length">
        <div slot="header" class="card-header">
          <span><i class="el-icon-top icon-inline" /> 求购广场置顶</span>
          <el-button type="text" @click="$router.push('/buy-requests')">查看全部</el-button>
        </div>
        <ul class="buy-pinned-list">
          <li
            v-for="item in pinnedBuyRequests"
            :key="item.id"
            class="buy-item"
            @click="goBuyDetail(item.id)"
          >
            <div class="buy-title">{{ item.title }}</div>
            <div class="buy-meta">
              <i class="el-icon-user-solid" />
              <span>{{ item.username || '用户' }}</span>
              <span class="dot">·</span>
              <i class="el-icon-time" />
              <span>{{ formatDateTime(item.createTime) }}</span>
              <span class="dot">·</span>
              <span>👍 {{ item.likeCount || 0 }}</span>
            </div>
          </li>
        </ul>
          </el-card>

      <el-card class="announcement-card" v-if="announcements.length">
        <div slot="header" class="card-header">
          <span><i class="el-icon-bell icon-inline" /> 平台公告</span>
        </div>
        <ul class="announcement-list">
          <li v-for="item in announcements" :key="item.id">
            <div class="ann-title">{{ item.title }}</div>
            <div class="ann-content">{{ item.content }}</div>
          </li>
        </ul>
      </el-card>

      <div class="goods-section">
        <div class="goods-block">
          <h3 class="section-title"><i class="el-icon-medal icon-inline" /> 最新上架</h3>
          <el-carousel
            v-if="latestGoods.length"
            height="220px"
            indicator-position="outside"
            :interval="4000"
          >
            <el-carousel-item
              v-for="g in latestGoods"
              :key="g.id"
            >
              <div class="goods-card carousel-card" @click="goGoodsDetail(g.id)">
                <div class="img-wrap">
                  <img v-if="g.imageUrl" :src="g.imageUrl" alt="" />
                  <div v-else class="img-placeholder">No Image</div>
                </div>
                <div class="goods-name">{{ g.name }}</div>
                <div class="goods-price">
                  ￥{{ g.price }}
                </div>
                <div class="goods-meta">
                  <span v-if="g.conditionLevel">成色：{{ conditionText(g.conditionLevel) }}</span>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="empty-tip">暂无商品</div>
        </div>

        <div class="goods-block">
          <h3 class="section-title"><i class="el-icon-hot-water icon-inline" /> 热门推荐</h3>
          <el-carousel
            v-if="hotGoods.length"
            height="220px"
            indicator-position="outside"
            :interval="4000"
          >
            <el-carousel-item
              v-for="g in hotGoods"
              :key="g.id"
            >
              <div class="goods-card carousel-card" @click="goGoodsDetail(g.id)">
                <div class="img-wrap">
                  <img v-if="g.imageUrl" :src="g.imageUrl" alt="" />
                  <div v-else class="img-placeholder">No Image</div>
                </div>
                <div class="goods-name">{{ g.name }}</div>
                <div class="goods-price">
                  ￥{{ g.price }}
                  <span class="view-count" v-if="g.viewCount != null">浏览 {{ g.viewCount }}</span>
                </div>
                <div class="goods-meta">
                  <span v-if="g.conditionLevel">成色：{{ conditionText(g.conditionLevel) }}</span>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="empty-tip">暂无数据</div>
        </div>
          </div>
        </main>

        <aside class="side-panel right-panel">
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-data-analysis icon-inline" /> 平台数据</div>
            <div class="stat-item"><span>公告数量</span><b>{{ announcements.length }}</b></div>
            <div class="stat-item"><span>置顶求购</span><b>{{ pinnedBuyRequests.length }}</b></div>
            <div class="stat-item"><span>最新上架</span><b>{{ latestGoods.length }}</b></div>
            <div class="stat-item"><span>热门推荐</span><b>{{ hotGoods.length }}</b></div>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-warning-outline icon-inline" /> 交易提醒</div>
            <ul class="tips-list">
              <li>线下当面交易，优先选择公共区域</li>
              <li>下单前确认成色、配件和价格</li>
              <li>如有纠纷可在订单页提交反馈</li>
            </ul>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-question icon-inline" /> 常见问题</div>
            <div class="faq-item" @click="$router.push('/profile')">如何修改个人资料？</div>
            <div class="faq-item" @click="$router.push('/my/items')">如何重新编辑被退回商品？</div>
            <div class="faq-item" @click="$router.push('/orders')">订单异常在哪里反馈？</div>
          </el-card>
          <el-card class="side-card" shadow="never">
            <div class="side-title"><i class="el-icon-phone-outline icon-inline" /> 联系我们</div>
            <div class="contact-row">
              <span>QQ群：</span>
              <b>123456789</b>
            </div>
            <div class="contact-row">
              <span>邮箱：</span>
              <b>service@campus.com</b>
            </div>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchPublishedAnnouncements } from '@/api/announcement'
import { fetchShopGoods } from '@/api/goods'
import { fetchBuyRequestSquare } from '@/api/buyRequest'
import { useUserStore } from '@/store'
import { conditionText } from '@/utils/display'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'HomeView',
  data () {
    return {
      announcements: [],
      pinnedBuyRequests: [],
      latestGoods: [],
      hotGoods: []
    }
  },
  computed: {
    isLogin () {
      const store = useUserStore()
      return store.isLogin
    }
  },
  created () {
    this.loadAnnouncements()
    this.loadPinnedBuyRequests()
    this.loadGoods()
  },
  methods: {
    async loadAnnouncements () {
      const res = await fetchPublishedAnnouncements()
      if (res && res.code === 200) {
        this.announcements = res.data || []
      }
    },
    conditionText,
    formatDateTime,
    async loadPinnedBuyRequests () {
      const res = await fetchBuyRequestSquare({ page: 1, size: 3 })
      if (res && res.code === 200 && res.data) {
        const pinned = Array.isArray(res.data.pinned) ? res.data.pinned : []
        const list = Array.isArray(res.data.list) ? res.data.list : []
        this.pinnedBuyRequests = (pinned.length ? pinned : list).slice(0, 3)
      }
    },
    async loadGoods () {
      // 最新上架：按时间倒序，取前 6 条
      const latestRes = await fetchShopGoods({ sort: 'latest' })
      if (latestRes && latestRes.code === 200 && Array.isArray(latestRes.data)) {
        this.latestGoods = latestRes.data.slice(0, 6)
      }
      // 热门推荐：按浏览量倒序，取前 5 条
      const hotRes = await fetchShopGoods({ sort: 'hot' })
      if (hotRes && hotRes.code === 200 && Array.isArray(hotRes.data)) {
        this.hotGoods = hotRes.data
          .filter(g => g.viewCount != null)
          .sort((a, b) => b.viewCount - a.viewCount)
          .slice(0, 5)
      }
    },
    goBuyDetail (id) {
      this.$router.push(`/buy-requests/${id}`)
    },
    goGoodsDetail (id) {
      this.$router.push(`/goods/${id}`)
    }
  }
}
</script>

<style scoped>
.home-hero {
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: stretch;
  align-items: flex-start;
  background: linear-gradient(135deg, #f0f5ff 0%, #f9f9ff 50%, #fdfdfd 100%);
  /* 脱离 DefaultLayout 的 1200 容器限制，真正铺满可视区 */
  width: 100vw;
  margin-left: calc(50% - 50vw);
}
.hero-content {
  width: 100%;
  max-width: none;
  text-align: left;
  padding: 12px 12px 28px;
}
.home-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr) 360px;
  gap: 12px;
  align-items: start;
}
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: sticky;
  top: 78px;
}
.side-card {
  border-radius: 12px;
}
.side-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.side-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.side-link {
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.side-link:hover {
  color: var(--primary-color);
  background: rgba(74, 144, 217, 0.1);
}
.tag-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.step-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.9;
}
.center-content {
  min-width: 0;
  width: 100%;
}
.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  color: var(--text-secondary);
  border-bottom: 1px dashed #ebeef5;
}
.stat-item:last-child {
  border-bottom: none;
}
.stat-item b {
  color: var(--primary-color);
}
.tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 13px;
}
.tips-list.compact {
  line-height: 1.7;
}
.faq-item {
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
}
.faq-item + .faq-item {
  margin-top: 8px;
}
.faq-item:hover {
  background: rgba(74, 144, 217, 0.1);
  color: var(--primary-color);
}
.contact-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-secondary);
  padding: 4px 0;
}
.contact-row b {
  color: var(--text-primary);
}
.hero-title {
  font-size: 34px;
  margin-bottom: 10px;
  color: #303133;
}
.hero-subtitle {
  font-size: 15px;
  color: #606266;
  margin-bottom: 20px;
}
.hero-actions {
  margin-bottom: 24px;
}
.hero-actions .el-button + .el-button {
  margin-left: 16px;
}
.quick-entries {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.entry-card {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}
.entry-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(74, 144, 217, 0.12);
  border-color: var(--primary-color);
}
.entry-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(74, 144, 217, 0.12);
  color: var(--primary-color);
  margin-bottom: 8px;
  font-size: 18px;
}
.entry-title {
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 4px;
}
.entry-desc {
  color: var(--text-secondary);
  font-size: 12px;
}
.buy-pinned-card {
  text-align: left;
  margin: 0 0 16px;
}
.buy-pinned-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.buy-item {
  padding: 8px 0;
  cursor: pointer;
}
.buy-item + .buy-item {
  border-top: 1px dashed #ebeef5;
}
.buy-item:hover .buy-title {
  color: var(--primary-color);
}
.buy-item:hover {
  background: rgba(74, 144, 217, 0.04);
  border-radius: 8px;
  padding-left: 8px;
  padding-right: 8px;
}
.buy-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.buy-meta {
  font-size: 12px;
  color: var(--text-secondary);
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.icon-inline {
  color: var(--primary-color);
  margin-right: 4px;
}
.announcement-card {
  text-align: left;
  margin: 0 0 20px;
  width: 100%;
}
.announcement-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.announcement-list li + li {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}
.ann-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.ann-content {
  font-size: 14px;
  color: #606266;
}
.goods-section {
  margin-top: 8px;
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
  justify-content: space-between;
}
.goods-block {
  flex: 1;
  max-width: none;
  background: #fff;
  border-radius: 8px;
  padding: 14px 18px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-align: left;
}
.section-title {
  margin-bottom: 8px;
  font-size: 15px;
  color: var(--primary-color);
}
.goods-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.goods-card {
  width: 220px;
  cursor: pointer;
}
.carousel-card {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.goods-card .img-wrap {
  width: 220px;
  height: 170px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 4px;
}
.goods-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.goods-name {
  margin-top: 6px;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.goods-price {
  margin-top: 2px;
  font-size: 13px;
  color: #f56c6c;
}
.goods-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}
.view-count {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.empty-tip {
  font-size: 13px;
  color: #909399;
}
@media (max-width: 992px) {
  .home-layout {
    grid-template-columns: 1fr;
  }
  .side-panel {
    position: static;
  }
  .left-panel,
  .right-panel {
    display: none;
  }
  .quick-entries {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 768px) {
  .hero-content {
    padding: 14px 10px 24px;
  }
  .quick-entries {
    grid-template-columns: 1fr;
  }
}
</style>
