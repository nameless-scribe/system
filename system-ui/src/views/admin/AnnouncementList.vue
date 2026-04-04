<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
    </div>
    <div class="page-toolbar">
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增公告</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row.status)">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
          <el-button type="text" style="color: red" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑公告' : '新增公告'" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="item in statusList"
              :key="item.value"
              :label="item.label"
              :value="Number(item.value)"
            />
          </el-select>
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
import { fetchAdminAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { announcementStatusLabel, announcementStatusType } from '@/utils/status'
import { formatDateTime } from '@/utils/date'

export default {
  name: 'AdminAnnouncementList',
  data () {
    return {
      list: [],
      dialogVisible: false,
      form: {
        id: null,
        title: '',
        content: '',
        status: 1
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
        const res = await fetchDictData('announcement_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[Number(item.value)] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.statusOptions = map
          this.statusList = res.data
        }
      } catch (e) {
        handleError(this, e, '加载公告状态字典失败')
      }
    },
    async load () {
      try {
        const res = await fetchAdminAnnouncements()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载公告列表失败')
      }
    },
    statusLabel (status) {
      return announcementStatusLabel(status, this.statusOptions)
    },
    statusType (status) {
      return announcementStatusType(status, this.statusOptions)
    },
    openDialog (row) {
      if (row) {
        this.form = {
          id: row.id,
          title: row.title,
          content: row.content,
          status: row.status
        }
      } else {
        this.form = {
          id: null,
          title: '',
          content: '',
          status: 1
        }
      }
      this.dialogVisible = true
    },
    async onSave () {
      if (!this.form.title) {
        this.$message.error('请输入标题')
        return
      }
      const payload = { ...this.form }
      try {
        if (payload.id) {
          await updateAnnouncement(payload.id, payload)
        } else {
          await createAnnouncement(payload)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '保存公告失败')
      }
    },
    async onDelete (id) {
      this.$confirm('确定要删除该公告吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteAnnouncement(id)
          this.$message.success('已删除')
          this.load()
        } catch (e) {
          handleError(this, e, '删除公告失败')
        }
      }).catch(() => {})
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
.toolbar {
  margin-bottom: 10px;
}
</style>

