<template>
  <div class="buy-page">
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-search">
        <el-input placeholder="搜索求购标题/内容..." prefix-icon="el-icon-search" />
      </div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/goods')">商品广场</el-button>
        <el-button type="text" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-button type="primary" @click="openPublish">发布求购</el-button>
        <el-button type="text" @click="$router.push('/profile')">个人中心</el-button>
      </div>
    </div>

    <div class="buy-content">
      <div class="header">
        <div>
          <h2 class="header-title">求购广场</h2>
          <div class="header-subtitle">浏览大家的求购需求，支持发布、点赞、评论</div>
        </div>
        <span class="meta-pill">共 <strong>{{ total }}</strong> 条求购</span>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">浏览设置</div>
            <el-select v-model="query.size" size="small" style="width:100%" @change="onSizeChange">
              <el-option label="每页 10 条" :value="10" />
              <el-option label="每页 20 条" :value="20" />
              <el-option label="每页 50 条" :value="50" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <el-button type="primary" size="small" class="apply-btn" @click="load">刷新列表</el-button>
            <el-button size="small" class="reset-btn" @click="openPublish">发布求购</el-button>
          </div>
        </aside>

        <main class="main-content">
          <el-card shadow="never" v-loading="loading">
            <div v-if="pinned.length" class="pinned">
              <div class="pinned-title"><i class="el-icon-top" /> 置顶（Top 5）</div>
              <el-row :gutter="12">
                <el-col :span="12" v-for="item in pinned" :key="item.id">
                  <div class="card pinned-card" @click="goDetail(item.id)">
                    <div class="card-title">
                      <div class="title-left">
                        <el-tag size="mini" type="warning">置顶</el-tag>
                        <span class="t">{{ item.title }}</span>
                      </div>
                      <span class="heat-badge">热度 {{ getHeat(item) }}</span>
                    </div>
                    <div class="card-content">{{ item.content }}</div>
                    <div class="extra-line">
                      <span><i class="el-icon-money" /> 预算：{{ budgetText(item) }}</span>
                      <span><i class="el-icon-phone-outline" /> 联系：{{ contactText(item) }}</span>
                    </div>
                    <div class="card-meta">
                      <span class="u">{{ item.username || '用户' }}</span>
                      <span class="dot">·</span>
                      <span>{{ formatDateTime(item.createTime) }}</span>
                      <span class="dot">·</span>
                      <span>👍 {{ item.likeCount || 0 }}</span>
                      <span class="dot">·</span>
                      <span>💬 {{ item.commentCount || 0 }}</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <div class="list-title"><i class="el-icon-time" /> 最新发布</div>
            <div v-if="list.length">
              <div class="card" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
                <div class="card-title">
                  <div class="title-left">
                    <span class="t">{{ item.title }}</span>
                  </div>
                  <span class="heat-badge">热度 {{ getHeat(item) }}</span>
                </div>
                <div class="card-content">{{ item.content }}</div>
                <div class="extra-line">
                  <span><i class="el-icon-money" /> 预算：{{ budgetText(item) }}</span>
                  <span><i class="el-icon-phone-outline" /> 联系：{{ contactText(item) }}</span>
                </div>
                <div class="card-meta">
                  <span class="u">{{ item.username || '用户' }}</span>
                  <span class="dot">·</span>
                  <span>{{ formatDateTime(item.createTime) }}</span>
                  <span class="dot">·</span>
                  <span>👍 {{ item.likeCount || 0 }}</span>
                  <span class="dot">·</span>
                  <span>💬 {{ item.commentCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-text">暂无求购信息</div>

            <div class="pager">
              <el-pagination
                background
                layout="total, prev, pager, next, sizes"
                :total="total"
                :page-size="query.size"
                :current-page="query.page"
                :page-sizes="[10, 20, 50]"
                @current-change="onPageChange"
                @size-change="onSizeChange"
              />
            </div>
          </el-card>
        </main>

        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 广场数据</div>
            <div class="panel-stat"><span>置顶数量</span><b>{{ pinned.length }}</b></div>
            <div class="panel-stat"><span>当前列表</span><b>{{ list.length }}</b></div>
            <div class="panel-stat"><span>总条数</span><b>{{ total }}</b></div>
            <div class="panel-stat"><span>当前页</span><b>{{ query.page }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/goods')">去商品广场看看</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看我的收藏</div>
            <div class="quick-link" @click="$router.push('/orders')">查看我的订单</div>
          </el-card>
        </aside>
      </div>
    </div>

    <el-dialog title="发布求购" :visible.sync="publishVisible" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="form.content" :rows="5" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="预算(可选)">
          <div style="display:flex; gap:10px;">
            <el-input-number v-model="form.priceMin" :min="0" :precision="2" style="width: 180px" placeholder="最低价" />
            <el-input-number v-model="form.priceMax" :min="0" :precision="2" style="width: 180px" placeholder="最高价" />
          </div>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" maxlength="200" show-word-limit placeholder="选填：例如微信/手机号/邮箱" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="publishVisible=false">取消</el-button>
        <el-button type="primary" @click="onPublish">发布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { fetchBuyRequestSquare, publishBuyRequest } from '@/api/buyRequest'
import { useUserStore } from '@/store'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'BuyRequestSquare',
  data () {
    return {
      loading: false,
      pinned: [],
      list: [],
      total: 0,
      query: { page: 1, size: 10 },
      publishVisible: false,
      form: { title: '', content: '', priceMin: null, priceMax: null, contact: '' }
    }
  },
  created () {
    this.load()
  },
  methods: {
    formatDateTime,
    getHeat (item) {
      const like = Number(item && item.likeCount ? item.likeCount : 0)
      const comment = Number(item && item.commentCount ? item.commentCount : 0)
      return like * 2 + comment
    },
    budgetText (item) {
      const min = item && item.priceMin != null ? Number(item.priceMin) : null
      const max = item && item.priceMax != null ? Number(item.priceMax) : null
      if (min != null && max != null) return `￥${min} - ￥${max}`
      if (min != null) return `￥${min} 起`
      if (max != null) return `￥${max} 以下`
      return '面议'
    },
    contactText (item) {
      const contact = item && item.contact ? String(item.contact).trim() : ''
      return contact || '站内私信'
    },
    async load () {
      this.loading = true
      try {
        const res = await fetchBuyRequestSquare(this.query)
        if (res && res.code === 200 && res.data) {
          this.pinned = res.data.pinned || []
          this.list = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        handleError(this, e, '加载求购广场失败')
      } finally {
        this.loading = false
      }
    },
    onPageChange (p) {
      this.query.page = p
      this.load()
    },
    onSizeChange (s) {
      this.query.size = s
      this.query.page = 1
      this.load()
    },
    goDetail (id) {
      this.$router.push(`/buy-requests/${id}`)
    },
    async openPublish () {
      const store = useUserStore()
      if (!store.user) await store.fetchCurrentUser()
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      this.form = { title: '', content: '', priceMin: null, priceMax: null, contact: '' }
      this.publishVisible = true
    },
    async onPublish () {
      if (!this.form.title || !this.form.content) {
        this.$message.error('请填写标题与内容')
        return
      }
      try {
        await publishBuyRequest(this.form)
        this.$message.success('发布成功')
        this.publishVisible = false
        this.query.page = 1
        await this.load()
      } catch (e) {
        handleError(this, e, '发布失败')
      }
    }
  }
}
</script>

<style scoped>
.buy-page {
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
.buy-content {
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
  color: var(--text-secondary);
  font-size: 13px;
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
}
.reset-btn {
  margin-top: 8px;
}
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
.pinned-title, .list-title {
  font-weight: 600;
  margin: 6px 0 10px;
  color: var(--text-primary);
}
.pinned-title i,
.list-title i {
  color: var(--primary-color);
  margin-right: 6px;
}
.card {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--card-bg);
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 8px 20px rgba(74, 144, 217, 0.12);
  transform: translateY(-2px);
}
.pinned-card {
  margin-bottom: 12px;
}
.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.title-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.t {
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.heat-badge {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--accent-color);
  background: rgba(255, 107, 53, 0.12);
  border: 1px solid rgba(255, 107, 53, 0.24);
  border-radius: 999px;
  padding: 2px 8px;
}
.card-content {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.extra-line {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}
.extra-line span {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.empty-text {
  text-align: center;
  padding: 16px 0;
  color: var(--text-secondary);
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

