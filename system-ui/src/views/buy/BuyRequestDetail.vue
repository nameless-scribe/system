<template>
  <div class="page buy-detail" v-loading="loading">
    <el-card shadow="never" v-if="detail">
      <div class="head">
        <el-avatar :size="44" :src="detail.avatar" icon="el-icon-user-solid" />
        <div class="meta">
          <div class="title">{{ detail.title }}</div>
          <div class="sub">
            <span class="u">{{ detail.username || '用户' }}</span>
            <span class="dot">·</span>
            <span>{{ formatDateTime(detail.createTime) }}</span>
            <span class="dot">·</span>
            <span>👍 {{ detail.likeCount || 0 }}</span>
            <span class="dot">·</span>
            <span>💬 {{ detail.commentCount || 0 }}</span>
          </div>
        </div>
        <div class="actions">
          <el-button :type="liked ? 'warning' : 'default'" :icon="liked ? 'el-icon-star-on' : 'el-icon-star-off'" @click="toggleLike">
            {{ liked ? '已点赞' : '点赞' }}
          </el-button>
        </div>
      </div>

      <div class="content">{{ detail.content }}</div>

      <div v-if="detail.priceMin != null || detail.priceMax != null" class="budget">
        预算：
        <b>{{ detail.priceMin != null ? detail.priceMin : '-' }}</b>
        ~
        <b>{{ detail.priceMax != null ? detail.priceMax : '-' }}</b>
      </div>
      <div v-if="detail.contact" class="contact">联系方式：{{ detail.contact }}</div>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px;">
      <div slot="header" class="card-header">
        <span>评论</span>
      </div>

      <div class="comment-box">
        <el-input type="textarea" v-model="commentText" :rows="3" maxlength="800" show-word-limit placeholder="写下你的评论…" />
        <div class="comment-actions">
          <el-button type="primary" @click="submitComment">发表评论</el-button>
        </div>
      </div>

      <div v-if="comments.length" class="comment-list">
        <div class="comment-item" v-for="c in comments" :key="c.id">
          <el-avatar :size="32" :src="c.avatar" icon="el-icon-user-solid" />
          <div class="c-body">
            <div class="c-meta">
              <span class="c-user">{{ c.username || '用户' }}</span>
              <span class="dot">·</span>
              <span class="c-time">{{ formatDateTime(c.createTime) }}</span>
            </div>
            <div class="c-content">{{ c.content }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-text">暂无评论</div>
    </el-card>
  </div>
</template>

<script>
import { fetchBuyRequestDetail, fetchBuyRequestComments, addBuyRequestComment, likeBuyRequest, unlikeBuyRequest, buyRequestLiked } from '@/api/buyRequest'
import { useUserStore } from '@/store'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'BuyRequestDetail',
  data () {
    return {
      loading: false,
      detail: null,
      comments: [],
      commentText: '',
      liked: false
    }
  },
  created () {
    this.load()
  },
  methods: {
    formatDateTime,
    async load () {
      const id = this.$route.params.id
      this.loading = true
      try {
        const [d, c] = await Promise.all([fetchBuyRequestDetail(id), fetchBuyRequestComments(id)])
        if (d && d.code === 200) this.detail = d.data
        if (c && c.code === 200) this.comments = c.data || []
        await this.loadLiked()
      } catch (e) {
        handleError(this, e, '加载求购详情失败')
      } finally {
        this.loading = false
      }
    },
    async loadLiked () {
      const store = useUserStore()
      if (!store.user) await store.fetchCurrentUser()
      if (!store.user) {
        this.liked = false
        return
      }
      try {
        const res = await buyRequestLiked(this.$route.params.id)
        if (res && res.code === 200) this.liked = !!res.data
      } catch (e) {
        this.liked = false
      }
    },
    async toggleLike () {
      const store = useUserStore()
      if (!store.user) await store.fetchCurrentUser()
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        if (this.liked) {
          await unlikeBuyRequest(this.$route.params.id)
          this.liked = false
          if (this.detail) this.detail.likeCount = Math.max(0, Number(this.detail.likeCount || 0) - 1)
        } else {
          await likeBuyRequest(this.$route.params.id)
          this.liked = true
          if (this.detail) this.detail.likeCount = Number(this.detail.likeCount || 0) + 1
        }
      } catch (e) {
        handleError(this, e, '操作失败')
      }
    },
    async submitComment () {
      const store = useUserStore()
      if (!store.user) await store.fetchCurrentUser()
      if (!store.user) {
        this.$message.error('请先登录')
        this.$router.push('/login')
        return
      }
      if (!this.commentText.trim()) {
        this.$message.error('请输入评论内容')
        return
      }
      try {
        await addBuyRequestComment(this.$route.params.id, this.commentText.trim())
        this.commentText = ''
        await this.load()
      } catch (e) {
        handleError(this, e, '发表评论失败')
      }
    }
  }
}
</script>

<style scoped>
.head {
  display: flex;
  gap: 12px;
}
.meta {
  flex: 1;
}
.title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.dot {
  margin: 0 6px;
  color: #c0c4cc;
}
.actions {
  display: flex;
  align-items: flex-start;
}
.content {
  margin-top: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
.budget, .contact {
  margin-top: 10px;
  font-size: 13px;
  color: #606266;
}
.comment-box {
  margin-bottom: 12px;
}
.comment-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}
.c-body {
  flex: 1;
}
.c-meta {
  font-size: 12px;
  color: #909399;
}
.c-user {
  font-weight: 600;
  color: #303133;
}
.c-content {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
.empty-text {
  text-align: center;
  padding: 16px 0;
  color: #909399;
  font-size: 13px;
}
</style>

