<template>
  <div class="page audit-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">系统日志</h2>
        <div class="page-subtitle">记录管理员操作，便于追溯与审计</div>
      </div>
    </div>

    <el-card shadow="never">
      <div class="page-toolbar">
        <el-input v-model="query.operatorName" placeholder="操作人" clearable style="width: 180px" />
        <el-input v-model="query.uri" placeholder="URI 关键字" clearable style="width: 220px" />
        <el-select v-model="query.success" placeholder="结果" clearable style="width: 140px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="query.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 380px"
        />
        <el-button type="primary" icon="el-icon-search" @click="onSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="onReset">重置</el-button>
      </div>

      <el-table :data="list" border stripe style="width: 100%">
        <el-table-column prop="createTime" label="时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="150" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="httpMethod" label="方法" width="90" />
        <el-table-column prop="uri" label="URI" min-width="240" show-overflow-tooltip />
        <el-table-column prop="costMs" label="耗时(ms)" width="110" />
        <el-table-column label="结果" width="110">
          <template slot-scope="scope">
            <el-tag :type="scope.row.success === 1 ? 'success' : 'danger'">
              {{ scope.row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultCode" label="Code" width="90" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="openDetail(scope.row.id)">详情</el-button>
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
          :page-sizes="[10, 20, 50, 100]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-drawer title="日志详情" :visible.sync="detailVisible" size="45%">
      <div v-if="detail" class="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detail.operatorId }}</el-descriptions-item>
          <el-descriptions-item label="旧角色">{{ detail.operatorRole }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
          <el-descriptions-item label="方法">{{ detail.httpMethod }}</el-descriptions-item>
          <el-descriptions-item label="URI">{{ detail.uri }}</el-descriptions-item>
          <el-descriptions-item label="Query">{{ detail.queryString }}</el-descriptions-item>
          <el-descriptions-item label="耗时(ms)">{{ detail.costMs }}</el-descriptions-item>
          <el-descriptions-item label="结果">{{ detail.success === 1 ? '成功' : '失败' }}</el-descriptions-item>
          <el-descriptions-item label="Code">{{ detail.resultCode }}</el-descriptions-item>
          <el-descriptions-item label="错误信息">{{ detail.errorMessage }}</el-descriptions-item>
        </el-descriptions>

        <div class="req-body">
          <div class="req-title">参数摘要</div>
          <pre class="req-pre">{{ detail.requestBody || '-' }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { fetchAuditLogs, fetchAuditLogDetail } from '@/api/auditLog'
import { handleError } from '@/utils/error'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'AdminAuditLogList',
  data () {
    return {
      list: [],
      total: 0,
      query: {
        operatorName: '',
        uri: '',
        success: undefined,
        timeRange: [],
        page: 1,
        size: 10
      },
      detailVisible: false,
      detail: null
    }
  },
  created () {
    this.load()
  },
  methods: {
    formatTime (v) {
      return formatDateTime(v)
    },
    async load () {
      try {
        const params = {
          operatorName: this.query.operatorName || undefined,
          uri: this.query.uri || undefined,
          success: this.query.success,
          startTime: this.query.timeRange && this.query.timeRange[0],
          endTime: this.query.timeRange && this.query.timeRange[1],
          page: this.query.page,
          size: this.query.size
        }
        const res = await fetchAuditLogs(params)
        if (res && res.code === 200 && res.data) {
          this.list = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        handleError(this, e, '加载系统日志失败')
      }
    },
    onSearch () {
      this.query.page = 1
      this.load()
    },
    onReset () {
      this.query = {
        operatorName: '',
        uri: '',
        success: undefined,
        timeRange: [],
        page: 1,
        size: 10
      }
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
    async openDetail (id) {
      this.detailVisible = true
      this.detail = null
      try {
        const res = await fetchAuditLogDetail(id)
        if (res && res.code === 200) {
          this.detail = res.data
        }
      } catch (e) {
        handleError(this, e, '加载日志详情失败')
      }
    }
  }
}
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.detail {
  padding: 12px 16px 24px;
}
.req-body {
  margin-top: 14px;
}
.req-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}
.req-pre {
  background: #0b1020;
  color: #e5e7eb;
  padding: 12px;
  border-radius: 8px;
  overflow: auto;
  max-height: 420px;
  font-size: 12px;
  line-height: 1.5;
}
</style>

