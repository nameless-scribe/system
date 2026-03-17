<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2 class="title">注册新用户</h2>
      <el-form :model="form" label-width="80px" @submit.native.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirm" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">注册</el-button>
          <el-button type="text" @click="$router.push('/login')">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { registerUser } from '@/api/user'
import { validateRegisterForm } from '@/utils/validators'

export default {
  name: 'UserRegisterPage',
  data () {
    return {
      form: {
        username: '',
        password: '',
        confirm: ''
      },
      loading: false
    }
  },
  methods: {
    async onSubmit () {
      const msg = validateRegisterForm(this.form)
      if (msg) {
        this.$message.error(msg)
        return
      }
      this.loading = true
      try {
        await registerUser({
          username: this.form.username,
          password: this.form.password
        })
        this.$message.success('注册成功，请登录')
        this.$router.push('/login')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #1f2a3d;
  background-image: radial-gradient(circle at 10% 20%, rgba(72, 118, 255, 0.35) 0, transparent 55%),
    radial-gradient(circle at 80% 80%, rgba(76, 175, 246, 0.28) 0, transparent 55%);
}
.register-card {
  width: 440px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  border-radius: 10px;
}
.title {
  text-align: center;
  margin-bottom: 20px;
}
</style>

