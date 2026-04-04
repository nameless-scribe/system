<template>
  <div class="ratings-shell">
    <!-- 顶部导航（统一风格） -->
    <div class="market-navbar">
      <div class="nav-logo" @click="$router.push('/')">校园易物</div>
      <div class="nav-actions">
        <el-button type="text" @click="$router.push('/orders')">我的订单</el-button>
        <el-button type="text" @click="$router.push('/favorites')">我的收藏</el-button>
        <el-button type="text" @click="$router.push('/goods')">继续逛商品</el-button>
      </div>
    </div>

    <div class="page">
      <!-- 页头 -->
      <div class="header">
        <div>
          <h2 class="header-title">我的评价</h2>
          <div class="header-subtitle">查看、筛选、编辑或删除你发表过的交易评价</div>
        </div>
        <span class="meta-pill">共 <strong>{{ list.length }}</strong> 条</span>
      </div>

      <div class="layout">
        <!-- 左侧筛选栏 -->
        <aside class="sidebar">
          <div class="sidebar-section">
            <div class="sidebar-title">快速筛选</div>
            <el-input
              v-model="keyword"
              placeholder="搜索 订单ID / 评语"
              prefix-icon="el-icon-search"
              clearable
            />
          </div>
          <div class="sidebar-section">
            <div class="sidebar-title">评分</div>
            <div class="rate-filter">
              <el-rate v-model="scoreFilter" :max="5" allow-half />
              <div class="sub-hint">显示评分 ≥ 选择值</div>
            </div>
          </div>
          <div class="sidebar-section">
            <el-button type="primary" class="apply-btn" @click="applyFilters">应用筛选</el-button>
            <el-button class="reset-btn" @click="resetFilters">重置</el-button>
          </div>
        </aside>

        <!-- 中间主内容 -->
        <main class="main-content">
          <el-card class="table-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span><i class="el-icon-chat-line-round" /> 历史评价列表</span>
              </div>
            </template>

            <el-empty v-if="filteredList.length === 0" description="暂无评价记录">
              <el-button type="primary" @click="$router.push('/goods')">去逛逛</el-button>
            </el-empty>

            <el-table
              v-else
              :data="pagedList"
              border
              stripe
              style="width:100%"
            >
              <el-table-column prop="orderId" label="订单ID" width="140">
                <template slot-scope="scope">
                  <el-tag size="small">{{ scope.row.orderId }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="被评商品" width="200">
                <template slot-scope="scope">
                  <div class="goods-cell">
                    <div class="goods-pill" @click="goOrder(scope.row)" title="查看订单详情">
                      <i class="el-icon-goods"></i>
                      <span>订单 #{{ scope.row.orderId }}</span>
                    </div>
                    <div class="goods-sub">点击跳转到订单详情</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="评分" width="160" align="center">
                <template slot-scope="scope">
                  <el-rate :value="scope.row.score" :disabled="true" />
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="评语" min-width="300">
                <template slot-scope="scope">
                  <span class="comment-text">{{ scope.row.comment || '（无）' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createdTime" label="时间" width="190">
                <template slot-scope="scope">
                  {{ formatTime(scope.row.createdTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template slot-scope="scope">
                  <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
                  <el-divider direction="vertical" />
                  <el-button type="text" style="color:#f56c6c" @click="onDelete(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pager" v-if="filteredList.length > pageSize">
              <el-pagination
                background
                layout="prev, pager, next"
                :page-size="pageSize"
                :current-page.sync="page"
                :total="filteredList.length"
              />
            </div>
          </el-card>
        </main>

        <!-- 右侧信息栏 -->
        <aside class="right-panel">
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-data-analysis" /> 数据概览</div>
            <div class="panel-stat"><span>总条数</span><b>{{ list.length }}</b></div>
            <div class="panel-stat"><span>当前页</span><b>{{ page }}</b></div>
            <div class="panel-stat"><span>每页条数</span><b>{{ pageSize }}</b></div>
            <div class="panel-stat"><span>筛选评分</span><b>{{ scoreFilter || '不限' }}</b></div>
          </el-card>
          <el-card shadow="never" class="right-card">
            <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
            <div class="quick-link" @click="$router.push('/orders')">回到我的订单</div>
            <div class="quick-link" @click="$router.push('/favorites')">查看我的收藏</div>
            <div class="quick-link" @click="$router.push('/goods')">继续逛商品</div>
          </el-card>
        </aside>
      </div>

      <!-- 编辑弹窗 -->
      <el-dialog title="编辑评价" :visible.sync="editDialogVisible" width="420px">
        <el-form label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="editForm.score" :max="5" />
          </el-form-item>
          <el-form-item label="评语">
            <el-input type="textarea" v-model="editForm.comment" :rows="3" />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="editDialogVisible=false">取 消</el-button>
          <el-button type="primary" @click="submitEdit">保 存</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { fetchMyRatings, deleteMyRating, updateMyRating } from '@/api/rating'
import { formatDateTime } from '@/utils/date'
import { handleError } from '@/utils/error'

export default {
  name: 'MyRatings',
  data () {
    return {
      list: [],
      page: 1,
      pageSize: 10,
      keyword: '',
      scoreFilter: 0,
      editDialogVisible: false,
      editing: null,
      editForm: {
        score: 5,
        comment: ''
      }
    }
  },
  created () {
    this.load()
  },
  computed: {
    filteredList () {
      const kw = (this.keyword || '').trim().toLowerCase()
      let arr = Array.isArray(this.list) ? [...this.list] : []
      if (kw) {
        arr = arr.filter(i =>
          String(i.orderId || '').includes(kw) ||
          String(i.comment || '').toLowerCase().includes(kw)
        )
      }
      if (this.scoreFilter && Number(this.scoreFilter) > 0) {
        const s = Number(this.scoreFilter)
        arr = arr.filter(i => Number(i.score || 0) >= s)
      }
      return arr
    },
    pagedList () {
      const start = (this.page - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  methods: {
    goOrder (row) {
      if (!row || !row.orderId) return
      this.$router.push({ name: 'orderDetail', params: { id: row.orderId } })
    },
    formatTime (v) {
      return formatDateTime(v)
    },
    async load () {
      try {
        const res = await fetchMyRatings()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载我的评价失败')
      }
    },
    applyFilters () {
      this.page = 1
    },
    resetFilters () {
      this.keyword = ''
      this.scoreFilter = 0
      this.page = 1
    },
    openEdit (row) {
      this.editing = row
      this.editForm.score = row.score
      this.editForm.comment = row.comment || ''
      this.editDialogVisible = true
    },
    async submitEdit () {
      if (!this.editing) return
      try {
        await updateMyRating({
          orderId: this.editing.orderId,
          score: this.editForm.score,
          comment: this.editForm.comment
        })
        this.$message.success('已保存')
        this.editDialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '保存失败')
      }
    },
    async onDelete (row) {
      try {
        await this.$confirm('确认删除这条评价吗？', '提示', { type: 'warning' })
      } catch (e) {
        return
      }
      try {
        await deleteMyRating(row.id)
        this.$message.success('已删除')
        this.load()
      } catch (e) {
        handleError(this, e, '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.ratings-shell {
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
.page {
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
.reset-btn { margin-top: 8px; }
.sidebar .el-button + .el-button { margin-left: 0; }
.rate-filter .sub-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.main-content { flex: 1; min-width: 0; }
.table-card { border-radius: 12px; }
.card-header { font-weight: 600; color: var(--text-primary); }
.card-header i { color: var(--primary-color); margin-right: 6px; }
.user-cell { display:flex; align-items:center; gap:8px; }
.user-cell .avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: rgba(74,144,217,0.15); color:#3A7BC8;
  display:flex; align-items:center; justify-content:center;
  font-size:12px; font-weight:600;
}
.comment-text { color: var(--text-primary); }
.pager { margin-top: 14px; text-align: right; }

.goods-cell { display:flex; flex-direction:column; gap:4px; }
.goods-pill {
  display:inline-flex; align-items:center; gap:6px;
  padding: 6px 10px; border-radius: 999px;
  background: rgba(74,144,217,0.1);
  color: var(--primary-color);
  cursor: pointer; transition: all .2s ease;
  width: fit-content;
}
.goods-pill:hover { background: rgba(74,144,217,0.18); transform: translateY(-1px); }
.goods-sub { font-size: 12px; color: var(--text-secondary); }

.right-panel { width: 280px; display:flex; flex-direction:column; gap:12px; }
.right-card { border-radius: 12px; }
.panel-title { font-weight: 600; color: var(--text-primary); margin-bottom: 10px; }
.panel-title i { color: var(--primary-color); margin-right: 6px; }
.panel-stat {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 0; border-bottom: 1px dashed var(--border-color);
  color: var(--text-secondary); font-size: 13px;
}
.panel-stat:last-child { border-bottom: none; }
.panel-stat b { color: var(--primary-color); }
.quick-link {
  padding: 8px 10px; border-radius: 8px; background: #f8fbff;
  color: var(--text-secondary); cursor: pointer; transition: all .2s ease; font-size: 13px;
}
.quick-link + .quick-link { margin-top: 8px; }
.quick-link:hover { background: rgba(74, 144, 217, 0.1); color: var(--primary-color); }

@media (max-width: 768px) {
  .layout { flex-direction: column; }
  .sidebar, .right-panel { width: 100%; }
  .market-navbar { padding: 0 12px; }
}
</style>

