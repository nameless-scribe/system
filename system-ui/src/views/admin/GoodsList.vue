<template>
  <div class="goods-page">
    <h2 class="title">商品管理</h2>
    <div class="toolbar">
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增商品</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column label="审核状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="auditStatusType(scope.row.auditStatus)">
            {{ auditStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="退回原因" width="180" show-overflow-tooltip />
      <el-table-column label="上架状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="goodsStatusType(scope.row.status)">
            {{ goodsStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="imageUrl" label="图片" width="160">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            style="width: 80px; height: 80px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
          <el-button
            type="text"
            v-if="scope.row.auditStatus === 0"
            @click="approve(scope.row)"
          >通过</el-button>
          <el-button
            type="text"
            v-if="scope.row.auditStatus === 0"
            style="color:#e6a23c"
            @click="openRejectDialog(scope.row)"
          >退回</el-button>
          <el-button type="text" style="color:red" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑商品' : '新增商品'" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :step="0.01" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="#"
            :http-request="customUpload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :on-error="handleUploadError"
          >
            <el-button size="small" type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="form.imageUrl" style="margin-top:10px;">
            <el-image :src="form.imageUrl" style="width: 100px; height: 100px" fit="cover" />
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="onSave">保 存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="退回原因" :visible.sync="rejectDialogVisible" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="原因">
          <el-input
            type="textarea"
            v-model="rejectForm.reason"
            :rows="3"
            placeholder="请输入退回原因"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmReject">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { fetchDictData } from '@/api/dict'
import { handleError } from '@/utils/error'
import { createBeforeUpload, createHttpRequest, handleUploadError as uploadErrorHandler } from '@/utils/upload'

export default {
  name: 'GoodsList',
  data () {
    return {
      list: [],
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        price: 0,
        stock: 0,
        status: 1,
        imageUrl: '',
        description: ''
      },
      rejectDialogVisible: false,
      rejectForm: {
        id: null,
        reason: ''
      },
      goodsAuditStatusOptions: {},
      goodsStatusOptions: {}
    }
  },
  created () {
    this.fetchData()
    this.loadGoodsAuditStatus()
    this.loadGoodsStatus()
  },
  methods: {
    // 自定义上传方法（使用 axios，自动携带 token）
    customUpload: createHttpRequest('/admin/upload/image'),
    async loadGoodsAuditStatus () {
      try {
        const res = await fetchDictData('goods_audit_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[Number(item.value)] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.goodsAuditStatusOptions = map
        }
      } catch (e) {
        handleError(this, e, '加载商品审核状态字典失败')
      }
    },
    async loadGoodsStatus () {
      try {
        const res = await fetchDictData('goods_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[Number(item.value)] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.goodsStatusOptions = map
        }
      } catch (e) {
        handleError(this, e, '加载商品状态字典失败')
      }
    },
    async fetchData () {
      try {
        const res = await request({
          url: '/admin/goods',
          method: 'get'
        })
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载商品列表失败')
      }
    },
    openDialog (row) {
      if (row) {
        this.form = { ...row }
      } else {
        this.form = {
          id: null,
          name: '',
          price: 0,
          stock: 0,
          status: 1,
          imageUrl: '',
          description: ''
        }
      }
      this.dialogVisible = true
    },
    handleUploadSuccess (response) {
      if (response && response.code === 200) {
        this.form.imageUrl = response.data
        this.$message.success('上传成功')
      } else {
        const msg = response && response.message ? response.message : '上传失败'
        this.$message.error(msg)
      }
    },
    beforeUpload: createBeforeUpload({ maxSizeMB: 5 }),
    handleUploadError (err) {
      uploadErrorHandler(this, err)
    },
    async onSave () {
      if (!this.form.name) {
        this.$message.error('请输入名称')
        return
      }
      const payload = { ...this.form }
      try {
        if (payload.id) {
          await request({
            url: `/admin/goods/${payload.id}`,
            method: 'put',
            data: payload
          })
        } else {
          await request({
            url: '/admin/goods',
            method: 'post',
            data: payload
          })
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.fetchData()
      } catch (e) {
        handleError(this, e, '保存商品失败')
      }
    },
    auditStatusText (val) {
      const opt = this.goodsAuditStatusOptions[val]
      if (opt) return opt.label
      if (val === 0 || val === null || val === undefined) return '待审核'
      if (val === 1) return '已通过'
      if (val === 2) return '被退回'
      return '未知'
    },
    auditStatusType (val) {
      const opt = this.goodsAuditStatusOptions[val]
      if (opt) return opt.color
      if (val === 0 || val === null || val === undefined) return 'info'
      if (val === 1) return 'success'
      if (val === 2) return 'danger'
      return 'info'
    },
    goodsStatusText (val) {
      const opt = this.goodsStatusOptions[val]
      if (opt) return opt.label
      if (val === 1) return '交易中'
      return '未上架'
    },
    goodsStatusType (val) {
      const opt = this.goodsStatusOptions[val]
      if (opt) return opt.color
      return val === 1 ? 'success' : 'info'
    },
    async onDelete (id) {
      this.$confirm('确定要删除该商品吗？该操作不可恢复。', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await request({
            url: `/admin/goods/${id}`,
            method: 'delete'
          })
          this.$message.success('已删除')
          this.fetchData()
        } catch (e) {
          handleError(this, e, '删除商品失败')
        }
      }).catch(() => {})
    },
    async approve (row) {
      try {
        await request({
          url: `/admin/goods/${row.id}/approve`,
          method: 'put'
        })
        this.$message.success('已通过审核')
        this.fetchData()
      } catch (e) {
        handleError(this, e, '通过审核失败')
      }
    },
    openRejectDialog (row) {
      this.rejectForm = {
        id: row.id,
        reason: ''
      }
      this.rejectDialogVisible = true
    },
    async confirmReject () {
      if (!this.rejectForm.reason) {
        this.$message.error('请输入退回原因')
        return
      }
      try {
        await request({
          url: `/admin/goods/${this.rejectForm.id}/reject`,
          method: 'put',
          data: { reason: this.rejectForm.reason }
        })
        this.$message.success('已退回')
        this.rejectDialogVisible = false
        this.fetchData()
      } catch (e) {
        handleError(this, e, '退回失败')
      }
    }
  }
}
</script>

<style scoped>
.goods-page {
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