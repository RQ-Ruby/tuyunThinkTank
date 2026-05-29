<template>
  <div id="userProfileView">
  <a-row justify="center">
    <a-col :span="20" :md="16" :lg="12">
      <a-card :bordered="false" class="profile-card">
          <template #title>
            <div class="card-title">
              <span>个人中心</span>
              <a-space>
                <a-button type="primary" @click="openEdit" v-if="isLoggedIn">编辑资料</a-button>
                <a-button v-else type="primary" href="/user/login">去登录</a-button>
              </a-space>
            </div>
          </template>

          <div v-if="isLoggedIn">
            <a-row :gutter="24" align="middle">
              <a-col :span="6">
                <div class="avatar-wrapper">
                  <a-avatar :src="profile.userAvatar" :size="96" />
                </div>
              </a-col>
              <a-col :span="18">
                <a-descriptions :column="1" size="middle">
                  <a-descriptions-item label="用户名">{{ profile.userName }}</a-descriptions-item>
                  <a-descriptions-item label="账号">
                    <a-space>
                      <span>{{ profile.userAccount }}</span>
                      <a-tooltip title="复制后可发送给团队管理员，邀请你加入团队空间">
                        <a-button size="small" @click="copyUserAccount">复制账号</a-button>
                      </a-tooltip>
                    </a-space>
                  </a-descriptions-item>
                  <a-descriptions-item label="身份">{{ roleLabel }}</a-descriptions-item>
                  <a-descriptions-item label="简介">{{ profile.userProfile || '（空）' }}</a-descriptions-item>
                  <a-descriptions-item label="创建时间">{{ formattedCreateTime }}</a-descriptions-item>
                </a-descriptions>
              </a-col>
            </a-row>
          </div>

          <div v-else class="empty-info">
            <a-empty description="请先登录后查看个人资料" />
          </div>
        </a-card>

        <!-- 我的空间列表 -->
        <div style="height: 16px" />
        <a-card :bordered="false" class="profile-card">
          <template #title>
            <div class="card-title">
              <span>我的空间</span>
            </div>
          </template>

          <div v-if="isLoggedIn">
            <a-list :data-source="mySpaces" :loading="spacesLoading" item-layout="horizontal">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="spaceTitle(item)"
                    :description="`共 ${item.totalCount || 0} 张照片`"
                  />
                  <template #actions>
                    <a-button type="link" :href="`/space/${item.id}`">进入空间</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
            <div v-if="!spacesLoading && mySpaces.length === 0" class="empty-info">
              <a-empty description="暂无空间">
                <a-button type="primary" href="/add_space">去创建空间</a-button>
              </a-empty>
            </div>
          </div>

          <div v-else class="empty-info">
            <a-empty description="请先登录后查看我的空间" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 编辑资料弹窗 -->
    <a-modal v-model:open="open" title="编辑个人资料" @ok="handleSave" @cancel="handleCancel">
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="用户名">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像地址">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像 URL" />
        </a-form-item>
        <a-form-item label="个人简介">
          <a-textarea v-model:value="editForm.userProfile" rows="4" placeholder="请输入简介" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
  
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { getLoginUserUsingGet, updateUserUsingPost } from '@/api/userController'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import dayjs from 'dayjs'

const loginUserStore = useLoginUserStore()

// 展示的资料
const profile = reactive<API.LoginUserVO>({})

// 是否已登录
const isLoggedIn = computed(() => !!loginUserStore.loginUser?.id)

// 角色文案
const roleLabel = computed(() => {
  const role = profile.userRole
  if (role === 'admin') return '管理员'
  if (role === 'user') return '普通用户'
  return '未设置'
})

// 创建时间格式化（YYYY年MM月DD日）
const formattedCreateTime = computed(() => {
  const t = profile.createTime
  if (!t) return '—'
  const d = dayjs(t)
  return d.isValid() ? d.format('YYYY年MM月DD日') : '—'
})

// 我的空间列表
const mySpaces = ref<API.SpaceVO[]>([])
const spacesLoading = ref(false)
const spaceTitle = (item: API.SpaceVO) => item.spaceName || `空间 #${item.id}`

// 获取我的空间
const fetchMySpaces = async () => {
  if (!loginUserStore.loginUser?.id) return
  spacesLoading.value = true
  try {
    const res = await listSpaceVoByPageUsingPost({
      userId: loginUserStore.loginUser.id,
      current: 1,
      pageSize: 10,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.data.code === 0 && res.data.data) {
      mySpaces.value = res.data.data.records ?? []
    } else {
      message.error('获取空间失败：' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间失败：' + e.message)
  } finally {
    spacesLoading.value = false
  }
}

// 编辑弹窗
const open = ref(false)
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

// 打开编辑弹窗
const openEdit = () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    return
  }
  editForm.id = loginUserStore.loginUser.id
  editForm.userName = profile.userName || ''
  editForm.userAvatar = profile.userAvatar || ''
  editForm.userProfile = profile.userProfile || ''
  open.value = true
}

// 保存资料
const handleSave = async () => {
  if (!editForm.id) {
    message.error('用户信息缺失，无法保存')
    return
  }
  const res = await updateUserUsingPost(editForm)
  if (res.data.code === 0) {
    message.success('资料更新成功')
    open.value = false
    // 更新本地展示与 store
    profile.userName = editForm.userName
    profile.userAvatar = editForm.userAvatar
    profile.userProfile = editForm.userProfile
    loginUserStore.setLoginUser({
      ...loginUserStore.loginUser,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile,
    })
    // 重新拉取后端登录用户，确保最新
    try {
      const latest = await getLoginUserUsingGet()
      if (latest.data.code === 0 && latest.data.data) {
        Object.assign(profile, latest.data.data)
        loginUserStore.setLoginUser(latest.data.data)
      }
    } catch {}
  } else {
    message.error('更新失败：' + res.data.message)
  }
}

const handleCancel = () => {
  open.value = false
}

// 复制我的账号
const copyUserAccount = async () => {
  const account = profile.userAccount
  if (!account) {
    message.warning('账号暂未加载')
    return
  }
  const text = String(account)
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      // 兼容老浏览器 / 非 https
      const input = document.createElement('textarea')
      input.value = text
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }
    message.success('已复制账号')
  } catch (e: any) {
    message.error('复制失败：' + e.message)
  }
}

// 初始化数据
onMounted(async () => {
  try {
    await loginUserStore.fetchLoginUser()
    Object.assign(profile, loginUserStore.loginUser)
    // 获取我的空间列表
    fetchMySpaces()
  } catch (e) {
    // 兜底再尝试拉取一次
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      Object.assign(profile, res.data.data)
      loginUserStore.setLoginUser(res.data.data)
      // 获取我的空间列表
      fetchMySpaces()
    }
  }
})
</script>

<style scoped>
#userProfileView {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: calc(100vh - 64px);
  padding: 24px;
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
}
</style>