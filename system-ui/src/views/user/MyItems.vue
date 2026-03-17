<template>
  <div class="page my-items-page">
    <div class="page-header">
      <h2 class="page-title">我发布的闲置</h2>
    </div>
    <div class="page-toolbar">
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">发布闲置</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="新旧程度" width="120">
        <template slot-scope="scope">
          {{ conditionText(scope.row.conditionLevel) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="statusType(scope.row)">
            {{ statusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="退回原因" width="180" show-overflow-tooltip />
      <el-table-column prop="imageUrl" label="图片" width="140">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            style="width: 60px; height: 60px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template slot-scope="scope">
          <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
          <el-button
            type="text"
            @click="on(scope.row)"
            v-if="scope.row.auditStatus === 1 && scope.row.status === 0"
          >上架</el-button>
          <el-button type="text" @click="off(scope.row)" v-if="scope.row.status === 1">下架</el-button>
          <el-button type="text" style="color: red" @click="onDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑闲置' : '发布闲置'" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.brandId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :step="0.01" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
        <el-form-item label="新旧程度">
          <el-select v-model="form.conditionLevel" placeholder="请选择" style="width: 100%">
            <el-option label="全新" :value="1" />
            <el-option label="9成新" :value="2" />
            <el-option label="8成新" :value="3" />
            <el-option label="7成新及以下" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="#"
            :http-request="customUpload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
          >
            <el-button size="small" type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="form.imageUrl" style="margin-top:10px;">
            <el-image :src="form.imageUrl" style="width: 100px; height: 100px" fit="cover" />
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" :rows="3" />
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
import { fetchMyItems, createMyItem, updateMyItem, offMyItem, onMyItem, deleteMyItem } from '@/api/userItem'
import { fetchPublicBrands } from '@/api/brand'
import { fetchDictData } from '@/api/dict'
import { conditionText } from '@/utils/display'
import { handleError } from '@/utils/error'
import { createBeforeUpload, createHttpRequest, handleUploadError as uploadErrorHandler } from '@/utils/upload'

export default {
  name: 'MyItems',
  data () {
    return {
      list: [],
      categories: [],
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        brandId: null,
        price: 0,
        stock: 1,
        status: 0,
        conditionLevel: 1,
        imageUrl: '',
        description: ''
      },
      goodsAuditStatusOptions: {},
      goodsStatusOptions: {}
    }
  },
  created () {
    this.loadCategories()
    this.load()
    this.loadGoodsAuditStatus()
    this.loadGoodsStatus()
  },
  methods: {
    // 自定义上传方法（使用 axios，自动携带 token）
    customUpload: createHttpRequest('/user/upload/image'),
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
        handleError(this, e, '加载商品审核状态失败')
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
        handleError(this, e, '加载商品状态失败')
      }
    },
    async loadCategories () {
      try {
        const res = await fetchPublicBrands()
        if (res && res.code === 200) {
          this.categories = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载分类失败')
      }
    },
    conditionText,
    async load () {
      try {
        const res = await fetchMyItems()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载列表失败')
      }
    },
    openDialog (row) {
      if (row) {
        this.form = { ...row }
      } else {
        this.form = {
          id: null,
          name: '',
          brandId: null,
          price: 0,
          stock: 1,
          status: 0,
          conditionLevel: 1,
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
    // 前端先校验大小，避免无意义的超大文件请求
    beforeUpload: createBeforeUpload({ maxSizeMB: 5 }),
    // 捕获 413 等错误，提示更明确的原因
    handleUploadError (err) {
      uploadErrorHandler(this, err)
    },
    async onSave () {
      if (!this.form.name) {
        this.$message.error('请输入名称')
        return
      }
      if (!this.form.brandId) {
        this.$message.error('请选择分类')
        return
      }
      const payload = { ...this.form }
      try {
        if (payload.id) {
          await updateMyItem(payload.id, payload)
        } else {
          await createMyItem(payload)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '保存失败')
      }
    },
    statusText (row) {
      const auditOpt = this.goodsAuditStatusOptions[row.auditStatus]
      const statusOpt = this.goodsStatusOptions[row.status]
      // 待审核或被退回：直接用审核状态字典
      if (row.auditStatus === 0 || row.auditStatus === 2) {
        if (auditOpt) return auditOpt.label
        return row.auditStatus === 0 ? '待审核' : '被退回'
      }
      // 已通过 + 未上架：显示审核通过
      if (row.auditStatus === 1 && row.status === 0) {
        if (auditOpt) return auditOpt.label
        return '已通过'
      }
      // 已通过 + 上架：显示上架状态（交易中）
      if (row.auditStatus === 1 && row.status === 1) {
        if (row.stock != null && Number(row.stock) <= 0) {
          return '已售罄'
        }
        if (statusOpt) return statusOpt.label
        return '交易中'
      }
      return '未知'
    },
    statusType (row) {
      const auditOpt = this.goodsAuditStatusOptions[row.auditStatus]
      const statusOpt = this.goodsStatusOptions[row.status]
      if (row.auditStatus === 0 || row.auditStatus === 2) {
        if (auditOpt) return auditOpt.color
        return row.auditStatus === 0 ? 'info' : 'danger'
      }
      if (row.auditStatus === 1 && row.status === 0) {
        if (auditOpt) return auditOpt.color || 'warning'
        return 'warning'
      }
      if (row.auditStatus === 1 && row.status === 1) {
        if (row.stock != null && Number(row.stock) <= 0) {
          return 'info'
        }
        if (statusOpt) return statusOpt.color || 'success'
        return 'success'
      }
      return 'info'
    },
    async off (row) {
      this.$confirm('确定要下架该闲置商品吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await offMyItem(row.id)
          this.$message.success('已下架')
          this.load()
        } catch (e) {
          handleError(this, e, '下架失败')
        }
      }).catch(() => {})
    },
    async on (row) {
      this.$confirm('确定要上架该闲置商品吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await onMyItem(row.id)
          this.$message.success('已上架')
          this.load()
        } catch (e) {
          handleError(this, e, '上架失败')
        }
      }).catch(() => {})
    },
    async onDelete (row) {
      this.$confirm('确定要删除该闲置商品吗？该操作不可恢复。', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteMyItem(row.id)
          this.$message.success('已删除')
          this.load()
        } catch (e) {
          handleError(this, e, '删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.my-items-page {
  padding: 8px 0 24px;
}
</style>

