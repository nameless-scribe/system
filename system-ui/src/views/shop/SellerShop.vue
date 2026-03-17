<template>
  <div class="page seller-shop">
    <el-card shadow="never" v-loading="loading">
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
            来到平台：{{ shop.joinedDays || 0 }} 天
            <span class="dot">·</span>
            信誉分：{{ rep ? (rep.score || 0) : 0 }}
            <span class="dot">·</span>
            好评率：{{ rep && rep.positiveRate != null ? `${rep.positiveRate}%` : '0%' }}
          </div>
          <div class="seller-intro">{{ profile.intro || '这个卖家很神秘，还没有填写简介。' }}</div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px;">
      <div slot="header" class="card-header">
        <span>在售商品（{{ shop.onSaleCount || 0 }}）</span>
      </div>
      <el-row :gutter="16" class="goods-row">
        <el-col :span="6" v-for="item in shop.onSaleGoods || []" :key="item.id">
          <el-card class="goods-card" shadow="hover" @click.native="goGoods(item.id)">
            <div class="img-wrapper">
              <img v-if="item.imageUrl" :src="item.imageUrl" class="goods-img">
              <div v-else class="img-placeholder">No Image</div>
            </div>
            <div class="goods-info">
              <h3 class="name" :title="item.name">{{ item.name }}</h3>
              <div class="price-row">
                <span class="price">￥{{ item.price }}</span>
              </div>
              <p class="meta">
                <span>库存：{{ item.stock }}</span>
                <span class="divider">|</span>
                <span>浏览：{{ item.viewCount || 0 }}</span>
              </p>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div v-if="!((shop.onSaleGoods || []).length)" class="empty-text">暂无在售商品</div>
    </el-card>
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
.seller-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}
.seller-meta {
  flex: 1;
}
.seller-name {
  font-size: 18px;
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
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
.img-wrapper {
  height: 140px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.goods-img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}
.img-placeholder {
  font-size: 13px;
  color: #c0c4cc;
}
.goods-info .name {
  font-size: 14px;
  margin: 10px 0 6px;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}
.meta {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}
.divider {
  margin: 0 6px;
  color: #e4e7ed;
}
.empty-text {
  text-align: center;
  padding: 18px 0;
  color: #909399;
  font-size: 13px;
}
</style>

