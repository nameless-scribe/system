<template>
  <div class="brand-page">
    <h2 class="title">品牌管理</h2>
    <div class="toolbar">
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增品牌</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="logoUrl" label="Logo" width="160">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.logoUrl"
            :src="scope.row.logoUrl"
            style="width: 80px; height: 40px"
            fit="contain"
          />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button type="text" @click="openDialog(scope.row)">编辑</el-button>
          <el-button type="text" style="color:red" @click="onDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑品牌' : '新增品牌'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-upload
            class="upload-demo"
            action="#"
            :http-request="customUpload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :on-error="handleUploadError"
          >
            <el-button size="small" type="primary">上传Logo</el-button>
          </el-upload>
          <div v-if="form.logoUrl" style="margin-top:10px;">
            <el-image :src="form.logoUrl" style="width: 100px; height: 50px" fit="contain" />
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
  </div>
</template>

<script>
import { fetchBrands, createBrand, updateBrand, deleteBrand } from '@/api/brand'
import { handleError } from '@/utils/error'
import { createBeforeUpload, createHttpRequest, handleUploadError as uploadErrorHandler } from '@/utils/upload'

export default {
  name: 'BrandList',
  data () {
    return {
      list: [],
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        logoUrl: '',
        description: ''
      }
    }
  },
  created () {
    this.load()
  },
  methods: {
    // 自定义上传方法（使用 axios，自动携带 token）
    customUpload: createHttpRequest('/admin/upload/image'),
    async load () {
      try {
        const res = await fetchBrands()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载品牌列表失败')
      }
    },
    openDialog (row) {
      if (row) {
        this.form = { ...row }
      } else {
        this.form = { id: null, name: '', logoUrl: '', description: '' }
      }
      this.dialogVisible = true
    },
    handleUploadSuccess (response) {
      if (response && response.code === 200) {
        this.form.logoUrl = response.data
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
      try {
        if (this.form.id) {
          await updateBrand(this.form.id, this.form)
        } else {
          await createBrand(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '保存品牌失败')
      }
    },
    async onDelete (row) {
      this.$confirm('确定要删除该品牌吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteBrand(row.id)
          this.$message.success('已删除')
          this.load()
        } catch (e) {
          handleError(this, e, '删除品牌失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.brand-page {
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

