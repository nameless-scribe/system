<template>
  <div class="market-page">
    <!-- 顶部导航栏 -->
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">
        校园易物
      </div>
      <div class="nav-search">
        <el-input
          v-model="keyword"
          placeholder="搜索教材、电子产品、生活用品..."
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/buy-requests')">求购广场</el-button>
        <el-button type="text" @click="$router.push('/favorites')">收藏</el-button>
        <el-button type="primary" @click="$router.push('/my/items')">发布闲置</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="shop-goods">
      <!-- 内容头部：标题 + 统计 -->
      <div class="header">
        <div class="header-text">
          <h2 class="header-title">校园精选闲置</h2>
          <p class="header-subtitle">发现同学们发布的教材、电子产品与生活好物</p>
        </div>
        <div class="header-meta">
          <span class="meta-pill">
            共 <strong>{{ filteredList.length }}</strong> 件商品
          </span>
        </div>
      </div>

      <div class="layout">
        <!-- 左侧筛选栏 -->
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">商品分类</div>
            <el-select
              v-model="query.brandId"
              placeholder="选择分类"
              clearable
              size="small"
              class="category-select"
              @change="onSelectBrand"
            >
              <el-option label="全部分类" :value="null" />
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title">价格区间</div>
            <div class="price-inputs">
              <el-input
                v-model.number="query.minPrice"
                placeholder="最低价"
                size="small"
              />
              <span class="price-sep">-</span>
              <el-input
                v-model.number="query.maxPrice"
                placeholder="最高价"
                size="small"
              />
            </div>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title">成色筛选</div>
            <el-radio-group
              v-model="query.conditionLevel"
              size="small"
              class="condition-group"
            >
              <el-radio-button :label="null">不限</el-radio-button>
              <el-radio-button :label="1">全新</el-radio-button>
              <el-radio-button :label="2">9成新</el-radio-button>
              <el-radio-button :label="3">8成新</el-radio-button>
              <el-radio-button :label="4">7成新及以下</el-radio-button>
            </el-radio-group>
          </div>

          <div class="sidebar-section">
            <div class="sidebar-title">排序方式</div>
            <el-radio-group
              v-model="query.sort"
              size="small"
              class="sort-group"
            >
              <el-radio-button label="latest">最新发布</el-radio-button>
              <el-radio-button label="priceAsc">价格从低到高</el-radio-button>
              <el-radio-button label="priceDesc">价格从高到低</el-radio-button>
              <el-radio-button label="hot">最受关注</el-radio-button>
            </el-radio-group>
          </div>

          <el-button type="primary" size="small" class="apply-btn" @click="load">
            应用筛选
          </el-button>
        </aside>

        <!-- 中间主内容区 -->
        <main class="main-content">
          <div class="goods-grid">
            <div class="goods-grid-item" v-for="item in pagedList" :key="item.id">
              <el-card
                class="goods-card"
                :class="{ 'sold-out-card': Number(item.stock || 0) <= 0 }"
                shadow="hover"
                @click.native="onCardClick(item)"
              >
                <!-- 商品主图（支持多图轮播：imageUrl 里用 , 或 ; 分隔） -->
                <div class="img-wrapper">
                  <el-carousel
                    v-if="getImages(item).length > 1"
                    height="220px"
                    indicator-position="none"
                    :interval="3500"
                  >
                    <el-carousel-item v-for="(src, idx) in getImages(item)" :key="idx">
                      <img :src="src" class="goods-img" @error="onImgError($event)">
                    </el-carousel-item>
                  </el-carousel>
                  <img
                    v-else-if="getImages(item).length === 1"
                    :src="getImages(item)[0]"
                    class="goods-img"
                    @error="onImgError($event)"
                  >
                  <div v-else class="img-placeholder">暂无图片</div>

                  <span class="category-pill">
                    {{ brandMap[item.brandId] || '未分类' }}
                  </span>
                  <el-tag
                    v-if="Number(item.stock || 0) <= 0"
                    type="info"
                    size="mini"
                    class="sold-out-tag"
                  >已售罄</el-tag>
                </div>

                <!-- 文案区 -->
                <div class="goods-info">
                  <h3 class="name" :title="item.name">{{ item.name }}</h3>

                  <div class="meta-row price-row">
                    <span class="meta-icon">💰</span>
                    <span class="price">￥{{ item.price }}</span>
                  </div>

                  <div class="meta-row">
                    <span class="meta-icon">📍</span>
                    <span class="meta-text">位置：—</span>
                  </div>

                  <div class="meta-row">
                    <span class="meta-icon">⭐</span>
                    <span class="meta-text">成色：{{ item.conditionLevel ? conditionText(item.conditionLevel) : '—' }}</span>
                  </div>

                  <div class="bottom-row">
                    <div class="seller">
                      <span class="meta-icon">👤</span>
                      <span class="meta-text">{{ item.ownerId ? `用户${String(item.ownerId).slice(-4)}` : '卖家' }}</span>
                    </div>
                    <div class="time">
                      <span class="meta-icon">🕐</span>
                      <span class="meta-text">—</span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <!-- 分页导航 -->
          <div class="pagination" v-if="filteredList.length">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="pageSize"
              :total="filteredList.length"
              :current-page.sync="page"
            />
          </div>
          <div class="empty-text" v-else>
            暂无符合条件的商品，试试修改一下筛选条件吧～
          </div>
        </main>

        <!-- 右侧信息栏 -->
        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 页面数据</div>
            <div class="panel-stat">
              <span>商品总数</span>
              <b>{{ filteredList.length }}</b>
            </div>
            <div class="panel-stat">
              <span>当前页</span>
              <b>{{ page }}</b>
            </div>
            <div class="panel-stat">
              <span>每页条数</span>
              <b>{{ pageSize }}</b>
            </div>
            <div class="panel-stat">
              <span>筛选分类</span>
              <b>{{ query.brandId ? (brandMap[query.brandId] || '已选择') : '全部' }}</b>
            </div>
          </el-card>

          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/buy-requests')">去求购广场看看</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看我的收藏</div>
            <div class="quick-link" @click="$router.push('/cart')">进入购物车</div>
            <div class="quick-link" @click="$router.push('/orders')">查看我的订单</div>
          </el-card>

          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-warning-outline" /> 交易小贴士</div>
            <ul class="tips-list">
              <li>优先同校当面交易，选择人多区域</li>
              <li>购买前确认成色、配件和实际功能</li>
              <li>有问题可在订单页联系卖家与反馈</li>
            </ul>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchShopGoods } from '@/api/goods'
