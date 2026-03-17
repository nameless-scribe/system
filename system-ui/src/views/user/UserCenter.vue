<template>
  <div class="user-center">
    <h2 class="title">个人中心</h2>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="profile">
          <div class="profile-block">
            <el-form :model="profile" label-width="90px" label-position="left" size="small">
              <el-form-item label="用户名">
                <span>{{ userInfo.username }}</span>
              </el-form-item>
              <el-form-item label="角色">
                <span>{{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input
                  v-model="profile.realName"
                  placeholder="可选填写"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input
                  v-model="profile.phone"
                  placeholder="用于联系交易"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input
                  v-model="profile.email"
                  placeholder="可选填写"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item label="出生日期">
                <el-date-picker
                  v-model="profile.birthday"
                  type="date"
                  placeholder="选择日期"
                  style="width: 100%;"
                  value-format="yyyy-MM-dd"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item label="年龄">
                <el-input-number
                  v-model="profile.age"
                  :min="0"
                  :max="150"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-select
                  v-model="profile.gender"
                  placeholder="请选择"
                  clearable
                  style="width: 100%;"
                  :disabled="!editing"
                >
                  <el-option label="男" value="M" />
                  <el-option label="女" value="F" />
                  <el-option label="保密" value="O" />
                </el-select>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input
                  type="textarea"
                  v-model="profile.intro"
                  :rows="3"
                  :disabled="!editing"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  v-if="!editing"
                  type="primary"
                  size="small"
                  @click="startEdit"
                >
                  编辑资料
                </el-button>
                <template v-else>
                  <el-button type="primary" size="small" @click="onSaveProfile">
                    保存资料
                  </el-button>
                  <el-button size="small" @click="onCancelEdit">
                    取消
                  </el-button>
                </template>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我发布的闲置" name="items">
          <MyItems />
        </el-tab-pane>
        <el-tab-pane label="我买到的" name="orders">
          <OrderList />
        </el-tab-pane>
        <el-tab-pane label="我卖出的" name="sold">
          <SoldOrderList />
        </el-tab-pane>
        <el-tab-pane label="收到的评价" name="ratings">
          <ReceivedRatings />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { useUserStore } from '@/store'
import { fetchProfile, saveProfile } from '@/api/profile'
import MyItems from './MyItems.vue'
import OrderList from '../order/OrderList.vue'
import SoldOrderList from '../order/SoldOrderList.vue'
import ReceivedRatings from './ReceivedRatings.vue'
import { handleError } from '@/utils/error'

export default {
  name: 'UserCenter',
  components: { MyItems, OrderList, SoldOrderList, ReceivedRatings },
  data () {
    return {
      activeTab: 'profile',
      userInfo: {
        username: '',
        role: ''
      },
      profile: {
        realName: '',
        phone: '',
        email: '',
        birthday: '',
        age: null,
        gender: '',
        intro: ''
      },
      editing: false,
      originalProfile: null
    }
  },
  created () {
    const store = useUserStore()
    if (store.user) {
      this.userInfo = {
        username: store.user.username,
        role: store.role
      }
      this.loadProfile()
    }
  },
  methods: {
    async loadProfile () {
      try {
        const res = await fetchProfile()
        if (res && res.code === 200 && res.data) {
          this.profile = Object.assign({}, this.profile, res.data)
        }
      } catch (e) {
        handleError(this, e, '加载个人资料失败')
      }
    },
    startEdit () {
      // 进入编辑模式前备份一份原始数据，用于取消还原
      this.originalProfile = JSON.parse(JSON.stringify(this.profile))
      this.editing = true
    },
    async onSaveProfile () {
      try {
        await saveProfile(this.profile)
        this.$message.success('个人资料已保存')
        this.editing = false
        this.originalProfile = null
      } catch (e) {
        handleError(this, e, '保存个人资料失败')
      }
    },
    onCancelEdit () {
      if (this.originalProfile) {
        this.profile = JSON.parse(JSON.stringify(this.originalProfile))
      }
      this.editing = false
      this.originalProfile = null
    }
  }
}
</script>

<style scoped>
.user-center {
  padding: 8px 0 24px;
  text-align: left;
}
.title {
  margin-bottom: 12px;
}
.profile-block {
  padding: 12px 0;
  line-height: 1.8;
}
</style>

