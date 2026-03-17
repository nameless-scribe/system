<template>
  <div class="dict-page">
    <h2 class="title">字典管理</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="字典类型" name="type">
        <div class="toolbar">
          <el-button type="primary" icon="el-icon-plus" @click="openTypeDialog()">新增类型</el-button>
        </div>
        <el-table :data="typeList" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="code" label="编码" width="160" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="openTypeDialog(scope.row)">编辑</el-button>
              <el-button type="text" style="color:red" @click="onDeleteType(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="字典数据" name="data">
        <div class="toolbar">
          <el-select
            v-model="currentTypeCode"
            placeholder="请选择字典类型"
            style="width: 260px; margin-right: 10px"
            @change="loadData"
          >
            <el-option
              v-for="t in typeList"
              :key="t.code"
              :label="t.name + ' (' + t.code + ')'"
              :value="t.code"
            />
          </el-select>
          <el-button
            type="primary"
            icon="el-icon-plus"
            :disabled="!currentTypeCode"
            @click="openDataDialog()"
          >新增数据</el-button>
        </div>
        <el-table :data="dataList" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="typeCode" label="类型编码" width="160" />
          <el-table-column prop="value" label="值" width="120" />
          <el-table-column prop="label" label="标签" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="color" label="颜色" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="openDataDialog(scope.row)">编辑</el-button>
              <el-button type="text" style="color:red" @click="onDeleteData(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 字典类型弹窗 -->
    <el-dialog :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" :visible.sync="typeDialogVisible" width="500px">
      <el-form :model="typeForm" label-width="90px">
        <el-form-item label="编码">
          <el-input v-model="typeForm.code" placeholder="例如：user_status" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="typeForm.name" placeholder="例如：用户状态" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="typeForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="typeForm.remark" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="typeDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="onSaveType">保 存</el-button>
      </span>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" :visible.sync="dataDialogVisible" width="520px">
      <el-form :model="dataForm" label-width="90px">
        <el-form-item label="类型编码">
          <el-input v-model="dataForm.typeCode" :disabled="!!currentTypeCode" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="dataForm.value" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="dataForm.label" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="dataForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="dataForm.color" placeholder="可选，例如：success/info/warning/danger" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="dataForm.remark" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dataDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="onSaveData">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  fetchDictTypes,
  createDictType,
  updateDictType,
  deleteDictType,
  fetchDictDataByType,
  createDictData,
  updateDictData,
  deleteDictData
} from '@/api/dict'
import { handleError } from '@/utils/error'

export default {
  name: 'DictManage',
  data () {
    return {
      activeTab: 'type',
      typeList: [],
      dataList: [],
      currentTypeCode: '',
      typeDialogVisible: false,
      dataDialogVisible: false,
      typeForm: {
        id: null,
        code: '',
        name: '',
        status: 1,
        remark: ''
      },
      dataForm: {
        id: null,
        typeCode: '',
        value: '',
        label: '',
        sort: 0,
        status: 1,
        color: '',
        remark: ''
      }
    }
  },
  created () {
    this.loadTypes()
  },
  methods: {
    async loadTypes () {
      try {
        const res = await fetchDictTypes()
        if (res && res.code === 200) {
          this.typeList = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载字典类型失败')
      }
    },
    async loadData () {
      if (!this.currentTypeCode) {
        this.dataList = []
        return
      }
      try {
        const res = await fetchDictDataByType(this.currentTypeCode)
        if (res && res.code === 200) {
          this.dataList = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载字典数据失败')
      }
    },
    openTypeDialog (row) {
      if (row) {
        this.typeForm = { ...row }
      } else {
        this.typeForm = {
          id: null,
          code: '',
          name: '',
          status: 1,
          remark: ''
        }
      }
      this.typeDialogVisible = true
    },
    async onSaveType () {
      if (!this.typeForm.code || !this.typeForm.name) {
        this.$message.error('请填写编码和名称')
        return
      }
      const payload = { ...this.typeForm }
      try {
        if (payload.id) {
          await updateDictType(payload.id, payload)
        } else {
          await createDictType(payload)
        }
        this.$message.success('保存成功')
        this.typeDialogVisible = false
        this.loadTypes()
      } catch (e) {
        handleError(this, e, '保存字典类型失败')
      }
    },
    async onDeleteType (id) {
      this.$confirm('确定要删除该字典类型及其下的所有数据吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteDictType(id)
          this.$message.success('已删除')
          this.loadTypes()
        } catch (e) {
          handleError(this, e, '删除字典类型失败')
        }
      }).catch(() => {})
    },
    openDataDialog (row) {
      if (row) {
        this.dataForm = { ...row }
      } else {
        this.dataForm = {
          id: null,
          typeCode: this.currentTypeCode || '',
          value: '',
          label: '',
          sort: 0,
          status: 1,
          color: '',
          remark: ''
        }
      }
      this.dataDialogVisible = true
    },
    async onSaveData () {
      if (!this.dataForm.typeCode || !this.dataForm.value || !this.dataForm.label) {
        this.$message.error('请填写类型编码、值、标签')
        return
      }
      const payload = { ...this.dataForm }
      try {
        if (payload.id) {
          await updateDictData(payload.id, payload)
        } else {
          await createDictData(payload)
        }
        this.$message.success('保存成功')
        this.dataDialogVisible = false
        this.loadData()
      } catch (e) {
        handleError(this, e, '保存字典数据失败')
      }
    },
    async onDeleteData (id) {
      this.$confirm('确定要删除该字典数据吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteDictData(id)
          this.$message.success('已删除')
          this.loadData()
        } catch (e) {
          handleError(this, e, '删除字典数据失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.dict-page {
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