import { fetchPublicBrands } from '@/api/brand'
import { conditionText } from '@/utils/display'
import { handleError } from '@/utils/error'

export default {
  name: 'ShopGoodsList',
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 8,
      keyword: '',
      categories: [],
      brandMap: {},
      query: {
        brandId: null,
        minPrice: null,
        maxPrice: null,
        conditionLevel: null,
        sort: 'latest'
      }
    }
  },
  created () {
    this.loadCategories()
    this.load()
  },
  computed: {
    filteredList () {
      const kw = (this.keyword || '').trim().toLowerCase()
      let arr = Array.isArray(this.list) ? [...this.list] : []
      if (kw) {
        arr = arr.filter(item => {
          const name = (item.name || '').toLowerCase()
          return name.includes(kw)
        })
      }
      return arr
    },
    pagedList () {
      const start = (this.page - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredList.slice(start, end)
    }
  },
  methods: {
    async loadCategories () {
      try {
        const res = await fetchPublicBrands()
        if (res && res.code === 200) {
          this.categories = res.data || []
          const map = {}
          this.categories.forEach(c => {
            map[c.id] = c.name
          })
          this.brandMap = map
        }
      } catch (e) {
        handleError(this, e, '加载分类失败')
      }
    },
    async load () {
      const params = { ...this.query }
      try {
        const res = await fetchShopGoods(params)
        if (res && res.code === 200) {
          this.list = res.data || []
          this.page = 1
        }
      } catch (e) {
        handleError(this, e, '加载商品列表失败')
      }
    },
    conditionText,
    getImages (item) {
      const url = item && item.imageUrl ? String(item.imageUrl).trim() : ''
      if (!url) return []
      // 兼容后端可能返回 "a,b,c" 或 "a;b;c"
      const parts = url.split(/[,;]/).map(s => s.trim()).filter(Boolean)
      return parts.length ? parts : [url]
    },
    onImgError (e) {
      const img = e && e.target
      if (!img) return
      // 简单兜底：给一个纯色占位，避免破图影响布局
      img.onerror = null
      img.src = 'data:image/svg+xml;utf8,' + encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400">
          <rect width="100%" height="100%" fill="#F5F7FA"/>
          <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#6C757D" font-size="18">暂无图片</text>
        </svg>`
      )
    },
    onSelectBrand (id) {
      this.query.brandId = id
      this.load()
    },
    onCardClick (item) {
      this.goDetail(item.id)
    },
    goDetail (id) {
      this.$router.push(`/goods/${id}`)
    }
  }
}
</script>

<style scoped>
.market-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-color);
  /* 脱离默认 1200 容器限制，尽量铺满可视宽度 */
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
.shop-goods {
  padding: 12px 18px 32px;
}
.header {
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-text {
  text-align: left;
}
.header-title {
  font-size: 22px;
  margin-bottom: 4px;
  font-weight: 600;
  color: var(--text-primary);
}
.header-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}
.header-meta {
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
  width: 300px;
  background: var(--card-bg);
  border-radius: 12px;
  padding: 16px 14px 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.sidebar-section + .sidebar-section {
  margin-top: 18px;
}
.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.category-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.category-list li {
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.category-list li:hover,
.category-list li.active {
  background: rgba(74, 144, 217, 0.10);
  color: var(--primary-color);
}
.category-select {
  width: 100%;
}
.price-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}
.price-sep {
  font-size: 12px;
  color: var(--text-secondary);
}
.condition-group,
.sort-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.apply-btn {
  width: 100%;
  margin-top: 12px;
}
.main-content {
  flex: 1;
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
.tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 13px;
}
.goods-grid {
  display: grid;
  /* 桌面端优先保证一行 4 列，卡片视觉放大 */
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: start;
  gap: 18px;
}
.goods-grid-item {
  min-width: 0;
  display: flex;
  justify-content: center;
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
@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .right-panel {
    width: 100%;
  }
  .goods-grid {
    grid-template-columns: 1fr;
  }
  .market-navbar {
    padding: 0 12px;
  }
  .nav-search {
    margin: 0 10px;
    max-width: none;
  }
}
.goods-card {
  margin-bottom: 0;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: 410px;
  width: 100%;
  max-width: 320px;
  display: flex;
  flex-direction: column;
}
.goods-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.14);
  transform: translateY(-6px);
}
.sold-out-card {
  cursor: not-allowed;
}
.sold-out-card .goods-img,
.sold-out-card .img-placeholder {
  filter: grayscale(100%);
  opacity: 0.7;
}
.sold-out-card:hover {
  border-color: #f0f0f0;
  box-shadow: none;
  transform: none;
}
.img-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
  background: #f7f9fc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.category-pill {
  position: absolute;
  left: 10px;
  bottom: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--primary-color);
  backdrop-filter: blur(6px);
}
.sold-out-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}
.goods-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.img-placeholder {
  font-size: 12px;
  color: var(--text-secondary);
}
.goods-info {
  padding: 10px 12px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.name {
  font-size: 15px;
  margin-bottom: 10px;
  font-weight: 600;
  color: var(--text-primary);
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}
.meta-icon {
  width: 18px;
  text-align: center;
  opacity: 0.95;
}
.meta-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.price {
  font-size: 18px;
  color: var(--accent-color);
  font-weight: 600;
}
.bottom-row {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}
.seller,
.time {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.pagination {
  margin-top: 20px;
  text-align: center;
}
.empty-text {
  margin-top: 40px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>

