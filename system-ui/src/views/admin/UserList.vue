<template>
  <div class="page user-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>
    <div class="page-toolbar">
      <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增用户</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="scope">
          <el-tag :type="userStatusColor(scope.row.status)">
            {{ userStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="160">
        <template slot-scope="scope">
          <el-tag :type="roleTagType(scope.row.role)">
            {{ roleLabel(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template slot-scope="scope">
          <el-button type="text" v-if="canEdit(scope.row)" @click="openDialog(scope.row)">编辑</el-button>
          <el-button type="text" v-if="canToggleStatus(scope.row)" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '封禁' : '解封' }}
          </el-button>
          <el-button type="text" style="color:red" v-if="canDelete(scope.row)" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.id ? '编辑用户' : '新增用户'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
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
import { fetchUsers, createUser, updateUser, deleteUser, changeUserStatus } from '@/api/adminUser'
import { fetchDictData } from '@/api/dict'
import { useUserStore } from '@/store'
import { canManageUser } from '@/utils/auth'
import { handleError } from '@/utils/error'
import { userStatusLabel, userStatusType } from '@/utils/status'

export default {
  name: 'AdminUserList',
  data () {
    return {
      list: [],
      dialogVisible: false,
      form: {
        id: null,
        username: '',
        password: '',
        role: 'USER',
        status: 1
      },
      userStatusOptions: {}
    }
  },
  created () {
    this.load()
    this.loadUserStatus()
  },
  computed: {
    currentRole () {
      const store = useUserStore()
      return store.role
    },
    currentUserId () {
      const store = useUserStore()
      return store.user && store.user.id
    }
  },
  methods: {
    async loadUserStatus () {
      // 从字典中加载用户状态数据（需要在后台配置 user_status 类型及数据）
      try {
        const res = await fetchDictData('user_status')
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(item => {
            map[Number(item.value)] = {
              label: item.label,
              color: item.color || 'info'
            }
          })
          this.userStatusOptions = map
        }
      } catch (e) {
        handleError(this, e, '加载用户状态字典失败')
      }
    },
    async load () {
      try {
        const res = await fetchUsers()
        if (res && res.code === 200) {
          this.list = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载用户列表失败')
      }
    },
    roleLabel (role) {
      if (role === 'SUPER_ADMIN') return '超级管理员'
      if (role === 'ADMIN') return '管理员'
      return '普通用户'
    },
    roleTagType (role) {
      if (role === 'SUPER_ADMIN') return 'warning'
      if (role === 'ADMIN') return 'danger'
      return 'info'
    },
    canEdit (row) {
      return canManageUser(this.currentRole, this.currentUserId, row)
    },
    canToggleStatus (row) {
      return canManageUser(this.currentRole, this.currentUserId, row)
    },
    canDelete (row) {
      return canManageUser(this.currentRole, this.currentUserId, row)
    },
    userStatusLabel (status) {
      return userStatusLabel(status, this.userStatusOptions)
    },
    userStatusColor (status) {
      return userStatusType(status, this.userStatusOptions)
    },
    openDialog (row) {
      if (row) {
        this.form = {
          id: row.id,
          username: row.username,
          password: '',
          role: row.role,
          status: row.status
        }
      } else {
        this.form = {
          id: null,
          username: '',
          password: '',
          role: 'USER',
          status: 1
        }
      }
      this.dialogVisible = true
    },
    async onSave () {
      if (!this.form.username) {
        this.$message.error('请输入用户名')
        return
      }
      if (!this.form.id && !this.form.password) {
        this.$message.error('新增用户必须设置密码')
        return
      }
      const payload = { ...this.form }
      try {
        if (payload.id) {
          delete payload.password
          await updateUser(payload.id, payload)
        } else {
          await createUser(payload)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      } catch (e) {
        handleError(this, e, '保存用户失败')
      }
    },
    async onDelete (id) {
      this.$confirm('确定要删除该用户吗？该操作不可恢复。', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(id)
          this.$message.success('已删除')
          this.load()
        } catch (e) {
          handleError(this, e, '删除用户失败')
        }
      }).catch(() => {})
    },
    async toggleStatus (row) {
      const newStatus = row.status === 1 ? 0 : 1
      const actionText = newStatus === 1 ? '解封' : '封禁'
      this.$confirm(`确定要${actionText}该用户吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await changeUserStatus(row.id, newStatus)
          this.$message.success(newStatus === 1 ? '已解封' : '已封禁')
          this.load()
        } catch (e) {
          handleError(this, e, '更新用户状态失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-page {
  padding: 8px 0 24px;
}
</style>

