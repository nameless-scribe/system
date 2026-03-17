<template>
  <div class="page rbac-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">权限管理</h2>
        <div class="page-subtitle">角色、权限点维护与分配（RBAC）</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="角色管理" name="roles">
        <div class="page-toolbar">
          <el-button type="primary" icon="el-icon-plus" @click="openRoleDialog()">新增角色</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        </div>
        <el-table :data="roles" border stripe>
          <el-table-column prop="code" label="角色编码" width="160" />
          <el-table-column prop="name" label="角色名称" />
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="220">
            <template slot-scope="scope">
              <el-button type="text" @click="openRoleDialog(scope.row)">编辑</el-button>
              <el-button type="text" @click="openRolePermDialog(scope.row)">分配权限</el-button>
              <el-button type="text" style="color:red" @click="onDeleteRole(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="权限点管理" name="perms">
        <div class="page-toolbar">
          <el-button type="primary" icon="el-icon-plus" @click="openPermDialog()">新增权限点</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        </div>
        <el-table :data="permissions" border stripe>
          <el-table-column prop="code" label="权限编码" min-width="220" show-overflow-tooltip />
          <el-table-column prop="name" label="权限名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <el-button type="text" @click="openPermDialog(scope.row)">编辑</el-button>
              <el-button type="text" style="color:red" @click="onDeletePerm(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="用户分配角色" name="userRoles">
        <div class="page-toolbar">
          <el-button icon="el-icon-refresh" @click="loadUsers">刷新用户</el-button>
        </div>
        <el-table :data="users" border stripe>
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="role" label="旧role字段" width="140" />
          <el-table-column label="操作" width="160">
            <template slot-scope="scope">
              <el-button type="text" @click="openUserRoleDialog(scope.row)">分配角色</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 角色弹窗 -->
    <el-dialog :title="roleForm.id ? '编辑角色' : '新增角色'" :visible.sync="roleDialogVisible" width="520px">
      <el-form :model="roleForm" label-width="90px">
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.code" placeholder="例如 ADMIN_AUDITOR" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.name" placeholder="例如 审计员" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="roleForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveRole">保存</el-button>
      </span>
    </el-dialog>

    <!-- 权限点弹窗 -->
    <el-dialog :title="permForm.id ? '编辑权限点' : '新增权限点'" :visible.sync="permDialogVisible" width="560px">
      <el-form :model="permForm" label-width="90px">
        <el-form-item label="权限编码">
          <el-input v-model="permForm.code" placeholder="例如 ADMIN:USER:READ" />
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model="permForm.name" placeholder="例如 用户管理-查询" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="permForm.module" placeholder="例如 USER" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="permForm.type" placeholder="例如 API" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="permForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="permForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSavePerm">保存</el-button>
      </span>
    </el-dialog>

    <!-- 角色分配权限 -->
    <el-dialog title="分配权限" :visible.sync="rolePermDialogVisible" width="620px">
      <div class="assign-tip">角色：<b>{{ currentRole && currentRole.name }}</b></div>
      <el-select v-model="selectedPermissionIds" multiple filterable style="width: 100%" placeholder="请选择权限点（可多选）">
        <el-option
          v-for="p in permissions"
          :key="p.id"
          :label="`${p.code}（${p.name}）`"
          :value="p.id"
        />
      </el-select>
      <span slot="footer">
        <el-button @click="rolePermDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveRolePerms">保存</el-button>
      </span>
    </el-dialog>

    <!-- 用户分配角色 -->
    <el-dialog title="分配角色" :visible.sync="userRoleDialogVisible" width="560px">
      <div class="assign-tip">用户：<b>{{ currentUser && currentUser.username }}</b></div>
      <el-select v-model="selectedRoleIds" multiple filterable style="width: 100%" placeholder="请选择角色（可多选）">
        <el-option v-for="r in roles" :key="r.id" :label="`${r.code}（${r.name}）`" :value="r.id" />
      </el-select>
      <span slot="footer">
        <el-button @click="userRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSaveUserRoles">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  fetchRoles,
  createRole,
  updateRole,
  deleteRole,
  fetchPermissions,
  createPermission,
  updatePermission,
  deletePermission,
  setUserRoles,
  setRolePermissions,
  fetchUserRoleIds,
  fetchRolePermissionIds
} from '@/api/rbac'
import { fetchUsers } from '@/api/adminUser'
import { handleError } from '@/utils/error'

