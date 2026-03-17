<template>
  <div class="page buy-square">
    <div class="page-header">
      <div>
        <h2 class="page-title">求购广场</h2>
        <div class="page-subtitle">浏览大家的求购需求，支持发布、点赞、评论</div>
      </div>
      <div>
        <el-button type="primary" icon="el-icon-edit" @click="openPublish">发布求购</el-button>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div v-if="pinned.length" class="pinned">
        <div class="pinned-title">置顶（Top 5）</div>
        <el-row :gutter="12">
          <el-col :span="12" v-for="item in pinned" :key="item.id">
            <div class="card pinned-card" @click="goDetail(item.id)">
              <div class="card-title">
                <el-tag size="mini" type="warning">置顶</el-tag>
                <span class="t">{{ item.title }}</span>
              </div>
              <div class="card-content">{{ item.content }}</div>
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

      <div class="list-title">最新发布</div>
      <div v-if="list.length">
        <div class="card" v-for="item in list" :key="item.id" @click="goDetail(item.id)">
          <div class="card-title">
            <span class="t">{{ item.title }}</span>
          </div>
          <div class="card-content">{{ item.content }}</div>
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
.pinned-title, .list-title {
  font-weight: 600;
  margin: 6px 0 10px;
  color: #303133;
}
.card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fff;
  margin-bottom: 10px;
  cursor: pointer;
}
.pinned-card {
  margin-bottom: 12px;
}
.card-title {
  display: flex;
  gap: 8px;
  align-items: center;
}
.t {
  font-weight: 600;
  color: #303133;
}
.card-content {
  margin-top: 8px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
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
  color: #909399;
  font-size: 13px;
}
</style>

