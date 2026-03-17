<template>
  <div class="shop-goods">
    <div class="header">
      <h2>校园精选闲置</h2>
    </div>

    <div class="filters card">
      <el-select v-model="query.brandId" placeholder="按分类" clearable style="width: 160px" @change="load">
        <el-option
          v-for="c in categories"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>
      <el-input
        v-model.number="query.minPrice"
        placeholder="最低价"
        style="width: 120px; margin-left: 8px"
        @change="load"
      />
      <el-input
        v-model.number="query.maxPrice"
        placeholder="最高价"
        style="width: 120px; margin-left: 8px"
        @change="load"
      />
      <el-select
        v-model="query.conditionLevel"
        placeholder="新旧程度"
        clearable
        style="width: 140px; margin-left: 8px"
        @change="load"
      >
        <el-option label="全新" :value="1" />
        <el-option label="9成新" :value="2" />
        <el-option label="8成新" :value="3" />
        <el-option label="7成新及以下" :value="4" />
      </el-select>
      <el-select
        v-model="query.sort"
        placeholder="排序"
        clearable
        style="width: 140px; margin-left: 8px"
        @change="load"
      >
        <el-option label="最新发布" value="latest" />
        <el-option label="价格从低到高" value="priceAsc" />
        <el-option label="价格从高到低" value="priceDesc" />
        <el-option label="最受关注" value="hot" />
      </el-select>
    </div>

    <el-row :gutter="16" class="goods-row">
      <el-col :span="6" v-for="item in list" :key="item.id">
        <el-card
          class="goods-card"
          :class="{ 'sold-out-card': Number(item.stock || 0) <= 0 }"
          shadow="hover"
          @click.native="onCardClick(item)"
        >
          <div class="img-wrapper">
            <img v-if="item.imageUrl" :src="item.imageUrl" class="goods-img">
            <div v-else class="img-placeholder">No Image</div>
            <el-tag v-if="Number(item.stock || 0) <= 0" type="info" size="mini" class="sold-out-tag">已售罄</el-tag>
          </div>
          <div class="goods-info">
            <h3 class="name" :title="item.name">{{ item.name }}</h3>
            <div class="price-row">
              <span class="price">￥{{ item.price }}</span>
              <span
                v-if="item.conditionLevel"
                class="condition-tag"
              >{{ conditionText(item.conditionLevel) }}</span>
            </div>
            <p class="meta">
              <span v-if="brandMap[item.brandId]">品牌：{{ brandMap[item.brandId] }}</span>
              <span class="divider">|</span>
              <span>库存：{{ item.stock }}</span>
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
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
        }
      } catch (e) {
        handleError(this, e, '加载商品列表失败')
      }
    },
    conditionText,
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
.shop-goods {
  padding: 12px 0 32px;
  background: #f5f5f5;
}
.header {
  margin: 0 8px 12px;
}
.header h2 {
  font-size: 22px;
  margin-bottom: 4px;
}
.subtitle {
  font-size: 13px;
  color: #909399;
}
.filters.card {
  margin: 0 8px 16px;
  padding: 10px 16px;
  background: #ffffff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.goods-row {
  margin: 0 4px;
}
.goods-card {
  margin-bottom: 20px;
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  background: #ffffff;
  transition: all 0.2s ease;
}
.goods-card:hover {
  border-color: #e1251b;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
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
  height: 190px;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
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
}
.img-placeholder {
  font-size: 12px;
  color: #c0c4cc;
}
.goods-info {
  padding: 8px 10px 10px;
}
.name {
  font-size: 15px;
  margin-bottom: 6px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: 600;
}
.condition-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  background-color: #ecf5ff;
  color: #409eff;
}
.meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.divider {
  margin: 0 6px;
  color: #e4e7ed;
}
</style>