export default {
  name: 'AdminRbacManage',
  data () {
    return {
      activeTab: 'roles',
      roles: [],
      permissions: [],
      users: [],

      roleDialogVisible: false,
      roleForm: { id: null, code: '', name: '', status: 1, sort: 0, remark: '' },

      permDialogVisible: false,
      permForm: { id: null, code: '', name: '', module: '', type: 'API', status: 1, sort: 0, remark: '' },

      rolePermDialogVisible: false,
      currentRole: null,
      selectedPermissionIds: [],

      userRoleDialogVisible: false,
      currentUser: null,
      selectedRoleIds: []
    }
  },
  created () {
    this.loadAll()
    this.loadUsers()
  },
  methods: {
    async loadAll () {
      try {
        const [r1, r2] = await Promise.all([fetchRoles(), fetchPermissions()])
        if (r1 && r1.code === 200) this.roles = r1.data || []
        if (r2 && r2.code === 200) this.permissions = r2.data || []
      } catch (e) {
        handleError(this, e, '加载RBAC数据失败')
      }
    },
    async loadUsers () {
      try {
        const res = await fetchUsers()
        if (res && res.code === 200) {
          this.users = res.data || []
        }
      } catch (e) {
        handleError(this, e, '加载用户列表失败')
      }
    },

    openRoleDialog (row) {
      this.roleForm = row
        ? { id: row.id, code: row.code, name: row.name, status: row.status, sort: row.sort, remark: row.remark }
        : { id: null, code: '', name: '', status: 1, sort: 0, remark: '' }
      this.roleDialogVisible = true
    },
    async onSaveRole () {
      if (!this.roleForm.code || !this.roleForm.name) {
        this.$message.error('请填写角色编码与名称')
        return
      }
      try {
        if (this.roleForm.id) {
          await updateRole(this.roleForm.id, this.roleForm)
        } else {
          await createRole(this.roleForm)
        }
        this.$message.success('保存成功')
        this.roleDialogVisible = false
        await this.loadAll()
      } catch (e) {
        handleError(this, e, '保存角色失败')
      }
    },
    onDeleteRole (row) {
      this.$confirm(`确认删除角色 ${row.code} 吗？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteRole(row.id)
            this.$message.success('已删除')
            await this.loadAll()
          } catch (e) {
            handleError(this, e, '删除角色失败')
          }
        })
        .catch(() => {})
    },

    openPermDialog (row) {
      this.permForm = row
        ? { ...row }
        : { id: null, code: '', name: '', module: '', type: 'API', status: 1, sort: 0, remark: '' }
      this.permDialogVisible = true
    },
    async onSavePerm () {
      if (!this.permForm.code || !this.permForm.name) {
        this.$message.error('请填写权限编码与名称')
        return
      }
      try {
        if (this.permForm.id) {
          await updatePermission(this.permForm.id, this.permForm)
        } else {
          await createPermission(this.permForm)
        }
        this.$message.success('保存成功')
        this.permDialogVisible = false
        await this.loadAll()
      } catch (e) {
        handleError(this, e, '保存权限点失败')
      }
    },
    onDeletePerm (row) {
      this.$confirm(`确认删除权限点 ${row.code} 吗？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deletePermission(row.id)
            this.$message.success('已删除')
            await this.loadAll()
          } catch (e) {
            handleError(this, e, '删除权限点失败')
          }
        })
        .catch(() => {})
    },

    openRolePermDialog (role) {
      this.currentRole = role
      this.selectedPermissionIds = []
      this.rolePermDialogVisible = true
      this.loadRolePermIds(role.id)
    },
    async loadRolePermIds (roleId) {
      try {
        const res = await fetchRolePermissionIds(roleId)
        if (res && res.code === 200) {
          this.selectedPermissionIds = res.data || []
        }
      } catch (e) {
        // 回显失败不阻断分配
        this.selectedPermissionIds = []
      }
    },
    async onSaveRolePerms () {
      if (!this.currentRole) return
      try {
        await setRolePermissions(this.currentRole.id, this.selectedPermissionIds || [])
        this.$message.success('保存成功')
        this.rolePermDialogVisible = false
      } catch (e) {
        handleError(this, e, '分配权限失败')
      }
    },

    openUserRoleDialog (user) {
      this.currentUser = user
      this.selectedRoleIds = []
      this.userRoleDialogVisible = true
      this.loadUserRoleIds(user.id)
    },
    async loadUserRoleIds (userId) {
      try {
        const res = await fetchUserRoleIds(userId)
        if (res && res.code === 200) {
          this.selectedRoleIds = res.data || []
        }
      } catch (e) {
        this.selectedRoleIds = []
      }
    },
    async onSaveUserRoles () {
      if (!this.currentUser) return
      try {
        await setUserRoles(this.currentUser.id, this.selectedRoleIds || [])
        this.$message.success('保存成功')
        this.userRoleDialogVisible = false
      } catch (e) {
        handleError(this, e, '分配角色失败')
      }
    }
  }
}
</script>

<style scoped>
.assign-tip {
  margin-bottom: 10px;
  color: #606266;
}
</style>

