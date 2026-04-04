<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-logo">校园闲置物品平台</div>
      <div class="login-subtitle">平台登录</div>
      <el-form :model="form" @submit.native.prevent="onSubmit" label-width="0" class="login-form">
        <el-form-item>
          <el-input
            v-model="form.username"
            autocomplete="off"
            placeholder="用户名"
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="off"
            placeholder="密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="onSubmit" :loading="loading">
            登录
          </el-button>
        </el-form-item>
        <div class="login-extra">
          <span class="hint">没有账号？</span>
          <el-button type="text" @click="$router.push('/register')">去注册</el-button>
          <el-button type="text" @click="cpDialogVisible = true">修改密码</el-button>
        </div>
      </el-form>
      <el-dialog
        title="修改密码"
        :visible.sync="cpDialogVisible"
        width="380px"
        :close-on-click-modal="false"
      >
        <el-form :model="cpForm" label-width="0" class="cp-form">
          <el-form-item>
            <el-input
              v-model="cpForm.username"
              autocomplete="off"
              placeholder="用户名"
              prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="cpForm.oldPassword"
              type="password"
              autocomplete="off"
              placeholder="旧密码"
              prefix-icon="el-icon-lock"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="cpForm.newPassword"
              type="password"
              autocomplete="off"
              placeholder="新密码（6-32 位）"
              prefix-icon="el-icon-lock"
            />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="cpDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="cpLoading" @click="onChangePassword">
            提交修改
          </el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '@/store'
import request from '@/utils/request'
import { validateLoginForm, validateChangePasswordForm } from '@/utils/validators'
import { handleError } from '@/utils/error'

export default {
  name: 'UserLoginPage',
  data () {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      cpForm: {
        username: '',
        oldPassword: '',
        newPassword: ''
      },
      cpLoading: false,
      cpDialogVisible: false
    }
  },
  methods: {
    async onSubmit () {
      const msg = validateLoginForm(this.form)
      if (msg) {
        this.$message.error(msg)
        return
      }
      this.loading = true
      try {
        const store = useUserStore()
        const res = await store.login(this.form)
        if (res && res.code === 200) {
          this.$message.success('登录成功')
          // 如果是管理员或超级管理员，跳到后台商品页，否则去商品列表
          if (store.role === 'ADMIN' || store.role === 'SUPER_ADMIN') {
            this.$router.push('/admin/goods')
          } else {
            this.$router.push('/goods')
          }
        }
      } catch (e) {
        // 使用统一错误处理，显示后端返回的业务提示
        handleError(this, e, '登录失败')
      } finally {
        this.loading = false
      }
    },
    async onChangePassword () {
      const msg = validateChangePasswordForm(this.cpForm)
      if (msg) {
        this.$message.error(msg)
        return
      }
      this.cpLoading = true
      try {
        const res = await request({
          url: '/auth/changePassword',
          method: 'post',
          params: {
            username: this.cpForm.username,
            oldPassword: this.cpForm.oldPassword,
            newPassword: this.cpForm.newPassword
          }
        })
        if (res && res.code === 200) {
          this.$message.success('密码修改成功，请使用新密码登录')
          // 同步登录表单里的用户名
          this.form.username = this.cpForm.username
          // 清空密码
          this.form.password = ''
          this.cpForm.oldPassword = ''
          this.cpForm.newPassword = ''
          this.cpDialogVisible = false
        }
      } catch (e) {
        // 使用统一错误处理，显示修改密码失败原因
        handleError(this, e, '修改密码失败')
      } finally {
        this.cpLoading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #1f2a3d;
  background-image: radial-gradient(circle at 10% 20%, rgba(72, 118, 255, 0.35) 0, transparent 55%),
    radial-gradient(circle at 80% 80%, rgba(76, 175, 246, 0.28) 0, transparent 55%);
  color: #fff;
}
.login-panel {
  width: 420px;
  padding: 36px 40px 30px;
  border-radius: 12px;
  background: rgba(12, 22, 40, 0.96);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.45);
}
.login-logo {
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 8px;
}
.login-subtitle {
  text-align: center;
  margin-bottom: 24px;
  font-size: 14px;
  color: #a3b1cc;
}
.login-form .el-input__inner {
  background-color: #182233;
  border-color: #303b52;
  color: #e5eaf5;
}
.login-form .el-input__inner::placeholder {
  color: #6f7a93;
}
.login-form .el-input__inner:focus {
  border-color: #409eff;
}
.login-btn {
  width: 100%;
  margin-top: 4px;
}
.login-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #a3b1cc;
}
.login-extra .el-button--text {
  padding-left: 4px;
}
.login-demo {
  margin-top: 18px;
  font-size: 12px;
  color: #8f9bb5;
  text-align: center;
}
.login-demo span {
  color: #ffd666;
}
.cp-form .el-input__inner {
  background-color: #182233;
  border-color: #303b52;
  color: #e5eaf5;
}
</style>

