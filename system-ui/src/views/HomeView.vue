<template>
  <div class="home-hero">
    <div class="hero-content">
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

      <el-card class="announcement-card" v-if="announcements.length">
        <div slot="header" class="card-header">
          <span>平台公告</span>
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
          <h3 class="section-title">最新上架</h3>
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
          <h3 class="section-title">热门推荐</h3>
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
    </div>
  </div>
</template>

<script>
import { fetchPublishedAnnouncements } from '@/api/announcement'
import { fetchShopGoods } from '@/api/goods'
import { useUserStore } from '@/store'
import { conditionText } from '@/utils/display'

export default {
  name: 'HomeView',
  data () {
    return {
      announcements: [],
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
  justify-content: center;
  align-items: flex-start;
  background: linear-gradient(135deg, #f0f5ff 0%, #f9f9ff 50%, #fdfdfd 100%);
}
.hero-content {
  max-width: 1400px;
  text-align: left;
  padding: 32px 20px 40px;
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
.announcement-card {
  text-align: left;
  margin: 0 0 20px;
  max-width: 720px;
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
  gap: 24px;
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
  color: #409eff;
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
</style>
