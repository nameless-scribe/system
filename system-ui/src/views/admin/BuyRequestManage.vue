<template>
  <div class="page admin-buy-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">求购管理</h2>
        <div class="page-subtitle">管理求购广场信息：查询、删除（软删除）</div>
      </div>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="关键词（标题/内容/用户名）" clearable style="width: 260px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 160px">
          <el-option label="正常" :value="1" />
          <el-option label="已删除" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="onSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
      </div>

      <el-table :data="list" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="username" label="发布人" width="140" />
        <el-table-column label="点赞/评论" width="130">
          <template slot-scope="scope">
            👍 {{ scope.row.likeCount || 0 }} / 💬 {{ scope.row.commentCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template slot-scope="scope">
            <el-button
              type="text"
              style="color:red"
              v-if="scope.row.status === 1"
              @click="onDelete(scope.row)"
            >
              删除
            </el-button>
            <span v-else class="muted">已删除</span>
          </template>
        </el-table-column>
      </el-table>

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
  </div>
</template>

<script>
import { fetchAdminBuyRequests, deleteAdminBuyRequest } from '@/api/adminBuyRequest'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/time'

export default {
  name: 'AdminBuyRequestManage',
  data () {
    return {
      loading: false,
      list: [],
      total: 0,
      query: {
        keyword: '',
        status: 1,
        page: 1,
        size: 10
      }
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
        const params = {
          keyword: this.query.keyword || undefined,
          status: this.query.status,
          page: this.query.page,
          size: this.query.size
        }
        const res = await fetchAdminBuyRequests(params)
        if (res && res.code === 200 && res.data) {
          this.list = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        handleError(this, e, '加载求购列表失败')
      } finally {
        this.loading = false
      }
    },
    onSearch () {
      this.query.page = 1
      this.load()
    },
    onReset () {
      this.query = { keyword: '', status: 1, page: 1, size: 10 }
      this.load()
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
    onDelete (row) {
      this.$confirm(`确定删除求购信息 #${row.id} 吗？（软删除，前台将不可见）`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteAdminBuyRequest(row.id)
            this.$message.success('已删除')
            this.load()
          } catch (e) {
            handleError(this, e, '删除失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.muted {
  color: #909399;
  font-size: 12px;
}
</style>

