<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">举报管理</h2>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="targetType" label="类型" width="90">
        <template slot-scope="scope">
          {{ targetTypeLabel(scope.row.targetType) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetName" label="目标" min-width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.targetName">{{ scope.row.targetName }}</span>
          <span v-else>#{{ scope.row.targetId }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="举报原因" />
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template slot-scope="scope">
          <el-button type="text" @click="openDialog(scope.row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="处理举报" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input
            v-model="form.result"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="onSave">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { fetchReports, handleReport } from '@/api/report'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { reportStatusLabel, reportStatusType } from '@/utils/status'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'AdminReportList',
  data () {
    return {
      list: [],
      dialogVisible: false,
      form: {
        id: null,
        status: 'PENDING',
        result: ''
      },
      statusOptions: {},
      statusList: []
    }
  },
  created () {
    this.load()
    this.loadStatusDict()
  },
  methods: {
    formatTime (v) {
      return formatDateTime(v)
    },
    async loadStatusDict () {
      try {
        const res = await fetchDictData('report_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[item.value] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.statusOptions = map
          this.statusList = res.data
        }
      } catch (e) {
        handleError(this, e, '加载举报状态字典失败')
      }
    },
    async load () {
      try {
        const res = await fetchReports()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载举报列表失败')
      }
    },
    targetTypeLabel (type) {
      if (type === 'USER') return '用户'
      if (type === 'GOODS') return '商品'
      if (type === 'ORDER') return '订单'
      return type || '-'
    },
    statusLabel (status) {
      return reportStatusLabel(status, this.statusOptions)
    },
    statusType (status) {
      return reportStatusType(status, this.statusOptions)
    },
    openDialog (row) {
      this.form = {
        id: row.id,
        status: row.status || 'PENDING',
        result: row.result || ''
      }
      this.dialogVisible = true
    },
    async onSave () {
      const payload = {
        status: this.form.status,
        result: this.form.result
      }
      try {
        await handleReport(this.form.id, payload)
        this.$message.success('处理成功')
        this.dialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '处理举报失败')
      }
    }
  }
}
</script>

<style scoped>
.page {
  padding: 8px 0 24px;
  text-align: left;
}
.title {
  margin-bottom: 12px;
}
</style>

