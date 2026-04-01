<template>
  <div class="user-shell">
    <div class="uc-header">
      <div>
        <h2 class="uc-title">个人中心</h2>
        <div class="uc-subtitle">管理资料、发布、订单与评价</div>
      </div>
      <div class="uc-actions">
        <el-button size="small" @click="$router.push('/goods')">去逛商品</el-button>
        <el-button size="small" @click="$router.push('/my/items')">发布闲置</el-button>
      </div>
    </div>

    <div class="uc-layout">
      <aside class="uc-left">
        <el-card shadow="never" class="uc-card">
          <div class="panel-title"><i class="el-icon-user-solid" /> 我的信息</div>
          <div class="profile-summary">
            <el-avatar :size="56" icon="el-icon-user-solid" />
            <div class="summary-meta">
              <div class="summary-name">{{ userInfo.username || '用户' }}</div>
              <div class="summary-sub">
                {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </div>
            </div>
          </div>
          <div class="quick-links">
            <div class="quick-link" @click="activeTab='profile'">编辑资料</div>
            <div class="quick-link" @click="activeTab='items'">我发布的</div>
            <div class="quick-link" @click="activeTab='orders'">我买到的</div>
            <div class="quick-link" @click="activeTab='sold'">我卖出的</div>
            <div class="quick-link" @click="activeTab='ratings'">收到的评价</div>
          </div>
        </el-card>

        <el-card shadow="never" class="uc-card">
          <div class="panel-title"><i class="el-icon-guide" /> 快捷入口</div>
          <div class="quick-link" @click="$router.push('/favorites')">我的收藏</div>
          <div class="quick-link" @click="$router.push('/cart')">购物车</div>
          <div class="quick-link" @click="$router.push('/orders')">我的订单</div>
        </el-card>
      </aside>

      <main class="uc-main">
        <el-card shadow="never" class="uc-card">
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
          <OrderList :embedded="true" />
        </el-tab-pane>
        <el-tab-pane label="我卖出的" name="sold">
          <SoldOrderList :embedded="true" />
        </el-tab-pane>
        <el-tab-pane label="收到的评价" name="ratings">
          <ReceivedRatings />
        </el-tab-pane>
      </el-tabs>
        </el-card>
      </main>
    </div>
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
.user-shell {
  min-height: calc(100vh - 64px);
  padding: 12px 18px 32px;
  text-align: left;
}
.uc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.uc-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}
.uc-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}
.uc-actions {
  display: flex;
  gap: 10px;
}
.uc-layout {
  display: flex;
  gap: 18px;
}
.uc-left {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.uc-main {
  flex: 1;
  min-width: 0;
}
.uc-card {
  border-radius: 12px;
}
.panel-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.panel-title i {
  color: var(--primary-color);
  margin-right: 6px;
}
.profile-summary {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}
.summary-meta {
  min-width: 0;
}
.summary-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.summary-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}
.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.quick-link {
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}
.quick-link:hover {
  background: rgba(74, 144, 217, 0.10);
  color: var(--primary-color);
}
.profile-block {
  padding: 12px 0;
  line-height: 1.8;
}
@media (max-width: 768px) {
  .uc-layout {
    flex-direction: column;
  }
  .uc-left {
    width: 100%;
  }
}
</style>

